package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.application.usecase.CreateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.domain.service.UsuarioAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Promove um {@code Fiel} já membro ativo de uma organização a um papel de liderança e, se ele ainda não
 * possuir {@code Usuario}, cria um novo login vinculado a ele — o único ponto do sistema em que a
 * participação pastoral comum "vira" acesso administrativo. Restrito a Pároco, Vigário, Mesa do CPP ou Admin
 * (ver {@link UsuarioAuthorizationService#requireParocoVigarioMesaCppOuAdmin()}).
 */
@Service
public class PromoverACoordenadorUseCase {

    private final MembroGrupoJpaRepository membroRepository;
    private final FielJpaRepository fielRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final UsuarioAuthorizationService authorizationService;

    public PromoverACoordenadorUseCase(
            MembroGrupoJpaRepository membroRepository,
            FielJpaRepository fielRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            UsuarioJpaRepository usuarioRepository,
            CreateUsuarioUseCase createUsuarioUseCase,
            UsuarioAuthorizationService authorizationService) {
        this.membroRepository = membroRepository;
        this.fielRepository = fielRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.authorizationService = authorizationService;
    }

    /**
     * Promove o Fiel ao papel de liderança informado, exigindo que ele já seja membro ativo da organização e
     * que o ator autenticado tenha autoridade para promover. Cria um {@code Usuario} novo apenas se o Fiel
     * ainda não possuir um.
     *
     * @param request dados da promoção
     * @return o vínculo atualizado e o {@code usuarioId} (existente ou recém-criado)
     * @throws IllegalArgumentException se o Fiel/organização não existirem, se o papel for inválido, ou se o
     *                                   Fiel não for membro ativo da organização
     * @throws org.springframework.security.access.AccessDeniedException se o ator não tiver autoridade
     */
    @Transactional
    public PromoverACoordenadorResponse execute(PromoverACoordenadorRequest request) {
        authorizationService.requireParocoVigarioMesaCppOuAdmin();

        FielEntity fiel = fielRepository.findById(request.fielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + request.fielId()));

        OrganizacaoEntity organizacao = organizacaoRepository.findById(request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.organizacaoId()));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(request.papel());
        if (papel == null) {
            throw new IllegalArgumentException("Papel inválido: " + request.papel());
        }

        MembroGrupoEntity membro = membroRepository
                .findByFielIdAndOrganizacaoIdAndAtivoTrue(request.fielId(), request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fiel não é membro ativo desta organização: " + request.fielId()));

        membro.setPapel(papel.storedValue());
        membroRepository.save(membro);

        LiderancaSyncSupport.sync(organizacaoRepository, papel, fiel.getNome(), organizacao);

        var usuarioExistente = usuarioRepository.findByFielId(request.fielId());
        boolean usuarioCriado = usuarioExistente.isEmpty();
        var usuarioId = usuarioExistente
                .map(u -> u.getId())
                .orElseGet(() -> createUsuarioUseCase.execute(request.username(), request.password(),
                        request.fielId()));

        return new PromoverACoordenadorResponse(membro.getId(), fiel.getId(), usuarioId, usuarioCriado);
    }
}
