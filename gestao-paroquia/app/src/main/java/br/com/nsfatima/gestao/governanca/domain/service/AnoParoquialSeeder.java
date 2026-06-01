package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AnoParoquialSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnoParoquialSeeder.class);
    private final AnoParoquialJpaRepository repository;

    public AnoParoquialSeeder(AnoParoquialJpaRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        seedIfEmpty();
    }

    @Transactional
    public void seedIfEmpty() {
        if (repository.count() == 0) {
            int currentYear = LocalDate.now().getYear();
            AnoParoquialEntity entity = new AnoParoquialEntity();
            entity.setAno(currentYear);
            entity.setStatus(AnoParoquialStatus.EM_PLANEJAMENTO.name());
            repository.save(entity);
            LOGGER.info("Parish Year initialized: Seeded year {} as EM_PLANEJAMENTO because the database was empty.", currentYear);
        }
    }
}
