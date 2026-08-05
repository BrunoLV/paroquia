package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.application.usecase.CreateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.domain.service.UsuarioAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.PromoverACoordenadorRequest;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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

class PromoverACoordenadorUseCaseTest {

    private MembroGrupoJpaRepository membroRepository;
    private FielJpaRepository fielRepository;
    private OrganizacaoJpaRepository organizacaoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private CreateUsuarioUseCase createUsuarioUseCase;
    private UsuarioAuthorizationService authorizationService;
    private PromoverACoordenadorUseCase useCase;

    private UUID fielId;
    private UUID organizacaoId;

    @BeforeEach
    void setUp() {
        membroRepository = mock(MembroGrupoJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        createUsuarioUseCase = mock(CreateUsuarioUseCase.class);
        authorizationService = mock(UsuarioAuthorizationService.class);
        useCase = new PromoverACoordenadorUseCase(membroRepository, fielRepository, organizacaoRepository,
                usuarioRepository, createUsuarioUseCase, authorizationService);

        fielId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Maria Coordenadora");
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        OrganizacaoEntity org = new OrganizacaoEntity();
        org.setId(organizacaoId);
        org.setTipo(TipoOrganizacao.PASTORAL);
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.of(org));

        MembroGrupoEntity membro = new MembroGrupoEntity();
        membro.setId(UUID.randomUUID());
        membro.setFielId(fielId);
        membro.setOrganizacaoId(organizacaoId);
        membro.setPapel("VOLUNTARIO_PASTORAL_LOCAL");
        membro.setAtivo(true);
        when(membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId))
                .thenReturn(Optional.of(membro));
    }

    private PromoverACoordenadorRequest request() {
        return new PromoverACoordenadorRequest(fielId, organizacaoId, "COORD_PASTORAL_LOCAL", "maria.coord",
                "senhaForte123");
    }

    @Test
    void deveCriarUsuarioQuandoFielNaoTemUsuarioPrevio() {
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.empty());
        UUID novoUsuarioId = UUID.randomUUID();
        when(createUsuarioUseCase.execute("maria.coord", "senhaForte123", fielId)).thenReturn(novoUsuarioId);

        var response = useCase.execute(request());

        assertThat(response.usuarioId()).isEqualTo(novoUsuarioId);
        assertThat(response.usuarioCriado()).isTrue();
        verify(authorizationService).requireParocoVigarioMesaCppOuAdmin();
        verify(createUsuarioUseCase).execute("maria.coord", "senhaForte123", fielId);
    }

    @Test
    void deveReutilizarUsuarioExistenteSemDuplicar() {
        UUID usuarioExistenteId = UUID.randomUUID();
        UsuarioEntity usuarioExistente = new UsuarioEntity();
        usuarioExistente.setId(usuarioExistenteId);
        usuarioExistente.setFielId(fielId);
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.of(usuarioExistente));

        var response = useCase.execute(request());

        assertThat(response.usuarioId()).isEqualTo(usuarioExistenteId);
        assertThat(response.usuarioCriado()).isFalse();
        verify(createUsuarioUseCase, never()).execute(any(), any(), any());
    }

    @Test
    void deveAtualizarPapelDoVinculoESincronizarLideranca() {
        when(usuarioRepository.findByFielId(fielId)).thenReturn(Optional.of(new UsuarioEntity()));

        useCase.execute(request());

        var captor = org.mockito.ArgumentCaptor.forClass(MembroGrupoEntity.class);
        verify(membroRepository).save(captor.capture());
        assertThat(captor.getValue().getPapel()).isEqualTo("coord-pastoral-local");
        verify(organizacaoRepository).save(any(OrganizacaoEntity.class));
    }

    @Test
    void deveLancarExcecaoQuandoFielNaoEhMembroAtivo() {
        when(membroRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é membro ativo");
    }

    @Test
    void deveLancarExcecaoQuandoFielNaoEncontrado() {
        when(fielRepository.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fiel não encontrado");
    }

    @Test
    void deveLancarExcecaoQuandoOrganizacaoNaoEncontrada() {
        when(organizacaoRepository.findById(organizacaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Organização não encontrada");
    }

    @Test
    void deveLancarExcecaoQuandoPapelInvalido() {
        var request = new PromoverACoordenadorRequest(fielId, organizacaoId, "PAPEL_INEXISTENTE", "x", "y");

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Papel inválido");
    }

    @Test
    void devePropagarNegacaoDeAutorizacao() {
        doThrow(new AccessDeniedException("negado")).when(authorizationService)
                .requireParocoVigarioMesaCppOuAdmin();

        assertThatThrownBy(() -> useCase.execute(request()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
