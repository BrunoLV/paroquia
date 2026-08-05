package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class FielJpaRepositoryTest {

    @Autowired
    private FielJpaRepository repository;

    private FielEntity criarFiel(String cpf) {
        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        fiel.setNome("Maria da Silva");
        fiel.setCpf(cpf);
        fiel.setDataNascimento(LocalDate.of(1990, 5, 20));
        fiel.setEmail("maria@example.com");
        fiel.setTelefone("11999999999");
        fiel.setEndereco("Rua das Flores, 123");
        fiel.setVocacao(Vocacao.LEIGO);
        fiel.setAtivo(true);
        fiel.setCriadoEmUtc(Instant.now());
        fiel.setAtualizadoEmUtc(Instant.now());
        fiel.setCriadoPor("teste");
        fiel.setAtualizadoPor("teste");
        return fiel;
    }

    @Test
    void devePersistirFielValido() {
        FielEntity fiel = criarFiel("11122233344");

        FielEntity saved = repository.saveAndFlush(fiel);

        Optional<FielEntity> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getNome()).isEqualTo("Maria da Silva");
        assertThat(found.get().getCpf()).isEqualTo("11122233344");
        assertThat(found.get().getVocacao()).isEqualTo(Vocacao.LEIGO);
    }

    @Test
    void deveFalharAoPersistirCpfDuplicado() {
        repository.saveAndFlush(criarFiel("55566677788"));

        FielEntity duplicado = criarFiel("55566677788");

        assertThatThrownBy(() -> repository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
