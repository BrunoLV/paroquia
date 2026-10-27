package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielContatoResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielNomeResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lista o registro comunitário de Fiéis da paróquia.
 *
 * REGRA DE NEGÓCIO:
 * Suporta filtragem por organização e vínculo pastoral, garantindo que
 * apenas coordenadores e administradores acessem dados de contato.
 */
@Service
public class ListarFieisUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;
    private final MembroGrupoJpaRepository membroGrupoRepository;

    public ListarFieisUseCase(FielJpaRepository fielRepository, FielMapper fielMapper,
                            MembroGrupoJpaRepository membroGrupoRepository) {
        this.fielRepository = fielRepository;
        this.fielMapper = fielMapper;
        this.membroGrupoRepository = membroGrupoRepository;
    }

    @Transactional(readOnly = true)
    public List<FielResponse> execute() {
        return fielRepository.findByAtivoTrue().stream()
                .map(fielMapper::toResponse)
                .toList();
    }

    /** Retorna somente os fiéis com vínculo ativo na organização do coordenador. */
    @Transactional(readOnly = true)
    public List<FielResponse> executeNoGrupo(UUID grupoId) {
        return fieisDoGrupo(grupoId).stream()
                .map(fielMapper::toResponse)
                .toList();
    }

    /** Retorna apenas os meios de contato que o coordenador pode usar no cuidado pastoral. */
    @Transactional(readOnly = true)
    public List<FielContatoResponse> executeContatoNoGrupo(UUID grupoId) {
        return fieisDoGrupo(grupoId).stream()
                .map(fielMapper::toContatoResponse)
                .toList();
    }

    /** Retorna somente identificador e nome de todos os fiéis ativos, para papéis consultivos. */
    @Transactional(readOnly = true)
    public List<FielNomeResponse> executeNomes() {
        return fielRepository.findByAtivoTrue().stream()
                .map(fielMapper::toNomeResponse)
                .toList();
    }

    /** Retorna somente identificador e nome dos fiéis com vínculo ativo na organização. */
    @Transactional(readOnly = true)
    public List<FielNomeResponse> executeNomesNoGrupo(UUID grupoId) {
        return fieisDoGrupo(grupoId).stream()
                .map(fielMapper::toNomeResponse)
                .toList();
    }

    private List<FielEntity> fieisDoGrupo(UUID grupoId) {
        Set<UUID> ids = membroGrupoRepository.findByOrganizacaoIdAndAtivoTrue(grupoId).stream()
                .map(MembroGrupoEntity::getFielId)
                .collect(Collectors.toSet());
        return fielRepository.findByAtivoTrue().stream()
                .filter(fiel -> ids.contains(fiel.getId()))
                .toList();
    }
}
