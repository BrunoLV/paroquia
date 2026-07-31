package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.PatrimonioNumeracaoAnualJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the yearly counter row in its own transaction so a unique-constraint
 * collision under concurrent first-use-of-year never aborts the caller's transaction.
 */
@Service
public class PatrimonioNumeracaoAnualSeeder {

    private final PatrimonioNumeracaoAnualJpaRepository repository;

    public PatrimonioNumeracaoAnualSeeder(PatrimonioNumeracaoAnualJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureYearRowExists(int ano) {
        if (repository.existsById(ano)) {
            return;
        }
        try {
            PatrimonioNumeracaoAnualEntity entity = new PatrimonioNumeracaoAnualEntity();
            entity.setAno(ano);
            entity.setUltimoNumero(0);
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            // another transaction concurrently seeded the same year — row already exists.
        }
    }
}
