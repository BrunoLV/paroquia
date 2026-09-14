package br.com.nsfatima.gestao.patrimonio.application.service;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.PatrimonioNumeracaoAnualEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.PatrimonioNumeracaoAnualJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Semeia a linha de contador anual em sua própria transação, para que uma colisão de
 * unique-constraint no primeiro uso concorrente do ano nunca aborte a transação de quem chamou.
 */
@Service
public class PatrimonioNumeracaoAnualSeeder {

    private final PatrimonioNumeracaoAnualJpaRepository repository;

    public PatrimonioNumeracaoAnualSeeder(PatrimonioNumeracaoAnualJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void garantirLinhaDoAno(int ano) {
        if (repository.existsById(ano)) {
            return;
        }
        try {
            PatrimonioNumeracaoAnualEntity entity = new PatrimonioNumeracaoAnualEntity();
            entity.setAno(ano);
            entity.setUltimoNumero(0);
            repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            // outra transação semeou o mesmo ano concorrentemente — a linha já existe.
        }
    }
}
