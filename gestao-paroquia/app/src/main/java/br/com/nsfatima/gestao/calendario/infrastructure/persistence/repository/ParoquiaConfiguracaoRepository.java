package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório de persistência de {@link ParoquiaConfiguracaoEntity}, responsável pelo CRUD dos
 * parâmetros de configuração operacional da paróquia (ex.: janelas/limites usados pelo contexto
 * de calendário) que não se enquadram em nenhum outro agregado específico.
 */
@Repository
public interface ParoquiaConfiguracaoRepository extends JpaRepository<ParoquiaConfiguracaoEntity, UUID> {
}
