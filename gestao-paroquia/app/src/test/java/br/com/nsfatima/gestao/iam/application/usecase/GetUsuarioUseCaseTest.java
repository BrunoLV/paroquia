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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private OrganizacaoJpaRepository organizacaoRepository;
    private FielJpaRepository fielRepository;
    private GetUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        useCase = new GetUsuarioUseCase(usuarioRepository, organizacaoRepository, fielRepository);
    }

    @Test
    void deveResolverDadosCadastraisAPartirDoFielVinculado() {
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
        fiel.setEmail("maria@example.com");
        fiel.setTelefone("11955554444");
        fiel.setEndereco("Rua X, 1");
        fiel.setVocacao(Vocacao.LEIGO);
        fiel.setBatizado(true);
        fiel.setPrimeiraComunhao(true);
        fiel.setCrismado(false);
        fiel.setCasadoIgrejaCatolica(false);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        UsuarioResponse response = useCase.execute(usuarioId);

        assertThat(response.nome()).isEqualTo("Maria Aparecida");
        assertThat(response.telefone()).isEqualTo("11955554444");
        assertThat(response.dataNascimento()).isEqualTo(LocalDate.of(1985, 4, 12));
        assertThat(response.batizado()).isTrue();
        assertThat(response.primeiraComunhao()).isTrue();
        assertThat(response.crismado()).isFalse();
        assertThat(response.casadoIgrejaCatolica()).isFalse();
    }
}
