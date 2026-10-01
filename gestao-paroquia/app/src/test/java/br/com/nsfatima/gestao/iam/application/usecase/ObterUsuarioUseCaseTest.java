package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObterUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private FielJpaRepository fielRepository;
    private ClerigoJpaRepository clerigoRepository;
    private ObterUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        clerigoRepository = mock(ClerigoJpaRepository.class);
        useCase = new ObterUsuarioUseCase(
                usuarioRepository, fielRepository, clerigoRepository);
    }

    @Test
    void deveResolverNomeAPartirDoFielVinculado() {
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setUsername("usuario.teste");
        usuario.setEnabled(true);
        usuario.setFielId(fielId);

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Maria Aparecida");
        fiel.setCpf("12312312312");
        fiel.setDataNascimento(LocalDate.of(1985, 4, 12));
        fiel.setTelefone("11955554444");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        UsuarioResponse response = useCase.execute(usuarioId);

        assertThat(response.nome()).isEqualTo("Maria Aparecida");
        assertThat(response.fielId()).isEqualTo(fielId);
        assertThat(response.username()).isEqualTo("usuario.teste");
    }

    @Test
    void deveResolverNomeAPartirDoClerigoVinculado() {
        UUID usuarioId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setUsername("padre.pedro");
        usuario.setEnabled(true);
        usuario.setClerigoId(clerigoId);

        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setId(clerigoId);
        clerigo.setNomeCivil("Pedro de Alcantara");
        clerigo.setNomeEclesiastico("Padre Pedro");
        clerigo.setCelular("11988887777");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(clerigoRepository.findById(clerigoId)).thenReturn(Optional.of(clerigo));

        UsuarioResponse response = useCase.execute(usuarioId);

        assertThat(response.nome()).isEqualTo("Padre Pedro");
        assertThat(response.clerigoId()).isEqualTo(clerigoId);
    }

    @Test
    void deveRetornarNomeNuloQuandoFielNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setUsername("orfao.usuario");
        usuario.setEnabled(true);
        usuario.setFielId(fielId);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(fielRepository.findById(fielId)).thenReturn(Optional.empty());

        UsuarioResponse response = useCase.execute(usuarioId);

        assertThat(response.nome()).isNull();
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException.class,
                () -> useCase.execute(usuarioId));
    }
}
