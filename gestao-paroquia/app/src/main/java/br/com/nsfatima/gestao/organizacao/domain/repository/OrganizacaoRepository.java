package br.com.nsfatima.gestao.organizacao.domain.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizacaoRepository {
    void save(Organizacao organizacao);
    Optional<Organizacao> findById(UUID id);
    List<Organizacao> findAll();
    boolean hasDependencies(UUID id);
    void delete(UUID id);
}
