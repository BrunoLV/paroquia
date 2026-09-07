package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListUsuariosUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private GrupoParoquialJpaRepository grupoParoquialRepository;
    private FielJpaRepository fielRepository;
    private ClerigoJpaRepository clerigoRepository;
    private ListUsuariosUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        grupoParoquialRepository = mock(GrupoParoquialJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        clerigoRepository = mock(ClerigoJpaRepository.class);
        useCase = new ListUsuariosUseCase(
                usuarioRepository,
                grupoParoquialRepository,
                fielRepository,
                clerigoRepository);
    }

    @Test
    void deveResolverDadosCadastraisDeCadaUsuarioAPartirDoFielVinculado() {
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setUsername("usuario.teste");
        usuario.setEnabled(true);
        usuario.setFielId(fielId);

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("Carlos Eduardo");
        fiel.setCpf("32132132100");
        fiel.setDataNascimento(LocalDate.of(1978, 9, 3));
        fiel.setEmail("carlos@example.com");
        fiel.setTelefone("11933332222");
        fiel.setEndereco("Rua Y, 2");
        fiel.setVocacao(Vocacao.LEIGO);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(fielRepository.findAllById(List.of(fielId))).thenReturn(List.of(fiel));
        when(clerigoRepository.findAllById(List.of())).thenReturn(List.of());

        List<UsuarioResponse> responses = useCase.execute();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().nome()).isEqualTo("Carlos Eduardo");
        assertThat(responses.getFirst().telefone()).isEqualTo("11933332222");
        assertThat(responses.getFirst().dataNascimento()).isEqualTo(LocalDate.of(1978, 9, 3));
    }

    @Test
    void deveResolverDadosCadastraisDeClerigoVinculado() {
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

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(fielRepository.findAllById(List.of())).thenReturn(List.of());
        when(clerigoRepository.findAllById(List.of(clerigoId))).thenReturn(List.of(clerigo));

        List<UsuarioResponse> responses = useCase.execute();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().nome()).isEqualTo("Padre Pedro");
        assertThat(responses.getFirst().telefone()).isEqualTo("11988887777");
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());
        when(fielRepository.findAllById(List.of())).thenReturn(List.of());
        when(clerigoRepository.findAllById(List.of())).thenReturn(List.of());

        List<UsuarioResponse> responses = useCase.execute();

        assertThat(responses).isEmpty();
    }

    @Test
    void deveRetornarCamposNulosQuandoFielNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setUsername("orfao.usuario");
        usuario.setEnabled(true);
        usuario.setFielId(fielId);

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(fielRepository.findAllById(List.of(fielId))).thenReturn(List.of());
        when(clerigoRepository.findAllById(List.of())).thenReturn(List.of());

        List<UsuarioResponse> responses = useCase.execute();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().nome()).isNull();
    }
}
