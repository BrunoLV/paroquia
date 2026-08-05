package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
@ActiveProfiles("test")
class MembroJpaRepositoryTest {

    @Autowired
    private MembroJpaRepository repository;

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Test
    @DisplayName("Deve salvar e buscar um membro por ID")
    void deveSalvarEBuscarMembro() {
        UsuarioEntity usuario = createUsuario();
        UUID id = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        MembroEntity membro = new MembroEntity();
        membro.setId(id);
        membro.setUsuarioId(usuario.getId());
        membro.setOrganizacaoId(organizacaoId);
        membro.setPapel("COORDENADOR");
        membro.setAtivo(true);

        repository.save(membro);

        MembroEntity found = repository.findById(id).orElseThrow();
        assertThat(found.getUsuarioId()).isEqualTo(usuario.getId());
        assertThat(found.getOrganizacaoId()).isEqualTo(organizacaoId);
        assertThat(found.getPapel()).isEqualTo("COORDENADOR");
    }

    @Test
    @DisplayName("Deve filtrar membros por organizacaoId")
    void deveFiltrarPorOrganizacaoId() {
        UUID org1 = UUID.randomUUID();
        UUID org2 = UUID.randomUUID();

        createMembro(createUsuario().getId(), org1);
        createMembro(createUsuario().getId(), org1);
        createMembro(createUsuario().getId(), org2);

        Page<MembroEntity> result = repository.findByFiltros(org1, null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Deve filtrar membros por status ativo")
    void deveFiltrarPorAtivo() {
        UUID orgId = UUID.randomUUID();
        MembroEntity m1 = createMembro(createUsuario().getId(), orgId);
        MembroEntity m2 = createMembro(createUsuario().getId(), orgId);
        m2.setAtivo(false);
        repository.save(m2);

        Page<MembroEntity> ativos = repository.findByFiltros(null, null, true, PageRequest.of(0, 10));
        Page<MembroEntity> inativos = repository.findByFiltros(null, null, false, PageRequest.of(0, 10));

        assertThat(ativos.getContent()).hasSize(1);
        assertThat(inativos.getContent()).hasSize(1);
    }

    private UsuarioEntity createUsuario() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setUsername("user_" + UUID.randomUUID() + "@test.com");
        usuario.setPasswordHash("hash");
        usuario.setFielId(FielTestFixtures.criarFiel(fielRepository));
        return usuarioRepository.save(usuario);
    }

    private MembroEntity createMembro(UUID usuarioId, UUID organizacaoId) {
        MembroEntity membro = new MembroEntity();
        membro.setId(UUID.randomUUID());
        membro.setUsuarioId(usuarioId);
        membro.setOrganizacaoId(organizacaoId);
        membro.setPapel("MEMBRO");
        membro.setAtivo(true);
        return repository.save(membro);
    }
}
