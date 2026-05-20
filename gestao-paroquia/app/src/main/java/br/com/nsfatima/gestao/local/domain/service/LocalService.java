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
        Local local = new Local(UUID.randomUUID(), nome, tipo, endereco, capacidade, caracteristicas, true);
        localRepository.save(local);
        return local;
    }

    @Transactional
    public Local updateLocal(UUID id, String nome, String tipo, String endereco, Integer capacidade, String caracteristicas, boolean ativo) {
        Local local = localRepository.findById(id)
                .orElseThrow(() -> LocalBusinessException.notFound(id));
        
        local.update(nome, tipo, endereco, capacidade, caracteristicas, ativo);
        localRepository.save(local);
        return local;
    }

    @Transactional
    public void deleteLocal(UUID id) {
        if (localRepository.isLocalInUse(id)) {
            throw LocalBusinessException.inUse(id);
        }
        localRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public Optional<Local> getLocal(UUID id) {
        return localRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Local> listLocais() {
        return localRepository.findAll();
    }
}
