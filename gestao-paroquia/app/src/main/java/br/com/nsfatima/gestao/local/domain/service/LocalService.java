package br.com.nsfatima.gestao.local.domain.service;

import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.local.domain.model.EnderecoLocal;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida dos Locais (espaços físicos/lógicos usados por Eventos), aplicando as regras
 * de negócio do domínio antes de persistir através do {@link LocalRepository}.
 *
 * <p>Regras de integridade referencial:
 * <ul>
 *   <li>Não é permitido inativar (status=false) nem excluir logicamente (soft delete) um Local que possua
 *       eventos futuros agendados ativos/não cancelados;</li>
 *   <li>Exclusão é sempre lógica (preenche {@code deletedAt} com o timestamp atual), preservando histórico
 *       de referências de Eventos passados.</li>
 * </ul>
 */
@Service
public class LocalService {

    private final LocalRepository localRepository;

    /**
     * Inicializa o serviço com o repositório de domínio usado para persistir e consultar Locais.
     *
     * @param localRepository porta de persistência de Locais
     */
    public LocalService(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    /**
     * Cadastra um novo Local sem vínculo com uma organização (Matriz/Comunidade), útil para espaços
     * de uso geral da paróquia.
     *
     * @param nome            nome do local
     * @param tipo            tipo/categoria do local
     * @param endereco        endereço físico do local
     * @param capacidade      capacidade máxima de pessoas
     * @param caracteristicas características adicionais (ex.: recursos disponíveis)
     * @return o Local criado, sempre com {@code ativo=true}
     */
    @Transactional
    public Local createLocal(String nome, String tipo, String endereco, Integer capacidade, String caracteristicas) {
        return createLocal(nome, tipo, endereco, capacidade, caracteristicas, null);
    }

    /**
     * Cadastra um novo Local, opcionalmente vinculado a uma organização (Matriz ou Comunidade).
     *
     * @param nome            nome do local
     * @param tipo            tipo/categoria do local
     * @param endereco        endereço físico do local
     * @param capacidade      capacidade máxima de pessoas
     * @param caracteristicas características adicionais (ex.: recursos disponíveis)
     * @param grupoParoquialId   ID da organização de subordinação, ou {@code null} para local sem vínculo
     * @return o Local criado, sempre com {@code ativo=true}
     */
    @Transactional
    public Local createLocal(String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             UUID grupoParoquialId) {
        Local local = new Local(UUID.randomUUID(), nome, tipo, endereco, capacidade, caracteristicas, true,
                grupoParoquialId);
        localRepository.save(local);
        return local;
    }

    /**
     * Cadastra um novo Local com dados completos e endereço estruturado.
     *
     * @param nome             nome do local
     * @param tipo             tipo/categoria do local
     * @param enderecoFormatado endereço plano formatado
     * @param enderecoLocal    endereço estruturado (Value Object)
     * @param capacidadeMaxima capacidade máxima de ocupação
     * @param descricao        descrição e finalidades de uso
     * @param igrejaId         ID da paróquia/comunidade vinculada
     * @return o Local cadastrado
     */
    @Transactional
    public Local createLocalCompleto(String nome,
                                    String tipo,
                                    String enderecoFormatado,
                                    EnderecoLocal enderecoLocal,
                                    Integer capacidadeMaxima,
                                    String descricao,
                                    UUID igrejaId) {
        Instant now = Instant.now();
        Local local = new Local(
                UUID.randomUUID(),
                nome,
                tipo,
                enderecoFormatado,
                enderecoLocal,
                capacidadeMaxima,
                capacidadeMaxima,
                descricao,
                descricao,
                true,
                igrejaId,
                null,
                null,
                now,
                now
        );
        localRepository.save(local);
        return local;
    }

    /**
     * Atualiza os dados de um Local existente sem alterar seu vínculo organizacional atual.
     *
     * @param id              ID do local a ser atualizado
     * @param nome            novo nome do local
     * @param tipo            novo tipo/categoria do local
     * @param endereco        novo endereço físico do local
     * @param capacidade      nova capacidade máxima de pessoas
     * @param caracteristicas novas características adicionais
     * @param ativo           novo status de ativação do local
     * @return o Local atualizado
     * @throws LocalBusinessException se não existir Local ou se possuir eventos futuros agendados ao inativar
     */
    @Transactional
    public Local updateLocal(UUID id,
                             String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             boolean ativo) {
        return updateLocal(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, null);
    }

    /**
     * Atualiza os dados de um Local existente, podendo também alterar (ou remover) seu vínculo com uma
     * organização (Matriz/Comunidade).
     *
     * @param id              ID do local a ser atualizado
     * @param nome            novo nome do local
     * @param tipo            novo tipo/categoria do local
     * @param endereco        novo endereço físico do local
     * @param capacidade      nova capacidade máxima de pessoas
     * @param caracteristicas novas características adicionais
     * @param ativo           novo status de ativação do local
     * @param grupoParoquialId   novo ID da organização de subordinação, ou {@code null} para desvincular
     * @return o Local atualizado
     * @throws LocalBusinessException se não existir Local ou se possuir eventos futuros ao inativar
     */
    @Transactional
    public Local updateLocal(UUID id,
                             String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             boolean ativo, UUID grupoParoquialId) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));

        if (!ativo && local.isAtivo() && localRepository.hasFutureEvents(id)) {
            throw LocalBusinessException.hasFutureEventsOnDeactivate(id);
        }

        local.update(nome, tipo, endereco, capacidade, caracteristicas, ativo, grupoParoquialId);
        localRepository.save(local);
        return local;
    }

    /**
     * Atualização completa de um Local suportando campos estruturados de endereço e validação de integridade.
     */
    @Transactional
    public Local updateLocalCompleto(UUID id,
                                    String nome,
                                    String tipo,
                                    String enderecoFormatado,
                                    EnderecoLocal enderecoLocal,
                                    Integer capacidadeMaxima,
                                    String descricao,
                                    boolean ativo,
                                    UUID igrejaId) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));

        if (!ativo && local.isAtivo() && localRepository.hasFutureEvents(id)) {
            throw LocalBusinessException.hasFutureEventsOnDeactivate(id);
        }

        local.updateCompleto(
                nome, tipo, enderecoFormatado, enderecoLocal, capacidadeMaxima, descricao, ativo, igrejaId);
        localRepository.save(local);
        return local;
    }

    /**
     * Exclui logicamente um Local (soft delete), validando integridade referencial com eventos futuros.
     *
     * @param id ID do local a ser excluído
     * @throws LocalBusinessException se não existir Local ou se possuir eventos futuros agendados
     */
    @Transactional
    public void deleteLocal(UUID id) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));

        if (localRepository.hasFutureEvents(id)) {
            throw LocalBusinessException.hasFutureEventsOnDelete(id);
        }

        local.marcarExcluido();
        localRepository.save(local);
    }

    /**
     * Busca um Local pelo seu identificador.
     *
     * @param id ID do local
     * @return o Local encontrado, ou {@link Optional#empty()} se não existir
     */
    @Transactional(readOnly = true)
    public Optional<Local> getLocal(UUID id) {
        return localRepository.findById(id);
    }

    /**
     * Lista todos os Locais cadastrados ativos e não excluídos.
     *
     * @return lista de todos os Locais
     */
    @Transactional(readOnly = true)
    public List<Local> listLocais() {
        return listLocais(null);
    }

    /**
     * Lista os Locais cadastrados não excluídos, opcionalmente restritos a uma organização.
     *
     * @param grupoParoquialId ID da organização para filtrar, ou {@code null} para listar todos os Locais
     * @return lista de Locais filtrada
     */
    @Transactional(readOnly = true)
    public List<Local> listLocais(UUID grupoParoquialId) {
        if (grupoParoquialId != null) {
            return localRepository.findByGrupoParoquialId(grupoParoquialId);
        }
        return localRepository.findAll();
    }
}
