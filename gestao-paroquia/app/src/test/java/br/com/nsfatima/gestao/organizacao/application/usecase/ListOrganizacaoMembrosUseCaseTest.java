package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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
class ListOrganizacaoMembrosUseCaseTest {

    @Mock
    private MembroGrupoJpaRepository vinculoRepository;
    @Mock
    private FielJpaRepository fielRepository;
    @Mock
    private OrganizacaoJpaRepository organizacaoRepository;

    private ListOrganizacaoMembrosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListOrganizacaoMembrosUseCase(
                vinculoRepository,
                fielRepository,
                organizacaoRepository
        );
    }

    @Test
    @DisplayName("Deve listar membros de uma organização resolvendo nomes a partir do Fiel")
    void deveListarOrganizacaoMembrosResolvendoNomeDoFiel() {
        UUID orgId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();

        MembroGrupoEntity memberEntity = new MembroGrupoEntity();
        memberEntity.setId(UUID.randomUUID());
        memberEntity.setOrganizacaoId(orgId);
        memberEntity.setFielId(fielId);
        memberEntity.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        memberEntity.setPapel("COORD_PASTORAL_LOCAL");
        memberEntity.setAtivo(true);

        FielEntity fielEntity = new FielEntity();
        fielEntity.setId(fielId);
        fielEntity.setNome("João da Silva");

        when(vinculoRepository.findByOrganizacaoIdAndAtivoTrue(orgId)).thenReturn(List.of(memberEntity));
        when(vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(orgId)).thenReturn(Collections.emptyList());
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(fielEntity));
        when(organizacaoRepository.findById(orgId)).thenReturn(Optional.empty());

        List<OrganizacaoMembroResponse> response = useCase.execute(orgId);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().fielNome()).isEqualTo("João da Silva");
        assertThat(response.getFirst().papel()).isEqualTo("COORD_PASTORAL_LOCAL");
    }
}
