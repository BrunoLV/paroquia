package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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
    private OrganizacaoJpaRepository organizacaoRepository;
    private FielJpaRepository fielRepository;
    private ListUsuariosUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        useCase = new ListUsuariosUseCase(usuarioRepository, organizacaoRepository, fielRepository);
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

        List<UsuarioResponse> responses = useCase.execute();

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().nome()).isEqualTo("Carlos Eduardo");
        assertThat(responses.getFirst().telefone()).isEqualTo("11933332222");
        assertThat(responses.getFirst().dataNascimento()).isEqualTo(LocalDate.of(1978, 9, 3));
    }
}
