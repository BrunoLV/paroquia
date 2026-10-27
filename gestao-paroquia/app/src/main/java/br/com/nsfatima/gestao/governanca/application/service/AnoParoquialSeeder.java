package br.com.nsfatima.gestao.governanca.application.service;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Garante que sempre exista um registro de Ano Paroquial ao subir a aplicação, provisionando o ano civil corrente
 * como {@code EM_PLANEJAMENTO} quando o banco está vazio (primeira execução/ambiente novo). Sem essa semente, a
 * trava de governança do calendário anual (ver {@link AnoParoquialRolloverService}) não teria um ano de partida
 * para operar.
 */
@Service
public class AnoParoquialSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnoParoquialSeeder.class);
    private final AnoParoquialJpaRepository repository;
    private final RelogioNegocio relogio;

    public AnoParoquialSeeder(AnoParoquialJpaRepository repository, RelogioNegocio relogio) {
        this.repository = repository;
        this.relogio = relogio;
    }

    /**
     * Reage à subida completa do contexto Spring para acionar o provisionamento inicial do Ano Paroquial.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        semearSeVazio();
    }

    /**
     * Cria o Ano Paroquial do ano civil corrente com status {@code EM_PLANEJAMENTO} caso a tabela ainda esteja
     * vazia; não faz nada se já existir qualquer ano registrado (idempotente após a primeira execução).
     */
    @Transactional
    public void semearSeVazio() {
        if (repository.count() == 0) {
            int anoCorrente = relogio.hoje().getYear();
            AnoParoquialEntity entity = new AnoParoquialEntity();
            entity.setAno(anoCorrente);
            entity.setStatus(AnoParoquialStatus.EM_PLANEJAMENTO);
            repository.save(entity);
            LOGGER.info("Ano Paroquial inicializado: ano {} semeado como EM_PLANEJAMENTO porque o banco estava vazio.",
                    anoCorrente);

        }
    }
}
