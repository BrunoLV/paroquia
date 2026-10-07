package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.EditarDescricaoAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNaoEncontradoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Única edição permitida sobre um ativo já cadastrado: o campo {@code descricao}. Todos os demais
 * campos são estruturalmente imutáveis e não são aceitos pelo contrato de entrada.
 */
@Service
public class EditarDescricaoAtivoUseCase {

    private final AtivoPatrimonialRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;

    public EditarDescricaoAtivoUseCase(AtivoPatrimonialRepository repository,
                                       EventoActorContextResolver actorContextResolver,
                                       PatrimonioAuthorizationService authorizationService) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId, EditarDescricaoAtivoRequest request) {
        AtivoPatrimonial ativo = carregar(ativoId);

        EventoActorContext actor = actorContextResolver.resolveRequired();
        authorizationService.assertPodeGerirPatrimonio(actor, ativo.getOrganizacaoId());

        ativo.atualizarDescricao(request.descricao(), Instant.now());
        return AtivoPatrimonialResponse.de(repository.save(ativo));
    }

    private AtivoPatrimonial carregar(UUID ativoId) {
        AtivoPatrimonial ativo = repository.findById(new AtivoPatrimonialId(ativoId))
                .orElseThrow(() -> new AtivoPatrimonialNaoEncontradoException("Ativo não encontrado: " + ativoId));
        if (ativo.estaExcluido()) {
            throw new AtivoPatrimonialNaoEncontradoException("Ativo não encontrado: " + ativoId);
        }
        return ativo;
    }
}
