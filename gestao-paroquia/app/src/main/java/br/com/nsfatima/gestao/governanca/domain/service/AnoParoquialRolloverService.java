package br.com.nsfatima.gestao.governanca.domain.service;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Orquestra a transição automática entre anos paroquiais: quando um ano é dado como {@code CONCLUIDO} (seja pelo
 * job agendado de virada de ano, seja por ação manual via {@code AnoParoquialController}), provisiona
 * automaticamente o próximo ano civil em {@code EM_PLANEJAMENTO} caso ele ainda não exista. Isso garante que a
 * Matriz e as Comunidades sempre tenham um ano paroquial aberto para novo planejamento assim que o anterior se
 * encerra, sem exigir criação manual.
 */
@Service
public class AnoParoquialRolloverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnoParoquialRolloverService.class);
    private final AnoParoquialJpaRepository repository;

    public AnoParoquialRolloverService(AnoParoquialJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Recupera o registro de governança de um ano paroquial específico.
     *
     * @param ano ano civil desejado
     * @return o {@link AnoParoquialEntity} correspondente, se existir
     */
    public Optional<AnoParoquialEntity> get(Integer ano) {
        return repository.findByAno(ano);
    }

    /**
     * Tarefa agendada para rollover do ano paroquial.
     * Executa automaticamente em 31 de dezembro às 23:59:59.
     */
    @Scheduled(cron = "59 59 23 31 12 *")
    @Transactional
    public void rolloverYearAutomatically() {
        int currentYear = LocalDate.now().getYear();
        LOGGER.info("Iniciando rollover automático do Ano Paroquial para o ano {}", currentYear);
        concludeYear(currentYear);
    }

    /**
     * Conclui um dado ano e cria automaticamente o próximo em EM_PLANEJAMENTO caso não exista.
     */
    @Transactional
    public void concludeYear(int year) {
        AnoParoquialEntity entity = repository.findByAno(year)
                .orElseGet(() -> {
                    AnoParoquialEntity newEntity = new AnoParoquialEntity();
                    newEntity.setAno(year);
                    return newEntity;
                });

        if (AnoParoquialStatus.CONCLUIDO.equals(entity.getStatus())) {
            LOGGER.info("Ano Paroquial {} já está CONCLUIDO.", year);
            return;
        }

        if (repository.existsByAnoLessThanAndStatusNot(year, AnoParoquialStatus.CONCLUIDO)) {
            throw new IllegalArgumentException(
                    "Existe um ano anterior não concluído. O ano " + year + " não pode ser concluído.");
        }

        entity.setStatus(AnoParoquialStatus.CONCLUIDO);
        repository.save(entity);

        int nextYear = year + 1;
        if (repository.findByAno(nextYear).isEmpty()) {
            AnoParoquialEntity proximaEntidade = new AnoParoquialEntity();
            proximaEntidade.setAno(nextYear);
            proximaEntidade.setStatus(AnoParoquialStatus.EM_PLANEJAMENTO);
            repository.save(proximaEntidade);
            LOGGER.info(
                    "Rollover do Ano Paroquial concluído: ano {} definido como CONCLUIDO, ano {} inicializado como "
                            + "EM_PLANEJAMENTO",
                    year, nextYear);

        } else {
            LOGGER.info(
                    "Rollover do Ano Paroquial concluído: ano {} definido como CONCLUIDO. O ano {} já estava "
                            + "inicializado.",
                    year, nextYear);

        }
    }
}
