package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.LavrarAssentoRequest;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.application.service.TeorAssentoFactory;
import br.com.nsfatima.gestao.sacramento.application.service.UnicidadeTermoService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/** Lavra um novo rascunho de assento (Secretaria ou Pároco); a fé pública só nasce na homologação. */
@Service
public class LavrarAssentoUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final TeorAssentoFactory teorFactory;
    private final UnicidadeTermoService unicidade;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;

    public LavrarAssentoUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                TeorAssentoFactory teorFactory, UnicidadeTermoService unicidade,
                                AssentoSacramentalRepository repository, SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.teorFactory = teorFactory;
        this.unicidade = unicidade;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public AssentoResponse execute(LavrarAssentoRequest request) {
        EventoActorContext ator = actorResolver.resolveRequired();
        Visibilidade visibilidade = Objects.requireNonNullElse(request.visibilidade(), Visibilidade.COMUM);
        acesso.exigir(ator, OperacaoSacramental.LAVRAR_RASCUNHO, visibilidade);

        TeorAssento teor = teorFactory.criar(request.teor());
        unicidade.garantir(request.tipo(), teor, null);
        AssentoSacramental assento = AssentoSacramental.lavrar(AssentoSacramentalId.gerar(), request.tipo(),
                visibilidade, teor, ator.actor(), Instant.now());
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "lavrar", null, salvo);
        return AssentoResponse.de(salvo);
    }
}
