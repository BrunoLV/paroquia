package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SlaDespachoPastoralResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

import java.util.List;

/**
 * Calcula o SLA médio de despacho e aprovação pastoral de eventos.
 */
@Service
public class CalcularSlaDespachoPastoralUseCase {

    private static final BigDecimal SLA_META_HORAS_PADRAO = BigDecimal.valueOf(48).setScale(1, RoundingMode.HALF_UP);
    private static final BigDecimal LIMIAR_EXCELENTE = BigDecimal.valueOf(24);
    private static final BigDecimal MINUTOS_POR_HORA = BigDecimal.valueOf(60);
    private static final BigDecimal CEM_PORCENTO = BigDecimal.valueOf(100).setScale(1, RoundingMode.HALF_UP);
    private static final int HORAS_SCALE = 1;

    private final AprovacaoJpaRepository aprovacaoJpaRepository;

    public CalcularSlaDespachoPastoralUseCase(AprovacaoJpaRepository aprovacaoJpaRepository) {
        this.aprovacaoJpaRepository = aprovacaoJpaRepository;
    }

    /**
     * Calcula o tempo médio decorrido para homologação pastoral pelo Pároco ou Conselhos.
     *
     * @return DTO com contadores, tempo médio em horas e avaliação de SLA
     */
    @Transactional(readOnly = true)
    public SlaDespachoPastoralResponse execute() {
        List<AprovacaoEntity> aprovacoes = aprovacaoJpaRepository.findAll();

        long total = aprovacoes.size();
        long aprovadas = aprovacoes.stream()
                .filter(a -> a.getStatus() == AprovacaoStatus.APROVADA)
                .count();
        long rejeitadas = aprovacoes.stream()
                .filter(a -> a.getStatus() == AprovacaoStatus.REPROVADA)
                .count();

        List<BigDecimal> duracoesHoras = aprovacoes.stream()
                .filter(a -> a.getCriadoEmUtc() != null && a.getDecididoEmUtc() != null)
                .map(a -> BigDecimal.valueOf(Duration.between(a.getCriadoEmUtc(), a.getDecididoEmUtc()).toMinutes())
                        .divide(MINUTOS_POR_HORA, HORAS_SCALE, RoundingMode.HALF_UP))
                .toList();

        BigDecimal tempoMedio = duracoesHoras.isEmpty()
                ? BigDecimal.ZERO.setScale(HORAS_SCALE, RoundingMode.HALF_UP)
                : duracoesHoras.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(duracoesHoras.size()), HORAS_SCALE, RoundingMode.HALF_UP);

        long dentroSlaCount = duracoesHoras.stream()
                .filter(d -> d.compareTo(SLA_META_HORAS_PADRAO) <= 0)
                .count();

        BigDecimal percentualDentroSla = duracoesHoras.isEmpty()
                ? CEM_PORCENTO
                : BigDecimal.valueOf(dentroSlaCount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(duracoesHoras.size()), HORAS_SCALE, RoundingMode.HALF_UP);

        String statusSla;
        if (tempoMedio.compareTo(LIMIAR_EXCELENTE) <= 0) {
            statusSla = "EXCELENTE";
        } else if (tempoMedio.compareTo(SLA_META_HORAS_PADRAO) <= 0) {
            statusSla = "DENTRO_DO_ESPERADO";
        } else {
            statusSla = "GARGALO_BUROCRATICO";
        }

        return new SlaDespachoPastoralResponse(
                total,
                aprovadas,
                rejeitadas,
                tempoMedio,
                SLA_META_HORAS_PADRAO,
                percentualDentroSla,
                statusSla
        );
    }
}
