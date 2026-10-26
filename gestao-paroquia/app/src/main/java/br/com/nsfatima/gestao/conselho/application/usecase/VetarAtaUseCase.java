package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Veto da ata pelo Pároco, com motivo obrigatório; a ata volta ao redator por reabertura. */
@Service
public class VetarAtaUseCase {

    private static final String ACAO = "VETAR";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public VetarAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                           AtaAuditoriaService auditoria, AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Veta a ata.
     *
     * @param id ata pendente de homologação
     * @param motivo justificativa do veto
     * @return a ata vetada
     */
    @Transactional
    public AtaVisao execute(UUID id, String motivo) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.VETAR);

        String antes = auditoria.capturar(ata);
        ata.vetar(ator.login(), motivo, Instant.now());
        AtaReuniao salva = repository.salvar(ata);
        auditoria.registrar(ator, ACAO, antes, salva);
        return montador.montar(ator, salva);
    }
}
