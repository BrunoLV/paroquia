package br.com.nsfatima.gestao.local.domain.service;

import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LocalService {

    private final LocalRepository localRepository;

    public LocalService(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    @Transactional
    public Local createLocal(String nome, String tipo, String endereco, Integer capacidade, String caracteristicas) {
        return createLocal(nome, tipo, endereco, capacidade, caracteristicas, null);
    }

    @Transactional
    public Local createLocal(String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             UUID organizacaoId) {
        Local local = new Local(UUID.randomUUID(), nome, tipo, endereco, capacidade, caracteristicas, true,
                organizacaoId);
        localRepository.save(local);
        return local;
    }

    @Transactional
    public Local updateLocal(UUID id,
                             String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             boolean ativo) {

        return updateLocal(id, nome, tipo, endereco, capacidade, caracteristicas, ativo, null);
    }

    @Transactional
    public Local updateLocal(UUID id,
                             String nome, String tipo, String endereco, Integer capacidade, String caracteristicas,
                             boolean ativo, UUID organizacaoId) {

        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));

        local.update(nome, tipo, endereco, capacidade, caracteristicas, ativo, organizacaoId);
        localRepository.save(local);
        return local;
    }

    @Transactional
    public void deleteLocal(UUID id) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));
        local.update(local.getNome(), local.getTipo(), local.getEndereco(),
                local.getCapacidade(), local.getCaracteristicas(), false, local.getOrganizacaoId());

        localRepository.save(local);
    }

    @Transactional(readOnly = true)
    public Optional<Local> getLocal(UUID id) {
        return localRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Local> listLocais() {
        return listLocais(null);
    }

    @Transactional(readOnly = true)
    public List<Local> listLocais(UUID organizacaoId) {
        if (organizacaoId != null) {
            return localRepository.findByOrganizacaoId(organizacaoId);
        }
        return localRepository.findAll();
    }
}
