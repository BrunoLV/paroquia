package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.OciosidadePatrimonialResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class CalcularOciosidadePatrimonialUseCase {

    private static final double HORAS_UTEIS_POR_LOCAL_SEMANA = 40.0;

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
        double totalHorasCapacidade = totalLocais * HORAS_UTEIS_POR_LOCAL_SEMANA;

        double horasOcupadas = eventos.stream()
                .filter(e -> e.getInicioUtc() != null && e.getFimUtc() != null)
                .mapToDouble(e -> (double) Duration.between(e.getInicioUtc(), e.getFimUtc()).toMinutes() / 60.0)
                .sum();

        double horasOciosas = Math.max(0.0, totalHorasCapacidade - horasOcupadas);
        double indiceOciosidade = totalHorasCapacidade > 0 ? (horasOciosas * 100.0) / totalHorasCapacidade : 0.0;

        String recomendacaoCae;
        if (indiceOciosidade >= 75.0) {
            recomendacaoCae = "OPORTUNIDADE_DE_NOVAS_FRENTES";
        } else if (indiceOciosidade >= 40.0) {
            recomendacaoCae = "EXCELENTE_APROVEITAMENTO";
        } else {
            recomendacaoCae = "SUBAPROVEITAMENTO_CRITICO";
        }

        return new OciosidadePatrimonialResponse(
                totalLocais,
                totalHorasCapacidade,
                Math.round(horasOcupadas * 10.0) / 10.0,
                Math.round(horasOciosas * 10.0) / 10.0,
                Math.round(indiceOciosidade * 10.0) / 10.0,
                recomendacaoCae
        );
    }
}
