package br.com.nsfatima.gestao.fiel.support;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cria um {@link FielEntity} válido e persistido, para testes que precisam de um {@code fielId} real.
 */
public final class FielTestFixtures {

    private FielTestFixtures() {
    }

    public static UUID criarFiel(FielJpaRepository repository) {
        return criarFiel(repository, "Fiel de Teste");
    }

    public static UUID criarFiel(FielJpaRepository repository, String nome) {
        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        fiel.setNome(nome);
        fiel.setCpf(UUID.randomUUID().toString().replace("-", "").substring(0, 11));
        fiel.setDataNascimento(LocalDate.of(1990, 1, 1));
        fiel.setEmail("fiel.teste." + UUID.randomUUID() + "@example.com");
        fiel.setTelefone("11999999999");
        fiel.setEnderecoLogradouro("Rua de Teste");
        fiel.setEnderecoNumero("1");
        fiel.setEnderecoBairro("Centro");
        fiel.setEnderecoCep("01000-000");
        fiel.setEnderecoCidade("São Paulo");
        fiel.setEnderecoEstado("SP");
        fiel.setVocacao(Vocacao.LEIGO);
        fiel.setAtivo(true);
        fiel.setDataBatismo(LocalDate.of(1990, 6, 1));
        fiel.setParoquiaBatismo("Matriz");
        fiel.setCriadoEmUtc(Instant.now());
        fiel.setAtualizadoEmUtc(Instant.now());
        fiel.setCriadoPor("teste");
        fiel.setAtualizadoPor("teste");
        return repository.saveAndFlush(fiel).getId();
    }
}
