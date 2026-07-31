package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.PatrimonioNumeracaoAnualJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NumeroTombamentoGenerator {

    private final PatrimonioNumeracaoAnualJpaRepository repository;
    private final PatrimonioNumeracaoAnualSeeder seeder;

    public NumeroTombamentoGenerator(PatrimonioNumeracaoAnualJpaRepository repository,
                                      PatrimonioNumeracaoAnualSeeder seeder) {
        this.repository = repository;
        this.seeder = seeder;
    }

    @Transactional
    public String generate(int ano) {
        seeder.ensureYearRowExists(ano);

        PatrimonioNumeracaoAnualEntity contador = repository.findById(ano)
                .orElseThrow(() -> new IllegalStateException(
                        "Contador de tombamento do ano " + ano + " não encontrado"));

        int novoNumero = contador.getUltimoNumero() + 1;
        contador.setUltimoNumero(novoNumero);
        repository.save(contador);

        return "PAT-" + ano + "-" + String.format("%04d", novoNumero);
    }
}
