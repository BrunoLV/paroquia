package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.RetificacaoRequest;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Retificação material de termo selado: o Pároco atesta uma ressalva; o teor original não é sobrescrito. */
@Service
public class RetificarAssentoUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;

    public RetificarAssentoUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                   AssentoSacramentalRepository repository, SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public AssentoResponse execute(UUID id, RetificacaoRequest request) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(id, ator, OperacaoSacramental.RETIFICAR);
        String antes = auditoria.capturar(assento);
        assento.retificar(request.campo(), request.valorOriginal(), request.valorCorrigido(),
                request.justificativa(), ator.actor(), Instant.now());
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "retificar", antes, salvo);
        return AssentoResponse.de(salvo);
    }
}
