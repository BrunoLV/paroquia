package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.CompatibilidadeOrgaoAta;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Homologação da ata, ato privativo e indelegável do Pároco (RN-CON-02). Sela a ata e lhe atribui o número
 * sequencial da série da organização convocante no ano.
 */
@Service
public class HomologarAtaUseCase {

    private static final String ACAO = "HOMOLOGAR";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final CompatibilidadeOrgaoAta compatibilidade;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public HomologarAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                               CompatibilidadeOrgaoAta compatibilidade, AtaAuditoriaService auditoria,
                               AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.compatibilidade = compatibilidade;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Homologa a ata.
     *
     * @param id ata pendente de homologação
     * @return a ata selada e numerada
     */
    @Transactional
    public AtaVisao execute(UUID id) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.HOMOLOGAR);
        compatibilidade.exigir(ata);

        String antes = auditoria.capturar(ata);
        ata.homologar(ator.login(), repository, Instant.now());
        AtaReuniao salva = repository.salvar(ata);
        auditoria.registrar(ator, ACAO, antes, salva);
        return montador.montar(ator, salva);
    }
}
