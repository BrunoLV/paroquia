package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Extensão de consulta de {@code GrupoParoquialJpaRepository}, segregada num contrato próprio (padrão de
 * segregação de repositórios usado no contexto {@code calendario}) para manter consultas específicas
 * separadas do CRUD genérico de {@link org.springframework.data.jpa.repository.JpaRepository}.
 */
@NoRepositoryBean
public interface GrupoParoquialConsultaJpaRepository {
    /**
     * Busca uma organização pelo nome, ignorando maiúsculas/minúsculas — usado para resolver referências a
     * organizações informadas por nome (ex.: ao sincronizar coordenador/vice-coordenador por texto livre).
     *
     * @param nome nome da organização, sem distinção de caixa
     * @return a organização encontrada, se houver
     */
    Optional<GrupoParoquialEntity> findByNomeIgnoreCase(String nome);

    /**
     * Lista os grupos ativos de um conjunto de tipos, usado pelo índice de retenção de pastorais
     * do dashboard (tipicamente {@code PASTORAL}, {@code MOVIMENTO}, {@code MINISTERIO}).
     *
     * @param tipos tipos de grupo a incluir
     * @return grupos ativos dos tipos informados
     */
    List<GrupoParoquialEntity> findByTipoInAndAtivoTrue(Collection<TipoGrupo> tipos);
}
