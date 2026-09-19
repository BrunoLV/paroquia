package br.com.nsfatima.gestao.patrimonio.application.service;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.PatrimonioNumeracaoAnualJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera o número de tombamento sequencial e único de um ativo patrimonial no formato
 * {@code PAT-<ano>-<sequencial>}, com o contador reiniciando a cada ano (a linha do contador anual
 * é criada sob demanda via {@link PatrimonioNumeracaoAnualSeeder}).
 */
@Service
public class GeradorNumeroTombamento {

    private final PatrimonioNumeracaoAnualJpaRepository repository;
    private final PatrimonioNumeracaoAnualSeeder seeder;

    public GeradorNumeroTombamento(PatrimonioNumeracaoAnualJpaRepository repository,
                                   PatrimonioNumeracaoAnualSeeder seeder) {
        this.repository = repository;
        this.seeder = seeder;
    }

    /**
     * Incrementa e retorna o próximo número de tombamento do ano informado, garantindo primeiro
     * que a linha do contador anual exista.
     *
     * @param ano ano de referência da numeração (normalmente o ano corrente)
     * @return número de tombamento no formato {@code PAT-<ano>-<sequencial com 4 dígitos>}
     * @throws IllegalStateException se o contador do ano não puder ser encontrado após o seeding
     */
    @Transactional
    public String gerar(int ano) {
        seeder.garantirLinhaDoAno(ano);

        PatrimonioNumeracaoAnualEntity contador = repository.findById(ano)
                .orElseThrow(() -> new IllegalStateException(
                        "Contador de tombamento do ano " + ano + " não encontrado"));

        int novoNumero = contador.getUltimoNumero() + 1;
        contador.setUltimoNumero(novoNumero);
        repository.save(contador);

        return "PAT-" + ano + "-" + String.format("%04d", novoNumero);
    }
}
