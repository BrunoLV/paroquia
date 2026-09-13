package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.CoeficienteImpactoSocialResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calcula métricas agregadas de impacto pastoral e social das iniciativas comunitárias.
 */
@Service
public class CalcularImpactoSocialUseCase {

    private static final int PERCENTUAL_SCALE = 1;
    private static final BigDecimal LIMIAR_MISSIONARIA = BigDecimal.valueOf(60);
    private static final BigDecimal LIMIAR_EQUILIBRADA = BigDecimal.valueOf(30);

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

        BigDecimal percentualSocial = calcularPercentual(sociais, total);

        String diagnostico;
        if (percentualSocial.compareTo(LIMIAR_MISSIONARIA) >= 0) {
            diagnostico = "MISSIONARIA_EM_SAIDA";
        } else if (percentualSocial.compareTo(LIMIAR_EQUILIBRADA) >= 0) {
            diagnostico = "EQUILIBRADA";
        } else {
            diagnostico = "EXCESSIVAMENTE_BUROCRATICA";
        }

        return new CoeficienteImpactoSocialResponse(
                total,
                sociais,
                internas,
                percentualSocial,
                diagnostico
        );
    }

    /** Percentual numerador/denominador com precisão decimal exata; {@code ZERO} se não houver denominador. */
    private BigDecimal calcularPercentual(long numerador, long denominador) {
        return denominador == 0L
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(numerador)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominador), PERCENTUAL_SCALE, RoundingMode.HALF_UP);
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
