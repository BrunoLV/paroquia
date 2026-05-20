package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MembroOrganizacaoJpaRepositoryTest {

    @Autowired
    private MembroOrganizacaoJpaRepository repository;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Test
    void shouldFindActiveMembershipsByUserId() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity user = new UsuarioEntity();
        user.setId(userId);
        user.setUsername("membro_teste");
        user.setPasswordHash("hash");
        usuarioRepository.save(user);

        UUID orgId = UUID.randomUUID();

        MembroOrganizacaoEntity membro = new MembroOrganizacaoEntity();
        membro.setId(UUID.randomUUID());
        membro.setUsuarioId(userId);
        membro.setOrganizacaoId(orgId);
        membro.setTipoOrganizacao("CATEQUESE");
        membro.setPapel("COORDENADOR");
        membro.setAtivo(true);
        repository.save(membro);

        MembroOrganizacaoEntity inativo = new MembroOrganizacaoEntity();
        inativo.setId(UUID.randomUUID());
        inativo.setUsuarioId(userId);
        inativo.setOrganizacaoId(orgId);
        inativo.setTipoOrganizacao("LITURGIA");
        inativo.setPapel("MEMBRO");
        inativo.setAtivo(false);
        repository.save(inativo);

        List<MembroOrganizacaoEntity> results = repository.findByUsuarioIdAndAtivoTrue(userId);
        assertEquals(1, results.size());
        assertEquals("CATEQUESE", results.getFirst().getTipoOrganizacao());
    }
}
