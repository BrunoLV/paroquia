package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MembroMapperTest {

    private UsuarioJpaRepository usuarioRepository;
    private OrganizacaoJpaRepository organizacaoRepository;
    private FielJpaRepository fielRepository;
    private MembroMapper mapper;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        mapper = new MembroMapper(usuarioRepository, organizacaoRepository, fielRepository);
    }

    @Test
    void deveResolverNomeETelefoneDoUsuarioAPartirDoFielVinculado() {
        UUID membroId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        MembroEntity entity = new MembroEntity();
        entity.setId(membroId);
        entity.setUsuarioId(usuarioId);
        entity.setPapel("COORDENADOR");
        entity.setAtivo(true);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setUsername("joao.silva");
        usuario.setFielId(fielId);

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("João da Silva");
        fiel.setCpf("11122233396");
        fiel.setDataNascimento(LocalDate.of(1980, 1, 1));
        fiel.setEmail("joao@example.com");
        fiel.setTelefone("11911112222");
        fiel.setEndereco("Rua Z, 3");
        fiel.setVocacao(Vocacao.LEIGO);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        MembroResponse response = mapper.toResponse(entity);

        assertThat(response.usuarioNome()).isEqualTo("João da Silva");
        assertThat(response.usuarioTelefone()).isEqualTo("11911112222");
        assertThat(response.usuarioEmail()).isEqualTo("joao.silva");
    }
}
