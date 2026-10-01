package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CriarUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private FielJpaRepository fielRepository;
    private ClerigoJpaRepository clerigoRepository;
    private GerenciarNotificacaoUseCase notificacaoUseCase;
    private CriarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        clerigoRepository = mock(ClerigoJpaRepository.class);
        notificacaoUseCase = mock(GerenciarNotificacaoUseCase.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        useCase = new CriarUsuarioUseCase(
                usuarioRepository,
                fielRepository,
                clerigoRepository,
                passwordEncoder,
                notificacaoUseCase);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveSalvarUsuarioComDadosCorretos() {
        UUID fielId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("novo.usuario")).thenReturn(Optional.empty());
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(new FielEntity()));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("novo.usuario", "senhaForte", fielId, null);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        UsuarioEntity saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("novo.usuario");
        assertThat(saved.getFielId()).isEqualTo(fielId);
        assertThat(saved.isEnabled()).isTrue();
    }

    @Test
    void deveCriarUsuarioComFielIdExistente() {
        UUID fielId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("novo.usuario")).thenReturn(Optional.empty());
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(new FielEntity()));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("novo.usuario", "senhaForte", fielId, null);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getFielId()).isEqualTo(fielId);
        assertThat(captor.getValue().getClerigoId()).isNull();
    }

    @Test
    void deveCriarUsuarioComClerigoIdExistente() {
        UUID clerigoId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("novo.padre")).thenReturn(Optional.empty());
        when(clerigoRepository.findById(clerigoId)).thenReturn(Optional.of(new ClerigoEntity()));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("novo.padre", "senhaForte", null, clerigoId);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getClerigoId()).isEqualTo(clerigoId);
        assertThat(captor.getValue().getFielId()).isNull();
    }

    @Test
    void deveCriarUsuarioPendentePelaSecretariaEDispararNotificacoes() {
        UUID fielId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID parocoId = UUID.randomUUID();

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Maria da Silva");

        UsuarioEntity admin = new UsuarioEntity();
        admin.setId(adminId);
        admin.setRoles("ROLE_ADMIN");

        UsuarioEntity paroco = new UsuarioEntity();
        paroco.setId(parocoId);
        paroco.setRoles("ROLE_PAROCO");

        when(usuarioRepository.findByUsernameIgnoreCase("maria.silva")).thenReturn(Optional.empty());
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(usuarioRepository.findAprovadoresAtivos()).thenReturn(List.of(admin, paroco));

        useCase.execute("maria.silva", "senhaForte", fielId, null, true);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        UsuarioEntity saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("maria.silva");
        assertThat(saved.isEnabled()).isFalse();

        verify(notificacaoUseCase).publicar(eq(adminId), eq("USUARIO_APROVACAO_PENDENTE"), any());
        verify(notificacaoUseCase).publicar(eq(parocoId), eq("USUARIO_APROVACAO_PENDENTE"), any());
    }

    @Test
    void deveFalharQuandoSecretariaTentarCriarUsuarioParaClerigo() {
        UUID clerigoId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("pe.joao")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("pe.joao", "senhaForte", null, clerigoId, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(
                        "Usuarios pendentes de aprovacao criados pela secretaria devem estar vinculados "
                                + "exclusivamente a um Fiel");

        verify(usuarioRepository, never()).save(any());
        verify(notificacaoUseCase, never()).publicar(any(), any(), any());
    }

    @Test
    void deveFalharComFielIdInexistente() {
        UUID fielId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("novo.usuario")).thenReturn(Optional.empty());
        when(fielRepository.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("novo.usuario", "senhaForte", fielId, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveFalharComClerigoIdInexistente() {
        UUID clerigoId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("novo.padre")).thenReturn(Optional.empty());
        when(clerigoRepository.findById(clerigoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("novo.padre", "senhaForte", null, clerigoId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoFielIdEClerigoIdInformadosJuntos() {
        UUID fielId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();
        when(usuarioRepository.findByUsernameIgnoreCase("novo.usuario")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("novo.usuario", "senhaForte", fielId, clerigoId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveFalharQuandoNemFielIdNemClerigoIdInformados() {
        when(usuarioRepository.findByUsernameIgnoreCase("novo.usuario")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("novo.usuario", "senhaForte", null, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(usuarioRepository, never()).save(any());
    }
}
