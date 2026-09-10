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
 * Orquestra o ciclo de vida dos Locais (espaços físicos usados por Eventos e celebrações),
 * aplicando as regras de integridade e políticas de negócio antes de persistir.
 *
 * <p>Regras de integridade referencial:
 * <ul>
 *   <li>Não é permitido inativar (ativo=false) nem excluir logicamente (soft delete) um Local que possua
 *       eventos futuros agendados não cancelados;</li>
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
     * Cadastra um novo Local com dados estruturados de endereço e capacidade máxima.
     *
     * @param nome             nome do local físico
     * @param tipo             tipo/categoria do espaço (ex.: SALAO_PAROQUIAL, SALA, AUDITORIO)
     * @param enderecoLocal    endereço estruturado (Value Object)
     * @param capacidadeMaxima lotação máxima permitida
     * @param descricao        finalidades de uso e restrições
     * @param igrejaId         ID da paróquia ou comunidade vinculada
     * @return o Local cadastrado persistido
     */
    @Transactional
    public Local createLocal(String nome,
                             String tipo,
                             EnderecoLocal enderecoLocal,
                             Integer capacidadeMaxima,
                             String descricao,
                             UUID igrejaId) {
        Instant now = Instant.now();
        Local local = new Local(
                UUID.randomUUID(),
                nome,
                tipo,
                enderecoLocal,
                capacidadeMaxima,
                descricao,
                true,
                igrejaId,
                null,
                null,
                now,
                now
        );
        localRepository.save(local);
        return localRepository.findById(local.getId()).orElse(local);
    }

    /**
     * Atualiza as informações de um Local existente validando regras de integridade.
     *
     * @param id               identificador do local
     * @param nome             novo nome
     * @param tipo             novo tipo/categoria
     * @param enderecoLocal    novo endereço estruturado
     * @param capacidadeMaxima nova lotação máxima
     * @param descricao        nova descrição/observações
     * @param ativo            status de disponibilidade do local
     * @param igrejaId         nova igreja vinculada
     * @return o Local atualizado persistido
     * @throws LocalBusinessException se o local não existir ou possuir eventos futuros ao inativar
     */
    @Transactional
    public Local updateLocal(UUID id,
                             String nome,
                             String tipo,
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

        local.alterarCadastro(nome, tipo, enderecoLocal, capacidadeMaxima, descricao, igrejaId);
        if (ativo) {
            local.ativar();
        } else {
            local.inativar();
        }
        localRepository.save(local);
        return localRepository.findById(local.getId()).orElse(local);
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
     * Lista os Locais cadastrados não excluídos, opcionalmente restritos a uma paróquia/comunidade.
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
