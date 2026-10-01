package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.NotificacaoRequest;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.NotificacaoCanonica;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Registra a notificação ao pároco do batismo exigida por Crisma, Matrimônio e Ordem. */
@Service
public class RegistrarNotificacaoCanonicaUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;

    public RegistrarNotificacaoCanonicaUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                               AssentoSacramentalRepository repository,
                                               SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public AssentoResponse execute(UUID id, NotificacaoRequest request) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(id, ator, OperacaoSacramental.AVERBAR_DIGITAR);
        String antes = auditoria.capturar(assento);
        assento.registrarNotificacao(new NotificacaoCanonica(UUID.randomUUID(), request.destinatarioParoquia(),
                request.emitidaEm(), request.enviadaEm(), ator.actor()));
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "notificar", antes, salvo);
        return AssentoResponse.de(salvo);
    }
}
