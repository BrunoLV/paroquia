package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.application.service.UsuarioAuthorizationService;
import br.com.nsfatima.gestao.iam.application.usecase.AvaliarAcessoPastoralUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoverVinculoUseCaseTest {

    private MembroGrupoJpaRepository vinculoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private UsuarioAuthorizationService authorizationService;
    private AvaliarAcessoPastoralUseCase acessoPastoral;
    private RemoverVinculoUseCase useCase;

    private final UUID usuarioId = UUID.randomUUID();
    private final UUID fielId = UUID.randomUUID();
    private final UUID vinculoId = UUID.randomUUID();
    private final UUID organizacaoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        vinculoRepository = mock(MembroGrupoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        authorizationService = mock(UsuarioAuthorizationService.class);
        acessoPastoral = mock(AvaliarAcessoPastoralUseCase.class);
        useCase = new RemoverVinculoUseCase(vinculoRepository, usuarioRepository, authorizationService,
                acessoPastoral);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setFielId(fielId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
    }

    private MembroGrupoEntity vinculo(UUID dono) {
        MembroGrupoEntity entity = new MembroGrupoEntity();
        entity.setId(vinculoId);
        entity.setFielId(dono);
        entity.setOrganizacaoId(organizacaoId);
        entity.setAtivo(true);
        return entity;
    }

    @Test
    void deveDesativarVinculoDoProprioFielAposAutorizarNaOrganizacaoDoVinculo() {
        MembroGrupoEntity entity = vinculo(fielId);
        when(vinculoRepository.findById(vinculoId)).thenReturn(Optional.of(entity));

        useCase.execute(usuarioId, vinculoId);

        verify(authorizationService).requireAdminOrCoordinatorOfOrganization(organizacaoId);
        assertThat(entity.isAtivo()).isFalse();
        verify(vinculoRepository).save(entity);
        verify(acessoPastoral).suspenderSemVinculo(fielId);
    }

    @Test
    void naoDeveRemoverVinculoDeOutroFielMesmoComIdValido() {
        MembroGrupoEntity entity = vinculo(UUID.randomUUID());
        when(vinculoRepository.findById(vinculoId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> useCase.execute(usuarioId, vinculoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Membro nao encontrado");

        assertThat(entity.isAtivo()).isTrue();
        verify(vinculoRepository, never()).save(any());
    }

    @Test
    void naoDeveRemoverVinculoDeOrganizacaoOndeOAtorNaoCoordena() {
        MembroGrupoEntity entity = vinculo(fielId);
        when(vinculoRepository.findById(vinculoId)).thenReturn(Optional.of(entity));
        doThrow(new AccessDeniedException("negado"))
                .when(authorizationService).requireAdminOrCoordinatorOfOrganization(organizacaoId);

        assertThatThrownBy(() -> useCase.execute(usuarioId, vinculoId))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(entity.isAtivo()).isTrue();
        verify(vinculoRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoVinculoNaoEncontrado() {
        when(vinculoRepository.findById(vinculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(usuarioId, vinculoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Membro nao encontrado");
    }
}
