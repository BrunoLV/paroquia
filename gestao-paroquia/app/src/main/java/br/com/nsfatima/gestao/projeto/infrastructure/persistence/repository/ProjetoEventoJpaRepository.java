package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para a entidade {@code ProjetoEventoEntity} (o projeto em si). O nome reflete
 * a tabela {@code projetos_eventos}, não uma relação evento-projeto.
 */
public interface ProjetoEventoJpaRepository extends JpaRepository<ProjetoEventoEntity, UUID> {

    /**
     * Busca projetos em um determinado status cuja data de início já foi atingida (menor ou
     * igual à data informada), usado por {@code ProjetoAutoActivationScheduler} para localizar
     * projetos {@code ABERTO} prontos para ativação automática.
     *
     * @param status status do projeto a filtrar.
     * @param date    data de corte (inclusiva) para a data de início.
     * @return lista de projetos que atendem aos critérios.
     */
    List<ProjetoEventoEntity> findByStatusAndInicioUtcLessThanEqual(ProjetoStatus status, Instant date);
}

