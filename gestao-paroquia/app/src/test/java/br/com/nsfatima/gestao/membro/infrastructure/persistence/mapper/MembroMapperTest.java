package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MembroMapperTest {

    private GrupoParoquialJpaRepository grupoParoquialRepository;
    private FielJpaRepository fielRepository;
    private MembroMapper mapper;

    @BeforeEach
    void setUp() {
        grupoParoquialRepository = mock(GrupoParoquialJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        mapper = new MembroMapper(grupoParoquialRepository, fielRepository);
    }

    @Test
    void deveResolverNomeEContatoDiretamenteDoFielVinculado() {
        UUID membroId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        MembroGrupoEntity entity = new MembroGrupoEntity();
        entity.setId(membroId);
        entity.setFielId(fielId);
        entity.setPapel("COORD_PASTORAL_LOCAL");
        entity.setAtivo(true);

        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setNome("João da Silva");
        fiel.setCpf("11122233396");
        fiel.setDataNascimento(LocalDate.of(1980, 1, 1));
        fiel.setEmail("joao@example.com");
        fiel.setTelefone("11911112222");
        fiel.setEndereco("Rua Z, 3");
        fiel.setVocacao(Vocacao.LEIGO);

        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fiel));

        MembroResponse response = mapper.toResponse(entity);

        assertThat(response.fielNome()).isEqualTo("João da Silva");
        assertThat(response.fielTelefone()).isEqualTo("11911112222");
        assertThat(response.fielEmail()).isEqualTo("joao@example.com");
    }
}
