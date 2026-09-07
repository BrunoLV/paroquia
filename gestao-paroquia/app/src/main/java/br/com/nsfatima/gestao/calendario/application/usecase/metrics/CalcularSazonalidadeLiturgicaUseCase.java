package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SazonalidadeLiturgicaResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Calcula a distribuição e sazonalidade de celebrações ao longo dos tempos litúrgicos.
 */
@Service
public class CalcularSazonalidadeLiturgicaUseCase {

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
        double fator = ciclo.getFatorDemanda() != null ? ciclo.getFatorDemanda() : 1.0;
        long eventosHistoricosBase = Math.round(10 * fator);
        long eventosPrevistos = Math.round(eventosHistoricosBase * fator);

        String nivel;
        if (fator >= 2.5) {
            nivel = "PICO_OPERACIONAL";
        } else if (fator >= 1.8) {
            nivel = "ALTA";
        } else if (fator >= 1.2) {
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
