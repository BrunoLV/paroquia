package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.dto.RascunharAtaComando;
import br.com.nsfatima.gestao.conselho.application.port.out.OrganizacoesPort;
import br.com.nsfatima.gestao.conselho.application.service.AtaAuditoriaService;
import br.com.nsfatima.gestao.conselho.application.service.AtaVisaoMontador;
import br.com.nsfatima.gestao.conselho.application.service.ConselhoAuthorizationService;
import br.com.nsfatima.gestao.conselho.application.service.ValidadorPresencaAta;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Abre uma ata em rascunho. Só quem é redator do tipo na organização convocante pode abri-la; o agregado
 * confere se a organização pode convocar o tipo e o validador confere a presença contra o cadastro.
 */
@Service
public class RascunharAtaUseCase {

    private static final String ACAO = "RASCUNHAR";

    private final AtaRepository repository;
    private final ConselhoAuthorizationService autorizacao;
    private final OrganizacoesPort organizacoes;
    private final ValidadorPresencaAta validadorPresenca;
    private final AtaAuditoriaService auditoria;
    private final AtaVisaoMontador montador;

    public RascunharAtaUseCase(AtaRepository repository, ConselhoAuthorizationService autorizacao,
                               OrganizacoesPort organizacoes, ValidadorPresencaAta validadorPresenca,
                               AtaAuditoriaService auditoria, AtaVisaoMontador montador) {
        this.repository = repository;
        this.autorizacao = autorizacao;
        this.organizacoes = organizacoes;
        this.validadorPresenca = validadorPresenca;
        this.auditoria = auditoria;
        this.montador = montador;
    }

    /**
     * Cria o rascunho.
     *
     * @param comando tipo, organizações e conteúdo inicial
     * @return a ata criada, como o ator a enxerga
     */
    @Transactional
    public AtaVisao execute(RascunharAtaComando comando) {
        AtorAutenticado ator = autorizacao.atorAtual();
        autorizacao.exigirRedacao(ator, comando.tipo(), comando.organizacaoConvocanteId(),
                comando.organizacaoAssociadaId());
        ReferenciaOrganizacao convocante = exigirOrganizacao(comando.organizacaoConvocanteId());
        ReferenciaOrganizacao associada = comando.organizacaoAssociadaId() == null
                ? null : exigirOrganizacao(comando.organizacaoAssociadaId());
        validadorPresenca.validar(RegrasTipoReuniaoPolicy.para(comando.tipo()), comando.conteudo(),
                convocante.id(), comando.organizacaoAssociadaId());
        validadorPresenca.exigirRedatorPresente(ator, RegrasTipoReuniaoPolicy.para(comando.tipo()),
                comando.conteudo(), convocante.id());

        AtaReuniao ata = repository.salvar(AtaReuniao.rascunhar(AtaReuniaoId.gerar(), comando.tipo(), convocante,
                associada, comando.conteudo(), ator.login(), Instant.now()));
        auditoria.registrar(ator, ACAO, null, ata);
        return montador.montar(ator, ata);
    }

    private ReferenciaOrganizacao exigirOrganizacao(UUID id) {
        return organizacoes.buscar(id)
                .orElseThrow(() -> new AtaInvalidaException("Organização não encontrada ou inativa."));
    }
}
