package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Abre a ata retificadora de uma ata selada (RN-CON-03). A original permanece intacta e recebe um registro
 * na trilha; a nova ata nasce em rascunho e percorre o fluxo normal do seu tipo.
 */
@Service
public class RetificarAtaUseCase {

    private static final String ACAO = "RETIFICAR";
    private static final String ACAO_NOVA = "RASCUNHAR";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public RetificarAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                               AtaAuditoriaService auditoria, AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Abre a retificação.
     *
     * @param id ata selada a retificar
     * @param justificativa motivo formal da retificação
     * @return a ata retificadora, em rascunho
     */
    @Transactional
    public AtaVisao execute(UUID id, String justificativa) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao original = autorizacao.carregar(id, ator, AcaoAta.RETIFICAR);

        AtaReuniao retificadora = repository.salvar(original.abrirRetificacao(AtaReuniaoId.gerar(), justificativa,
                ator.login(), Instant.now()));
        String estado = auditoria.capturar(original);
        auditoria.registrar(ator, ACAO, estado, original);
        auditoria.registrar(ator, ACAO_NOVA, null, retificadora);
        return montador.montar(ator, retificadora);
    }
}
