package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class MembroOrganizacaoJpaRepositoryTest {

    @Autowired
    private MembroOrganizacaoJpaRepository repository;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Test
    void deveBuscarActiveVinculosPorUsuarioId() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = new UsuarioEntity();
        user.setId(userId);
        user.setUsername("membro_teste");
        user.setPasswordHash("hash");
        user.setFielId(FielTestFixtures.criarFiel(fielRepository));
        usuarioRepository.save(user);

        UUID orgId = UUID.randomUUID();

        MembroOrganizacaoEntity membro = new MembroOrganizacaoEntity();
        membro.setId(UUID.randomUUID());
        membro.setUsuarioId(userId);
        membro.setOrganizacaoId(orgId);
        membro.setTipoOrganizacao(TipoOrganizacao.PASTORAL);
        membro.setPapel("COORDENADOR");
        membro.setAtivo(true);
        repository.save(membro);

        MembroOrganizacaoEntity inativo = new MembroOrganizacaoEntity();
        inativo.setId(UUID.randomUUID());
        inativo.setUsuarioId(userId);
        inativo.setOrganizacaoId(orgId);
        inativo.setTipoOrganizacao(TipoOrganizacao.MOVIMENTO);
        inativo.setPapel("MEMBRO");
        inativo.setAtivo(false);
        repository.save(inativo);

        List<MembroOrganizacaoEntity> results = repository.findByUsuarioIdAndAtivoTrue(userId);
        assertEquals(1, results.size());
        assertEquals(TipoOrganizacao.PASTORAL, results.getFirst().getTipoOrganizacao());
    }
}
