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
     * Verifica se o Local possui algum Evento associado (diretamente ou pela associação com múltiplos
     * locais), preservando as referências históricas. A exclusão do local é sempre lógica.
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
     * Verifica se o Local tem reservas pendentes ou confirmadas que ainda não terminaram, que também
     * impedem a inativação ou a exclusão lógica: quem reservou o espaço precisa ser avisado antes.
     *
     * @param id ID do local
     * @return {@code true} se houver reservas futuras ou em andamento
     */
    boolean hasFutureReservas(UUID id);
}
