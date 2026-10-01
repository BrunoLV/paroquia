package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.VincularParticipanteRequest;
import br.com.nsfatima.gestao.sacramento.application.port.out.ConsultaPessoaPort;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Vincula ou desvincula tardiamente um participante a um cadastro de fiel/clérigo. Altera apenas o
 * ponteiro relacional (inclusive em termo selado); o snapshot textual permanece intocado.
 */
@Service
public class VincularParticipanteUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final ConsultaPessoaPort pessoas;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;

    public VincularParticipanteUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                       ConsultaPessoaPort pessoas, AssentoSacramentalRepository repository,
                                       SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.pessoas = pessoas;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public AssentoResponse vincular(UUID assentoId, UUID participanteId, VincularParticipanteRequest request) {
        if ((request.fielId() == null) == (request.clerigoId() == null)) {
            throw new IllegalArgumentException("Informe exatamente um entre fielId e clerigoId.");
        }
        return executar(assentoId, "vincular", assento -> {
            if (request.fielId() != null) {
                pessoas.nomeDoFiel(request.fielId())
                        .orElseThrow(() -> new IllegalArgumentException("Fiel informado não está cadastrado."));
                assento.vincularFiel(participanteId, request.fielId());
            } else {
                pessoas.nomeDoClerigo(request.clerigoId())
                        .orElseThrow(() -> new IllegalArgumentException("Clérigo informado não está cadastrado."));
                assento.vincularClerigo(participanteId, request.clerigoId());
            }
        });
    }

    @Transactional
    public AssentoResponse desvincular(UUID assentoId, UUID participanteId) {
        return executar(assentoId, "desvincular", assento -> assento.desvincular(participanteId));
    }

    private AssentoResponse executar(UUID assentoId, String acao, Consumer<AssentoSacramental> op) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(assentoId, ator, OperacaoSacramental.VINCULAR);
        String antes = auditoria.capturar(assento);
        op.accept(assento);
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, acao, antes, salvo);
        return AssentoResponse.de(salvo);
    }
}
