package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.OciosidadePatrimonialResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

/**
 * Calcula a métrica de ociosidade de bens e espaços físicos da paróquia.
 */
@Service
public class CalcularOciosidadePatrimonialUseCase {

    private static final BigDecimal HORAS_UTEIS_POR_LOCAL_SEMANA = BigDecimal.valueOf(40);
    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);
    private static final int HORAS_SCALE = 1;
    private static final BigDecimal LIMIAR_OPORTUNIDADE = BigDecimal.valueOf(75);
    private static final BigDecimal LIMIAR_EXCELENTE = BigDecimal.valueOf(40);

    private final LocalJpaRepository localJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;

    public CalcularOciosidadePatrimonialUseCase(
            LocalJpaRepository localJpaRepository,
            EventoJpaRepository eventoJpaRepository) {
        this.localJpaRepository = localJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
    }

    /**
     * Calcula o Índice de Ociosidade Patrimonial das salas, capelas e auditórios paroquiais.
     *
     * @return DTO com total de locais, horas úteis, horas ociosas e recomendação do CAE
     */
    @Transactional(readOnly = true)
    public OciosidadePatrimonialResponse execute() {
        List<LocalEntity> locais = localJpaRepository.findAll();
        List<EventoEntity> eventos = eventoJpaRepository.findAll();

        long totalLocais = locais.isEmpty() ? 1 : locais.size();
        BigDecimal totalHorasCapacidade = BigDecimal.valueOf(totalLocais)
                .multiply(HORAS_UTEIS_POR_LOCAL_SEMANA)
                .setScale(HORAS_SCALE, RoundingMode.HALF_UP);

        long totalMinutosOcupados = eventos.stream()
                .filter(e -> e.getInicioUtc() != null && e.getFimUtc() != null)
                .mapToLong(e -> Duration.between(e.getInicioUtc(), e.getFimUtc()).toMinutes())
                .sum();
        BigDecimal horasOcupadas = BigDecimal.valueOf(totalMinutosOcupados)
                .divide(MINUTOS_POR_HORA, HORAS_SCALE, RoundingMode.HALF_UP);

        BigDecimal horasOciosas = totalHorasCapacidade.subtract(horasOcupadas).max(BigDecimal.ZERO);
        BigDecimal indiceOciosidade = totalHorasCapacidade.compareTo(BigDecimal.ZERO) > 0
                ? horasOciosas.multiply(BigDecimal.valueOf(100))
                        .divide(totalHorasCapacidade, HORAS_SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        String recomendacaoCae;
        if (indiceOciosidade.compareTo(LIMIAR_OPORTUNIDADE) >= 0) {
            recomendacaoCae = "OPORTUNIDADE_DE_NOVAS_FRENTES";
        } else if (indiceOciosidade.compareTo(LIMIAR_EXCELENTE) >= 0) {
            recomendacaoCae = "EXCELENTE_APROVEITAMENTO";
        } else {
            recomendacaoCae = "SUBAPROVEITAMENTO_CRITICO";
        }

        return new OciosidadePatrimonialResponse(
                totalLocais,
                totalHorasCapacidade,
                horasOcupadas,
                horasOciosas,
                indiceOciosidade,
                recomendacaoCae
        );
    }
}
