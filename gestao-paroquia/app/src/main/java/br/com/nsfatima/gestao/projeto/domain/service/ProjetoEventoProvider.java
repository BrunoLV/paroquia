package br.com.nsfatima.gestao.projeto.domain.service;

import java.time.Instant;
import java.util.UUID;

public interface ProjetoEventoProvider {
    long countByProjetoId(UUID projetoId);

    long countByProjetoIdAndStatus(UUID projetoId, String status);

    long countByProjetoIdAndFimUtcLessThan(UUID projetoId, Instant data);

    long countByProjetoIdAndFimUtcGreaterThanEqual(UUID projetoId, Instant data);

    java.util.List<String> findInvolvedOrganizationNames(UUID projetoId);
}
