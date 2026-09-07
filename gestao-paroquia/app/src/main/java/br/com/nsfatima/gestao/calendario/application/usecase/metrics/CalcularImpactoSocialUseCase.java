package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.CoeficienteImpactoSocialResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Calcula métricas agregadas de impacto pastoral e social das iniciativas comunitárias.
 */
@Service
public class CalcularImpactoSocialUseCase {

    private final EventoJpaRepository eventoJpaRepository;

    public CalcularImpactoSocialUseCase(EventoJpaRepository eventoJpaRepository) {
        this.eventoJpaRepository = eventoJpaRepository;
    }

    /**
     * Calcula o Coeficiente de Impacto Social ("Pastoral em Saída"), mensurando a proporção de ações
     * externas/caridade frente às reuniões internas/administrativas.
     *
     * @return DTO com contagens e diagnóstico pastoral
     */
    @Transactional(readOnly = true)
    public CoeficienteImpactoSocialResponse execute() {
        List<EventoEntity> eventos = eventoJpaRepository.findAll();

        long total = eventos.size();
        long sociais = eventos.stream()
                .filter(this::isEventoSocialOuCaridade)
                .count();
        long internas = eventos.stream()
                .filter(this::isReuniaoInterna)
                .count();

        double percentualSocial = total > 0 ? (sociais * 100.0) / total : 0.0;

        String diagnostico;
        if (percentualSocial >= 60.0) {
            diagnostico = "MISSIONARIA_EM_SAIDA";
        } else if (percentualSocial >= 30.0) {
            diagnostico = "EQUILIBRADA";
        } else {
            diagnostico = "EXCESSIVAMENTE_BUROCRATICA";
        }

        return new CoeficienteImpactoSocialResponse(
                total,
                sociais,
                internas,
                Math.round(percentualSocial * 10.0) / 10.0,
                diagnostico
        );
    }

    private boolean isEventoSocialOuCaridade(EventoEntity e) {
        String cat = e.getCategoria() != null ? e.getCategoria().name() : "";
        String titulo = e.getTitulo() != null ? e.getTitulo().toUpperCase() : "";
        return cat.contains("SOCIAL") || cat.contains("CARIDADE") || cat.contains("MISERICORDIA") ||
                titulo.contains("BAZAR") || titulo.contains("DONATIVO") || titulo.contains("SOLIDARI");
    }

    private boolean isReuniaoInterna(EventoEntity e) {
        String cat = e.getCategoria() != null ? e.getCategoria().name() : "";
        String titulo = e.getTitulo() != null ? e.getTitulo().toUpperCase() : "";
        return cat.contains("REUNIAO") || cat.contains("INTERNA") || cat.contains("ADMIN") ||
                titulo.contains("REUNIÃO") || titulo.contains("REUNIAO") || titulo.contains("CONSELHO");
    }
}
