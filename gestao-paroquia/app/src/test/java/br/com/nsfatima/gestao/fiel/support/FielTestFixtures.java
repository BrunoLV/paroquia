package br.com.nsfatima.gestao.fiel.support;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Cria um {@link FielEntity} válido e persistido, para testes que precisam de um {@code fielId} real
 * (obrigatório em {@code UsuarioEntity} desde a track {@code usuario_fiel_decoupling}).
 */
public final class FielTestFixtures {

    private FielTestFixtures() {
    }

    /**
     * Persiste um Fiel de teste com um CPF único gerado a partir de um UUID aleatório, e retorna seu id.
     *
     * @param repository repositório a usar para persistir o Fiel
     * @return o id do Fiel persistido
     */
    public static UUID criarFiel(FielJpaRepository repository) {
        return criarFiel(repository, "Fiel de Teste");
    }

    /**
     * Persiste um Fiel de teste com o nome informado e um CPF único gerado a partir de um UUID aleatório, e
     * retorna seu id. Útil quando o teste faz asserções sobre o nome de exibição resolvido via Fiel.
     *
     * @param repository repositório a usar para persistir o Fiel
     * @param nome       nome a atribuir ao Fiel
     * @return o id do Fiel persistido
     */
    public static UUID criarFiel(FielJpaRepository repository, String nome) {
        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        fiel.setNome(nome);
        fiel.setCpf(UUID.randomUUID().toString().replace("-", "").substring(0, 11));
        fiel.setDataNascimento(LocalDate.of(1990, 1, 1));
        fiel.setEmail("fiel.teste." + UUID.randomUUID() + "@example.com");
        fiel.setTelefone("11999999999");
        fiel.setEndereco("Rua de Teste, 1");
        fiel.setVocacao(Vocacao.LEIGO);
        fiel.setAtivo(true);
        fiel.setBatizado(true);
        fiel.setPrimeiraComunhao(true);
        fiel.setCrismado(true);
        fiel.setCasadoIgrejaCatolica(false);
        fiel.setCriadoEmUtc(Instant.now());
        fiel.setAtualizadoEmUtc(Instant.now());
        fiel.setCriadoPor("teste");
        fiel.setAtualizadoPor("teste");
        return repository.saveAndFlush(fiel).getId();
    }
}
