package br.com.nsfatima.gestao.projeto.domain.service;

import java.util.UUID;
import java.time.Instant;

public interface ProjectEventProvider {
    long countByProjetoId(UUID projetoId);
    long countByProjetoIdAndStatus(UUID projetoId, String status);
    long countByProjetoIdAndFimUtcLessThan(UUID projetoId, Instant data);
    long countByProjetoIdAndFimUtcGreaterThanEqual(UUID projetoId, Instant data);
    java.util.List<String> findInvolvedOrganizationNames(UUID projetoId);
}
