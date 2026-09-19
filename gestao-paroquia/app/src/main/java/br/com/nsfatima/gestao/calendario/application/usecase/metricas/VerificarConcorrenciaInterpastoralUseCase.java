package br.com.nsfatima.gestao.calendario.application.usecase.metricas;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.ConcorrenciaInterpastoralResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.ItemConflitoDTO;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Verifica potenciais conflitos de data, horário ou espaço físico entre pastorais distintas.
 */
@Service
public class VerificarConcorrenciaInterpastoralUseCase {

    private static final long MAX_INTERVALLO_DIAS_CONCORRENCIA = 3;

    private final EventoJpaRepository eventoJpaRepository;

    public VerificarConcorrenciaInterpastoralUseCase(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    /**
     * Motor analítico de detecção preventivo de sobreposição/concorrência interpastoral.
     *
     * @return DTO com contagem de conflitos, lista detalhada e nível de risco
     */
    @Transactional(readOnly = true)
    public ConcorrenciaInterpastoralResponse execute() {
        List<EventoEntity> eventos = eventoJpaRepository.findAll();
        List<ItemConflitoDTO> conflitos = new ArrayList<>();

        for (int i = 0; i < eventos.size(); i++) {
            for (int j = i + 1; j < eventos.size(); j++) {
                EventoEntity a = eventos.get(i);
                EventoEntity b = eventos.get(j);

                if (isConcorrencia(a, b)) {
                    conflitos.add(criarConflitoItem(a, b));
                }
            }
        }

        String risco = conflitos.isEmpty() ? "BAIXO" : (conflitos.size() > 2 ? "ALTO" : "MEDIO");

        return new ConcorrenciaInterpastoralResponse(
                conflitos.size(),
                conflitos,
                risco
        );
    }

    private boolean isConcorrencia(EventoEntity a, EventoEntity b) {
if (a.getGrupoParoquialResponsavelId() != null
                && a.getGrupoParoquialResponsavelId().equals(b.getGrupoParoquialResponsavelId())) {
            return false;
        }

        if (a.getInicioUtc() == null || b.getInicioUtc() == null) {
            return false;
        }

        long diasDiferenca = Math.abs(Duration.between(a.getInicioUtc(), b.getInicioUtc()).toDays());
        if (diasDiferenca > MAX_INTERVALLO_DIAS_CONCORRENCIA) {
            return false;
        }

        boolean mesmaCategoria = a.getCategoria() != null && a.getCategoria() == b.getCategoria();
        boolean mesmoTema = extrairPalavraChave(a.getTitulo()).equalsIgnoreCase(extrairPalavraChave(b.getTitulo()));

        return mesmaCategoria || mesmoTema;
    }

    private String extrairPalavraChave(String titulo) {
        if (titulo == null) {
            return "";
        }
        String t = titulo.toLowerCase();
        if (t.contains("casais") || t.contains("familia")) {
            return "CASAI_FAMILIA";
        }
        if (t.contains("jovens") || t.contains("juventude")) {
            return "JOVENS";
        }
        if (t.contains("catequese") || t.contains("crisma")) {
            return "CATEQUESE";
        }
        return titulo;
    }

    private ItemConflitoDTO criarConflitoItem(EventoEntity a, EventoEntity b) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

        return new ItemConflitoDTO(
                a.getId(),
                a.getTitulo(),
                a.getCategoria() != null ? a.getCategoria().name() : "Pastoral A",
                b.getId(),
                b.getTitulo(),
                b.getCategoria() != null ? b.getCategoria().name() : "Pastoral B",
                fmt.format(a.getInicioUtc()),
                "Público Geral/Familiar",
                "Risco de esvaziamento por concorrência de eventos similares em datas próximas."
        );
    }
}
