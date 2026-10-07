package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Extensão de consulta de {@code OrganizacaoJpaRepository}, segregada num contrato próprio (padrão de
 * segregação de repositórios usado no contexto {@code calendario}) para manter consultas específicas
 * separadas do CRUD genérico de {@link org.springframework.data.jpa.repository.JpaRepository}.
 */
@NoRepositoryBean
public interface OrganizacaoConsultaJpaRepository {
    /**
     * Busca uma organização pelo nome, ignorando maiúsculas/minúsculas — usado para resolver referências a
     * organizações informadas por nome (ex.: ao sincronizar coordenador/vice-coordenador por texto livre).
     *
     * @param nome nome da organização, sem distinção de caixa
     * @return a organização encontrada, se houver
     */
    Optional<OrganizacaoEntity> findByNomeIgnoreCase(String nome);

    /**
     * Lista os grupos ativos de um conjunto de tipos, usado pelo índice de retenção de pastorais
     * do dashboard (tipicamente {@code PASTORAL}, {@code MOVIMENTO}, {@code MINISTERIO}).
     *
     * @param tipos tipos de grupo a incluir
     * @return grupos ativos dos tipos informados
     */
    List<OrganizacaoEntity> findByTipoInAndAtivoTrue(Collection<TipoOrganizacao> tipos);
}
