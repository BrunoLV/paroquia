package br.com.nsfatima.gestao.local.domain.service;

import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida dos Locais (espaços físicos/lógicos usados por Eventos), aplicando as regras
 * de negócio do domínio antes de persistir através do {@link LocalRepository}.
 *
 * <p>Regra de exclusão: não há remoção física de um Local por este serviço — {@link #deleteLocal(UUID)}
 * realiza uma desativação lógica (seta {@code ativo=false}), preservando o histórico de referências de
 * Eventos passados que apontam para o local. A remoção física existe apenas no {@link LocalRepository}
 * como capacidade de infraestrutura, não é acionada pelo fluxo padrão de negócio.
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
     * Cadastra um novo Local, opcionalmente vinculado a uma organização (Matriz ou Comunidade), o que
     * restringe/escopa seu uso a essa organização para fins de autorização e listagem.
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
     * @throws LocalBusinessException se não existir Local com o ID informado
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
     * @throws LocalBusinessException se não existir Local com o ID informado
     */
    @Transactional
    public Local updateLocal(UUID id,
                             String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             boolean ativo, UUID grupoParoquialId) {

        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));

        local.update(nome, tipo, endereco, capacidade, caracteristicas, ativo, grupoParoquialId);
        localRepository.save(local);
        return local;
    }

    /**
     * Desativa logicamente um Local (não o remove fisicamente), preservando os demais dados e o
     * histórico de Eventos que já o referenciam.
     *
     * @param id ID do local a ser desativado
     * @throws LocalBusinessException se não existir Local com o ID informado
     */
    @Transactional
    public void deleteLocal(UUID id) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));
        local.update(local.getNome(), local.getTipo(), local.getEndereco(),
                local.getCapacidade(), local.getCaracteristicas(), false, local.getGrupoParoquialId());

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
     * Lista todos os Locais cadastrados, sem filtro por organização.
     *
     * @return lista de todos os Locais
     */
    @Transactional(readOnly = true)
    public List<Local> listLocais() {
        return listLocais(null);
    }

    /**
     * Lista os Locais cadastrados, opcionalmente restritos a uma organização (Matriz/Comunidade) —
     * usado para escopar a seleção de locais ao planejar Eventos de uma organização específica.
     *
     * @param grupoParoquialId ID da organização para filtrar, ou {@code null} para listar todos os Locais
     * @return lista de Locais, filtrada por organização quando {@code grupoParoquialId} não for {@code null}
     */
    @Transactional(readOnly = true)
    public List<Local> listLocais(UUID grupoParoquialId) {
        if (grupoParoquialId != null) {
            return localRepository.findByGrupoParoquialId(grupoParoquialId);
        }
        return localRepository.findAll();
    }
}
