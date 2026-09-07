package br.com.nsfatima.gestao.fiel.support;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Utilitário de apoio a testes com fixture de fiel. */
public final class FielTestFixtures {

    private FielTestFixtures() {
    }

    public static UUID criarFiel(FielJpaRepository repository) {
        return criarFiel(repository, "Fiel de Teste");
    }

    public static UUID criarFiel(FielJpaRepository repository, String nome) {
        var fiel = new FielEntity();
        UUID id = UUID.randomUUID();
        fiel.setId(id);
        fiel.setNome(nome);
        fiel.setCpf(String.format("%011d", Math.floorMod(id.getLeastSignificantBits(), 100_000_000_000L)));
        fiel.setDataNascimento(LocalDate.of(1990, 1, 1));
        fiel.setEmail("teste-" + id + "@example.com");
        fiel.setCelular("11999999999");
        fiel.setAtivo(true);
        fiel.setCriadoEmUtc(Instant.now());
        fiel.setAtualizadoEmUtc(Instant.now());
        fiel.setCriadoPor("teste");
        fiel.setAtualizadoPor("teste");
        return repository.save(fiel).getId();
    }
}
