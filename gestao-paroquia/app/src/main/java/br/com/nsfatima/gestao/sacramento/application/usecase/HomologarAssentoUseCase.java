package br.com.nsfatima.gestao.sacramento.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.RequisitosAssentoRegistry;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Homologa (sela) o termo: ato privativo do Pároco que confere fé pública e imutabilidade ao teor. */
@Service
public class HomologarAssentoUseCase {

    private final EventoActorContextResolver actorResolver;
    private final AssentoAcessoService acesso;
    private final RequisitosAssentoRegistry requisitos;
    private final AssentoSacramentalRepository repository;
    private final SacramentoAuditoriaService auditoria;

    public HomologarAssentoUseCase(EventoActorContextResolver actorResolver, AssentoAcessoService acesso,
                                   RequisitosAssentoRegistry requisitos, AssentoSacramentalRepository repository,
                                   SacramentoAuditoriaService auditoria) {
        this.actorResolver = actorResolver;
        this.acesso = acesso;
        this.requisitos = requisitos;
        this.repository = repository;
        this.auditoria = auditoria;
    }

    @Transactional
    public AssentoResponse execute(UUID id) {
        EventoActorContext ator = actorResolver.resolveRequired();
        AssentoSacramental assento = acesso.carregar(id, ator, OperacaoSacramental.HOMOLOGAR);
        String antes = auditoria.capturar(assento);
        assento.homologar(requisitos.para(assento.getTipo()), ator.actor(), Instant.now());
        AssentoSacramental salvo = repository.save(assento);
        auditoria.registrar(ator, "homologar", antes, salvo);
        return AssentoResponse.de(salvo);
    }
}
