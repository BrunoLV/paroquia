package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.support.infrastructure.configuration.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class MembroGrupoJpaRepositoryTest {

    @Autowired
    private MembroGrupoJpaRepository repository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Test
    @DisplayName("Deve salvar e buscar um vínculo MembroGrupo por ID")
    void deveSalvarEBuscarMembroGrupo() {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository);
        UUID organizacaoId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        MembroGrupoEntity membro = new MembroGrupoEntity();
        membro.setId(id);
        membro.setFielId(fielId);
        membro.setOrganizacaoId(organizacaoId);
        membro.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        membro.setPapel("MEMBRO_PASTORAL");
        membro.setAtivo(true);

        repository.save(membro);

        MembroGrupoEntity found = repository.findById(id).orElseThrow();
        assertThat(found.getFielId()).isEqualTo(fielId);
        assertThat(found.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(found.getPapel()).isEqualTo("MEMBRO_PASTORAL");
        assertThat(found.getCriadoEmUtc()).isNotNull();
        assertThat(found.getAtualizadoEmUtc()).isNotNull();
    }

    @Test
    @DisplayName("Deve falhar ao persistir sem fielId pois agora é obrigatório")
    void deveFalharAoPersistirSemFielIdPoisAgoraEhObrigatorio() {
        MembroGrupoEntity membro = new MembroGrupoEntity();
        membro.setId(UUID.randomUUID());
        membro.setOrganizacaoId(UUID.randomUUID());
        membro.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        membro.setPapel("MEMBRO_PASTORAL");
        membro.setAtivo(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class, () -> repository.saveAndFlush(membro));
    }

    @Test
    @DisplayName("Deve filtrar por organizacaoId e por fielId")
    void deveFiltrarPorOrganizacaoIdEFielId() {
        UUID fiel1 = FielTestFixtures.criarFiel(fielRepository, "Fiel Um");
        UUID fiel2 = FielTestFixtures.criarFiel(fielRepository, "Fiel Dois");
        UUID org1 = UUID.randomUUID();
        UUID org2 = UUID.randomUUID();

        salvar(fiel1, org1);
        salvar(fiel2, org1);
        salvar(fiel1, org2);

        Page<MembroGrupoEntity> porOrg = repository.findByFiltros(org1, null, null, PageRequest.of(0, 10));
        Page<MembroGrupoEntity> porFiel = repository.findByFiltros(null, fiel1, null, PageRequest.of(0, 10));

        assertThat(porOrg.getContent()).hasSize(2);
        assertThat(porFiel.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Deve buscar vínculos ativos por fielId")
    void deveBuscarVinculosAtivosPorFielId() {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository);
        UUID orgId = UUID.randomUUID();

        MembroGrupoEntity ativo = new MembroGrupoEntity();
        ativo.setId(UUID.randomUUID());
        ativo.setFielId(fielId);
        ativo.setOrganizacaoId(orgId);
        ativo.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        ativo.setPapel("COORD_PASTORAL_LOCAL");
        ativo.setAtivo(true);
        repository.save(ativo);

        MembroGrupoEntity inativo = new MembroGrupoEntity();
        inativo.setId(UUID.randomUUID());
        inativo.setFielId(fielId);
        inativo.setOrganizacaoId(orgId);
        inativo.setTipoOrganizacao(TipoOrganizacao.MOVIMENTO);
        inativo.setPapel("MEMBRO_MOVIMENTO");
        inativo.setAtivo(false);
        repository.save(inativo);

        var results = repository.findByFielIdAndAtivoTrue(fielId);
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getTipoOrganizacao()).isEqualTo(TipoOrganizacao.PASTORAL);
    }

    private void salvar(UUID fielId, UUID organizacaoId) {
        MembroGrupoEntity membro = new MembroGrupoEntity();
        membro.setId(UUID.randomUUID());
        membro.setFielId(fielId);
        membro.setOrganizacaoId(organizacaoId);
        membro.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        membro.setPapel("MEMBRO_PASTORAL");
        membro.setAtivo(true);
        repository.save(membro);
    }
}
