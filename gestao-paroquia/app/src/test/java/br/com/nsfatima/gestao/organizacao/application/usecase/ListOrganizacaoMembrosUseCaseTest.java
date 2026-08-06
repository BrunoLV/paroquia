package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialMembroResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ListGrupoParoquialMembrosUseCaseTest {

    @Mock
    private MembroGrupoJpaRepository vinculoRepository;
    @Mock
    private FielJpaRepository fielRepository;
    @Mock
    private GrupoParoquialJpaRepository grupoParoquialRepository;

    private ListGrupoParoquialMembrosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListGrupoParoquialMembrosUseCase(
                vinculoRepository,
                fielRepository,
                grupoParoquialRepository
        );
    }

    @Test
    @DisplayName("Deve listar membros de uma organização resolvendo nomes a partir do Fiel")
    void deveListarGrupoParoquialMembrosResolvendoNomeDoFiel() {
        UUID orgId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        MembroGrupoEntity memberEntity = new MembroGrupoEntity();
        memberEntity.setId(UUID.randomUUID());
        memberEntity.setGrupoParoquialId(orgId);
        memberEntity.setFielId(fielId);
        memberEntity.setTipoGrupo(TipoGrupo.PASTORAL);
        memberEntity.setPapel("COORD_PASTORAL_LOCAL");
        memberEntity.setAtivo(true);

        FielEntity fielEntity = new FielEntity();
        fielEntity.setId(fielId);
        fielEntity.setNome("João da Silva");

        when(vinculoRepository.findByGrupoParoquialIdAndAtivoTrue(orgId)).thenReturn(List.of(memberEntity));
        when(vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(orgId)).thenReturn(Collections.emptyList());
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fielEntity));
        when(grupoParoquialRepository.findById(orgId)).thenReturn(Optional.empty());

        List<GrupoParoquialMembroResponse> response = useCase.execute(orgId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().fielNome()).isEqualTo("João da Silva");
        assertThat(response.getFirst().papel()).isEqualTo("COORD_PASTORAL_LOCAL");
    }
}
