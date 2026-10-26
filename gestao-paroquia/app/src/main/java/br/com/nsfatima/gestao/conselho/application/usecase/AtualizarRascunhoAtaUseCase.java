package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.CompatibilidadeOrgaoAta;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.application.service.ValidadorPresencaAta;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Substitui o conteúdo de uma ata em rascunho; ata submetida ou selada não admite edição. */
@Service
public class AtualizarRascunhoAtaUseCase {

    private static final String ACAO = "REDIGIR";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final ValidadorPresencaAta validadorPresenca;
    private final CompatibilidadeOrgaoAta compatibilidade;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public AtualizarRascunhoAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                                       ValidadorPresencaAta validadorPresenca, CompatibilidadeOrgaoAta compatibilidade,
                                       AtaAuditoriaService auditoria, AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.validadorPresenca = validadorPresenca;
        this.compatibilidade = compatibilidade;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Atualiza o rascunho.
     *
     * @param id ata a editar
     * @param conteudo novo conteúdo integral
     * @return a ata atualizada
     */
    @Transactional
    public AtaVisao execute(UUID id, ConteudoAta conteudo) {
        AtorAutenticado ator = autorizacao.atorAtual();
        AtaReuniao ata = autorizacao.carregar(id, ator, AcaoAta.REDIGIR);
        compatibilidade.exigir(ata);
        validadorPresenca.validar(ata.regras(), conteudo, ata.getOrganizacaoConvocanteId(),
                ata.getOrganizacaoAssociadaId());
        validadorPresenca.exigirRedatorPresente(ator, ata.regras(), conteudo, ata.getOrganizacaoConvocanteId());

        String antes = auditoria.capturar(ata);
        ata.redigir(conteudo);
        AtaReuniao salva = repository.salvar(ata);
        auditoria.registrar(ator, ACAO, antes, salva);
        return montador.montar(ator, salva);
    }
}
