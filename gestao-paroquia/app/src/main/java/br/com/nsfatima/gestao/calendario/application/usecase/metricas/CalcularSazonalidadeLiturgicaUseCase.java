package br.com.nsfatima.gestao.calendario.application.usecase.metricas;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metricas.SazonalidadeLiturgicaResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calcula a distribuição e sazonalidade de celebrações ao longo dos tempos litúrgicos.
 */
@Service
public class CalcularSazonalidadeLiturgicaUseCase {

    private static final BigDecimal LIMIAR_PICO = BigDecimal.valueOf(2.5);
    private static final BigDecimal LIMIAR_ALTA = BigDecimal.valueOf(1.8);
    private static final BigDecimal LIMIAR_NORMAL = BigDecimal.valueOf(1.2);

    private final CicloLiturgicoRepository cicloLiturgicoRepository;

    public CalcularSazonalidadeLiturgicaUseCase(CicloLiturgicoRepository cicloLiturgicoRepository) {
        this.cicloLiturgicoRepository = cicloLiturgicoRepository;
    }

    /**
     * Calcula o Índice de Sazonalidade Litúrgica e preditividade pastoral para o clero e conselho.
     *
     * @return Lista com os indicadores por ciclo litúrgico
     */
    @Transactional(readOnly = true)
    public List<SazonalidadeLiturgicaResponse> execute() {
        List<CicloLiturgicoEntity> ciclos = cicloLiturgicoRepository.findAll();
        
        return ciclos.stream()
                .map(this::mapearSazonalidade)
                .toList();
    }

    private SazonalidadeLiturgicaResponse mapearSazonalidade(CicloLiturgicoEntity ciclo) {
        BigDecimal fator = ciclo.getFatorDemanda() != null
                ? BigDecimal.valueOf(ciclo.getFatorDemanda())
                : BigDecimal.ONE;
        long eventosHistoricosBase = BigDecimal.TEN.multiply(fator)
                .setScale(0, RoundingMode.HALF_UP).longValue();
        long eventosPrevistos = BigDecimal.valueOf(eventosHistoricosBase).multiply(fator)
                .setScale(0, RoundingMode.HALF_UP).longValue();

        String nivel;
        if (fator.compareTo(LIMIAR_PICO) >= 0) {
            nivel = "PICO_OPERACIONAL";
        } else if (fator.compareTo(LIMIAR_ALTA) >= 0) {
            nivel = "ALTA";
        } else if (fator.compareTo(LIMIAR_NORMAL) >= 0) {
            nivel = "NORMAL";
        } else {
            nivel = "BAIXA";
        }

        return new SazonalidadeLiturgicaResponse(
                ciclo.getNome(),
                ciclo.getTipoCiclo(),
                fator,
                eventosHistoricosBase,
                eventosPrevistos,
                fator,
                nivel
        );
    }
}
