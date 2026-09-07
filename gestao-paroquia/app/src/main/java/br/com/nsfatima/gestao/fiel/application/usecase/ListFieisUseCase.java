package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielContatoResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper.FielMapper;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListFieisUseCase {

    private final FielJpaRepository fielRepository;
    private final FielMapper fielMapper;
    private final MembroGrupoJpaRepository membroGrupoRepository;

    public ListFieisUseCase(FielJpaRepository fielRepository, FielMapper fielMapper,
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
    public List<FielResponse> executeNoGrupo(java.util.UUID grupoId) {
        return fieisDoGrupo(grupoId).stream()
                .map(fielMapper::toResponse)
                .toList();
    }

    /** Retorna apenas os meios de contato que o coordenador pode usar no cuidado pastoral. */
    @Transactional(readOnly = true)
    public List<FielContatoResponse> executeContatoNoGrupo(java.util.UUID grupoId) {
        return fieisDoGrupo(grupoId).stream()
                .map(fielMapper::toContatoResponse)
                .toList();
    }

    private List<br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity> fieisDoGrupo(
            java.util.UUID grupoId) {
        var ids = membroGrupoRepository.findByGrupoParoquialIdAndAtivoTrue(grupoId).stream()
                .map(br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity::getFielId)
                .collect(java.util.stream.Collectors.toSet());
        return fielRepository.findByAtivoTrue().stream()
                .filter(fiel -> ids.contains(fiel.getId()))
                .toList();
    }
}
