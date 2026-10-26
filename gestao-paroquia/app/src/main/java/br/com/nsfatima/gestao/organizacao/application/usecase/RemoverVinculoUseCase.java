package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.application.service.UsuarioAuthorizationService;
import br.com.nsfatima.gestao.iam.application.usecase.AvaliarAcessoPastoralUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revoga o vínculo de um Fiel com uma organização, desativando-o (sem excluí-lo) para preservar o
 * histórico de quem já atuou naquela organização e em que papel.
 *
 * <p>O identificador do vínculo sozinho não prova nada: a remoção exige que o vínculo pertença ao Fiel do
 * usuário da rota e que o ator tenha autoridade sobre a organização do vínculo — coordenar uma organização
 * em comum com o usuário não autoriza mexer nos vínculos dele em outras organizações.</p>
 */
@Service
public class RemoverVinculoUseCase {

    private final MembroGrupoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final UsuarioAuthorizationService authorizationService;
    private final AvaliarAcessoPastoralUseCase acessoPastoral;

    public RemoverVinculoUseCase(
            MembroGrupoJpaRepository vinculoRepository,
            UsuarioJpaRepository usuarioRepository,
            UsuarioAuthorizationService authorizationService,
            AvaliarAcessoPastoralUseCase acessoPastoral) {
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.authorizationService = authorizationService;
        this.acessoPastoral = acessoPastoral;
    }

    /**
     * Desativa o vínculo de um Fiel com uma organização para revogar seus acessos e responsabilidades
     * administrativas naquele contexto específico, mantendo o registro histórico do vínculo.
     *
     * @param usuarioId identificador do usuário dono do vínculo, conforme a rota
     * @param vinculoId identificador do vínculo a desativar
     * @throws IllegalArgumentException se o vínculo não existir ou não pertencer ao Fiel do usuário (a mesma
     *     resposta nos dois casos, para não permitir sondar identificadores de vínculos alheios)
     * @throws org.springframework.security.access.AccessDeniedException se o ator não for admin global nem
     *     coordenador da organização do vínculo
     */
    @Transactional
    public void execute(UUID usuarioId, UUID vinculoId) {
        UUID fielId = usuarioRepository.findById(usuarioId)
                .map(u -> u.getFielId())
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + vinculoId));
        MembroGrupoEntity entity = vinculoRepository.findById(vinculoId)
                .filter(v -> fielId != null && fielId.equals(v.getFielId()))
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + vinculoId));

        authorizationService.requireAdminOrCoordinatorOfOrganization(entity.getOrganizacaoId());

        entity.setAtivo(false);
        vinculoRepository.save(entity);
        acessoPastoral.suspenderSemVinculo(entity.getFielId());
    }
}
