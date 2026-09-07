package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SlaDespachoPastoralResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import java.util.List;

/**
 * Calcula o SLA médio de despacho e aprovação pastoral de eventos.
 */
@Service
public class CalcularSlaDespachoPastoralUseCase {

    private static final double SLA_META_HORAS_PADRAO = 48.0;

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

        List<Double> duracoesHoras = aprovacoes.stream()
                .filter(a -> a.getCriadoEmUtc() != null && a.getDecididoEmUtc() != null)
                .map(a -> (double) Duration.between(a.getCriadoEmUtc(), a.getDecididoEmUtc()).toMinutes() / 60.0)
                .toList();

        double tempoMedio = duracoesHoras.isEmpty() ? 0.0 :
                duracoesHoras.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        long dentroSlaCount = duracoesHoras.stream()
                .filter(d -> d <= SLA_META_HORAS_PADRAO)
                .count();

        double percentualDentroSla = duracoesHoras.isEmpty() ? 100.0 :
                (dentroSlaCount * 100.0) / duracoesHoras.size();

        String statusSla;
        if (tempoMedio <= 24.0) {
            statusSla = "EXCELENTE";
        } else if (tempoMedio <= SLA_META_HORAS_PADRAO) {
            statusSla = "DENTRO_DO_ESPERADO";
        } else {
            statusSla = "GARGALO_BUROCRATICO";
        }

        return new SlaDespachoPastoralResponse(
                total,
                aprovadas,
                rejeitadas,
                Math.round(tempoMedio * 10.0) / 10.0,
                SLA_META_HORAS_PADRAO,
                Math.round(percentualDentroSla * 10.0) / 10.0,
                statusSla
        );
    }
}
