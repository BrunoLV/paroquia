package br.com.nsfatima.gestao.local.domain.repository;

import br.com.nsfatima.gestao.local.domain.model.Local;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência (Ports &amp; Adapters) para o agregado {@link Local}, isolando o domínio do
 * contexto {@code local} de detalhes de infraestrutura (JPA, banco de dados).
 */
public interface LocalRepository {

    /**
     * Persiste (insere ou atualiza) um Local.
     *
     * @param local instância de domínio a ser persistida
     */
    void save(Local local);

    /**
     * Busca um Local pelo seu identificador.
     *
     * @param id ID do local
     * @return o Local encontrado, ou {@link Optional#empty()} se não existir
     */
    Optional<Local> findById(UUID id);

    /**
     * Retorna todos os Locais cadastrados, independentemente de organização ou status de ativação.
     *
     * @return lista de todos os Locais
     */
    List<Local> findAll();

    /**
     * Retorna os Locais vinculados a uma organização (Matriz ou Comunidade) específica — usado para
     * escopar a seleção de locais ao contexto organizacional de quem está agendando um Evento.
     *
     * @param organizacaoId ID da organização de subordinação
     * @return lista de Locais vinculados à organização informada
     */
    List<Local> findByOrganizacaoId(UUID organizacaoId);

    /**
     * Verifica se o Local possui algum Evento associado, para dar suporte à decisão de bloquear uma
     * eventual remoção física que quebraria referências históricas.
     *
     * @param id ID do local
     * @return {@code true} se existir ao menos um Evento referenciando este local
     */
    boolean isLocalInUse(UUID id);

    /**
     * Verifica se o Local possui eventos futuros agendados (ativos/não cancelados), para bloquear
     * inativação ou exclusão lógica conforme regra de integridade referencial.
     *
     * @param id ID do local
     * @return {@code true} se houver eventos futuros agendados
     */
    boolean hasFutureEvents(UUID id);

    /**
     * Remove fisicamente o registro de um Local. Não é usada pelo fluxo padrão de negócio (que
     * desativa logicamente via {@code LocalService.excluir}) — capacidade de infraestrutura para
     * cenários administrativos/de dados que exigem remoção definitiva.
     *
     * @param id ID do local a ser removido
     */
    void delete(UUID id);
}
