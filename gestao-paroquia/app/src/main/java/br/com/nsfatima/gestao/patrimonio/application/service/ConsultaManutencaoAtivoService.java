package br.com.nsfatima.gestao.patrimonio.application.service;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * API pública do contexto de patrimônio para o módulo de Reservas: expõe as janelas de manutenção
 * de um ativo que bloqueiam o calendário, sem vazar o agregado {@code AtivoManutencao}.
 */
@Service
public class ConsultaManutencaoAtivoService {

    private final AtivoManutencaoRepository manutencaoRepository;

    public ConsultaManutencaoAtivoService(AtivoManutencaoRepository manutencaoRepository) {
        this.manutencaoRepository = manutencaoRepository;
    }

    /**
     * Janelas (em aberto ou previstas) do ativo que se sobrepõem ao intervalo informado. Uma janela
     * em aberto é sempre bloqueante; uma prevista bloqueia apenas o seu próprio período.
     */
    @Transactional(readOnly = true)
    public List<JanelaManutencaoAtivo> janelasBloqueantes(UUID ativoId, Instant inicio, Instant fim) {
        return manutencaoRepository.findJanelasBloqueantes(new AtivoPatrimonialId(ativoId), inicio, fim).stream()
                .map(this::toJanela)
                .toList();
    }

    private JanelaManutencaoAtivo toJanela(AtivoManutencao m) {
        Instant fim = m.getDataFimReal() != null ? m.getDataFimReal() : m.getPeriodo().fimPrevista();
        return new JanelaManutencaoAtivo(m.getPeriodo().inicio(), fim, m.emAberto());
    }
}
