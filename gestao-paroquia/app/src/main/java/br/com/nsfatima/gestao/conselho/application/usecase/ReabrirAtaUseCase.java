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

import java.util.UUID;

/** Devolve ao redator uma ata vetada para correção, mantendo o motivo do veto à vista. */
@Service
public class ReabrirAtaUseCase {

    private static final String ACAO = "REABRIR";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public ReabrirAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                             AtaAuditoriaService auditoria, AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Reabre a ata vetada.
     *
     * @param id ata vetada
     * @return a ata de volta em rascunho
     */
    @Transactional
    public AtaVisao execute(UUID id) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.REABRIR);

        String antes = auditoria.capturar(ata);
        ata.reabrir();
        AtaReuniao salva = repository.salvar(ata);
        auditoria.registrar(ator, ACAO, antes, salva);
        return montador.montar(ator, salva);
    }
}
