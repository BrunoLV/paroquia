package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AtualizarRascunhoRequest;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.NotificacaoVinculoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.application.service.TeorAssentoFactory;
import br.com.nsfatima.gestao.sacramento.application.service.UnicidadeTermoService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/** Substitui o teor de um rascunho; assentos selados rejeitam a edição (use retificação). */
@Service
public class AtualizarRascunhoUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final TeorAssentoFactory teorFactory;
    private final UnicidadeTermoService unicidade;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;
    private final NotificacaoVinculoService notificacaoVinculo;

    public AtualizarRascunhoUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                    TeorAssentoFactory teorFactory, UnicidadeTermoService unicidade,
                                    AssentoSacramentalRepository repository, SacramentoAuditoriaService auditoria,
                                    NotificacaoVinculoService notificacaoVinculo) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.teorFactory = teorFactory;
        this.unicidade = unicidade;
        this.repository = repository;
        this.auditoria = auditoria;
        this.notificacaoVinculo = notificacaoVinculo;
    }

    @Transactional
    public AssentoResponse execute(UUID id, AtualizarRascunhoRequest request) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(id, ator, OperacaoSacramental.LAVRAR_RASCUNHO);
        String antes = auditoria.capturar(assento);
        Set<String> pendentesAntes = notificacaoVinculo.pendentes(assento);

        TeorAssento teor = teorFactory.criar(request.teor(),
                acesso.podeAprovarVinculo(ator, assento.getVisibilidade()), assento.getTeor().participantes());
        unicidade.garantir(assento.getTipo(), teor, id);
        assento.substituirTeor(teor);
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "atualizar", antes, salvo);
        notificacaoVinculo.notificarNovos(ator.actor(), pendentesAntes, salvo);
        return AssentoResponse.de(salvo);
    }
}
