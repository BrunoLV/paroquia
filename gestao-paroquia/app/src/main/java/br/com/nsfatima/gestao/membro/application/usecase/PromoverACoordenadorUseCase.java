package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.application.usecase.CreateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.domain.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Promove um {@code Fiel} já membro ativo de uma organização a um papel de liderança e, se ele ainda não
 * possuir {@code Usuario}, cria um novo login vinculado a ele — o único ponto do sistema em que a
 * participação pastoral comum "vira" acesso administrativo. Restrito a quem tem papel global (Pároco,
 * Vigário, Admin ou Secretaria Paroquial — ver {@link PapelGlobalAuthorizationService}) ou é Mesa do CPP
 * (papel escopado, fora do domínio de papéis globais).
 */
@Service
public class PromoverACoordenadorUseCase {

    private final MembroGrupoJpaRepository membroRepository;
    private final FielJpaRepository fielRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final PapelGlobalAuthorizationService papelGlobalAuthorizationService;

    public PromoverACoordenadorUseCase(
            MembroGrupoJpaRepository membroRepository,
            FielJpaRepository fielRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository,
            UsuarioJpaRepository usuarioRepository,
            CreateUsuarioUseCase createUsuarioUseCase,
            PapelGlobalAuthorizationService papelGlobalAuthorizationService) {
        this.membroRepository = membroRepository;
        this.fielRepository = fielRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.usuarioRepository = usuarioRepository;
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.papelGlobalAuthorizationService = papelGlobalAuthorizationService;
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
        requireGlobalOuMesaCpp();

        FielEntity fiel = fielRepository.findById(request.fielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + request.fielId()));

        GrupoParoquialEntity grupoParoquial = grupoParoquialRepository.findById(request.grupoParoquialId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.grupoParoquialId()));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(request.papel());
        if (papel == null) {
            throw new IllegalArgumentException("Papel inválido: " + request.papel());
        }
        PapelGlobalValidation.rejectIfGlobal(papel);

        MembroGrupoEntity membro = membroRepository
                .findByFielIdAndGrupoParoquialIdAndAtivoTrue(request.fielId(), request.grupoParoquialId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fiel não é membro ativo desta organização: " + request.fielId()));

        membro.setPapel(papel.storedValue());
        membroRepository.save(membro);

        LiderancaSyncSupport.sync(grupoParoquialRepository, papel, fiel.getNome(), grupoParoquial);

        var usuarioExistente = usuarioRepository.findByFielId(request.fielId());
        boolean usuarioCriado = usuarioExistente.isEmpty();
        var usuarioId = usuarioExistente
                .map(u -> u.getId())
                .orElseGet(() -> createUsuarioUseCase.execute(request.username(), request.password(),
                        request.fielId()));

        return new PromoverACoordenadorResponse(membro.getId(), fiel.getId(), usuarioId, usuarioCriado);
    }

    /**
     * Restringe a promoção a quem tem papel global (via {@link PapelGlobalAuthorizationService}) ou é Mesa
     * do CPP — papel escopado, deliberadamente fora do domínio de papéis globais desta track.
     */
    private void requireGlobalOuMesaCpp() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Não autenticado");
        }

        boolean mesaCppOuAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CPP_MESA_CPP".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
        if (mesaCppOuAdmin) {
            return;
        }

        UUID actorId = auth.getPrincipal() instanceof UsuarioDetails details ? details.getUsuarioId() : null;
        if (actorId == null || !papelGlobalAuthorizationService.isGlobalByUsuarioId(actorId)) {
            throw new AccessDeniedException(
                    "Apenas Pároco, Vigário, Mesa do CPP, Admin ou Secretaria Paroquial podem promover um Fiel");
        }
    }
}
