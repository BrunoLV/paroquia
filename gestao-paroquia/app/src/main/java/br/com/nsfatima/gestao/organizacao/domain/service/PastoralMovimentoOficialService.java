package br.com.nsfatima.gestao.organizacao.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.PastoralMovimentoOficialBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PastoralMovimentoOficialService {

    private final PastoralMovimentoOficialRepository repository;

    public PastoralMovimentoOficialService(PastoralMovimentoOficialRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PastoralMovimentoOficial create(String nome, TipoOrganizacao tipo) {
        validate(null, nome, tipo);
        PastoralMovimentoOficial oficial = new PastoralMovimentoOficial(UUID.randomUUID(), nome, tipo);
        repository.save(oficial);
        return oficial;
    }

    @Transactional
    public PastoralMovimentoOficial update(UUID id, String nome, TipoOrganizacao tipo) {
        PastoralMovimentoOficial oficial = repository.findById(id)
                .orElseThrow(() -> PastoralMovimentoOficialBusinessException.notFound(id));
        validate(id, nome, tipo);
        oficial.update(nome, tipo);
        repository.save(oficial);
        return oficial;
    }

    @Transactional
    public void delete(UUID id) {
        PastoralMovimentoOficial oficial = repository.findById(id)
                .orElseThrow(() -> PastoralMovimentoOficialBusinessException.notFound(id));
        repository.delete(id);
    }

    @Transactional(readOnly = true)
    public Optional<PastoralMovimentoOficial> get(UUID id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<PastoralMovimentoOficial> list() {
        return repository.findAll();
    }

    private void validate(UUID id, String nome, TipoOrganizacao tipo) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new PastoralMovimentoOficialBusinessException("Nome é obrigatório");
        }
        if (tipo != TipoOrganizacao.PASTORAL && tipo != TipoOrganizacao.MOVIMENTO) {
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
