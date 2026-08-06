package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.PastoralMovimentoOficialBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orquestra o ciclo de vida do catálogo oficial de Pastorais e Movimentos ({@link PastoralMovimentoOficial}),
 * garantindo que cada item tenha nome obrigatório e único e tipo restrito a {@code PASTORAL} ou
 * {@code MOVIMENTO}. Este catálogo é referenciado por {@code GrupoParoquial.pastoralOficialId} para garantir
 * que instâncias locais de uma pastoral/movimento em diferentes comunidades compartilhem o mesmo nome/tipo
 * oficial.
 */
@Service
public class PastoralMovimentoOficialService {

    private final PastoralMovimentoOficialRepository repository;

    public PastoralMovimentoOficialService(PastoralMovimentoOficialRepository repository) {
        this.repository = repository;
    }

    /**
     * Cria um novo item do catálogo oficial, validando nome obrigatório/único e tipo permitido.
     *
     * @param nome nome oficial da pastoral/movimento
     * @param tipo tipo, restrito a {@code PASTORAL} ou {@code MOVIMENTO}
     * @return o item recém-criado
     * @throws PastoralMovimentoOficialBusinessException se o nome for ausente/duplicado ou o tipo inválido
     */
    @Transactional
    public PastoralMovimentoOficial create(String nome, TipoGrupo tipo) {
        validate(null, nome, tipo);
        PastoralMovimentoOficial oficial = new PastoralMovimentoOficial(UUID.randomUUID(), nome, tipo);
        repository.save(oficial);
        return oficial;
    }

    /**
     * Atualiza nome e tipo de um item existente do catálogo oficial, revalidando as mesmas regras de
     * {@link #create(String, TipoGrupo)} (exceto que o próprio item, se já tiver este nome, não conta
     * como duplicidade).
     *
     * @param id identificador do item a atualizar
     * @param nome novo nome oficial
     * @param tipo novo tipo, restrito a {@code PASTORAL} ou {@code MOVIMENTO}
     * @return o item atualizado
     * @throws PastoralMovimentoOficialBusinessException se o item não existir, o nome for ausente/duplicado
     *     por outro item, ou o tipo for inválido
     */
    @Transactional
    public PastoralMovimentoOficial update(UUID id, String nome, TipoGrupo tipo) {
        PastoralMovimentoOficial oficial = repository.findById(id)
                .orElseThrow(() -> PastoralMovimentoOficialBusinessException.notFound(id));
        validate(id, nome, tipo);
        oficial.update(nome, tipo);
        repository.save(oficial);
        return oficial;
    }

    /**
     * Remove definitivamente um item do catálogo oficial.
     *
     * @param id identificador do item a remover
     * @throws PastoralMovimentoOficialBusinessException se o item não existir
     */
    @Transactional
    public void delete(UUID id) {
        PastoralMovimentoOficial oficial = repository.findById(id)
                .orElseThrow(() -> PastoralMovimentoOficialBusinessException.notFound(id));
        repository.delete(id);
    }

    /**
     * Busca um item do catálogo oficial pelo seu identificador.
     *
     * @param id identificador do item
     * @return o item encontrado, ou {@link Optional#empty()} se não existir
     */
    @Transactional(readOnly = true)
    public Optional<PastoralMovimentoOficial> get(UUID id) {
        return repository.findById(id);
    }

    /**
     * Lista todos os itens do catálogo oficial de Pastorais e Movimentos.
     *
     * @return lista de todos os itens cadastrados
     */
    @Transactional(readOnly = true)
    public List<PastoralMovimentoOficial> list() {
        return repository.findAll();
    }

    private void validate(UUID id, String nome, TipoGrupo tipo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new PastoralMovimentoOficialBusinessException("Nome é obrigatório");
        }
        if (tipo != TipoGrupo.PASTORAL && tipo != TipoGrupo.MOVIMENTO) {
            throw new PastoralMovimentoOficialBusinessException("Tipo deve ser PASTORAL ou MOVIMENTO");
        }

        // Unique name check (case-insensitive or exact)
        if (repository.existsByNome(nome)) {
            // Check if it belongs to the current entity being updated
            Optional<PastoralMovimentoOficial> existing = repository.findAll().stream()
                    .filter(o -> o.getNome().equalsIgnoreCase(nome))
                    .findFirst();
            if (existing.isPresent() && (id == null || !existing.get().getId().equals(id))) {
                throw new PastoralMovimentoOficialBusinessException("Pastoral ou Movimento com este nome já existe");
            }
        }
    }
}
