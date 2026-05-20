package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MembroJpaRepositoryTest {

    @Autowired
    private MembroJpaRepository repository;

    @Test
    @DisplayName("Deve salvar e buscar um membro por ID")
    void deveSalvarEBuscarMembro() {
        UUID id = UUID.randomUUID();
        MembroEntity membro = new MembroEntity();
        membro.setId(id);
        membro.setNomeCompleto("João da Silva");
        membro.setDataNascimento(LocalDate.of(1980, 5, 15));
        membro.setAtivo(true);

        repository.save(membro);

        MembroEntity found = repository.findById(id).orElseThrow();
        assertThat(found.getNomeCompleto()).isEqualTo("João da Silva");
        assertThat(found.getDataNascimento()).isEqualTo(LocalDate.of(1980, 5, 15));
    }

    @Test
    @DisplayName("Deve filtrar membros por nome")
    void deveFiltrarPorNome() {
        createMembro("Alice Oliveira");
        createMembro("Bob Silva");
        createMembro("Carlos Oliveira");

        Page<MembroEntity> result = repository.findByFiltros("Oliveira", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(MembroEntity::getNomeCompleto)
                .containsExactlyInAnyOrder("Alice Oliveira", "Carlos Oliveira");
    }

    @Test
    @DisplayName("Deve filtrar membros por status ativo")
    void deveFiltrarPorAtivo() {
        MembroEntity m1 = createMembro("Ativo");
        MembroEntity m2 = createMembro("Inativo");
        m2.setAtivo(false);
        repository.save(m2);

        Page<MembroEntity> ativos = repository.findByFiltros(null, true, PageRequest.of(0, 10));
        Page<MembroEntity> inativos = repository.findByFiltros(null, false, PageRequest.of(0, 10));

        assertThat(ativos.getContent()).hasSize(1);
        assertThat(ativos.getContent().getFirst().getNomeCompleto()).isEqualTo("Ativo");
        assertThat(inativos.getContent()).hasSize(1);
        assertThat(inativos.getContent().getFirst().getNomeCompleto()).isEqualTo("Inativo");
    }

    private MembroEntity createMembro(String nome) {
        MembroEntity membro = new MembroEntity();
        membro.setId(UUID.randomUUID());
        membro.setNomeCompleto(nome);
        membro.setAtivo(true);
        return repository.save(membro);
    }
}
