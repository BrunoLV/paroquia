package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Revoga o vínculo de um Fiel com uma organização, desativando-o (sem excluí-lo) para preservar o
 * histórico de quem já atuou naquela organização e em que papel.
 */
@Service
public class RemoverVinculoUseCase {

    private final MembroGrupoJpaRepository vinculoRepository;

    public RemoverVinculoUseCase(MembroGrupoJpaRepository vinculoRepository) {
        this.vinculoRepository = vinculoRepository;
    }

    /**
     * Desativa o vínculo de um Fiel com uma organização para revogar seus acessos e responsabilidades
     * administrativas naquele contexto específico, mantendo o registro histórico do vínculo.
     *
     * @param vinculoId identificador do vínculo a desativar
     * @throws IllegalArgumentException se o vínculo não existir
     */
    @Transactional
    public void execute(UUID vinculoId) {
        MembroGrupoEntity entity = vinculoRepository.findById(vinculoId)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + vinculoId));

        entity.setAtivo(false);
        vinculoRepository.save(entity);
    }
}
