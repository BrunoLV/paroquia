package br.com.nsfatima.gestao.local.domain.repository;

import br.com.nsfatima.gestao.local.domain.model.Local;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocalRepository {
    void save(Local local);
    Optional<Local> findById(UUID id);
    List<Local> findAll();
    boolean isLocalInUse(UUID id);
    void delete(UUID id);
}
