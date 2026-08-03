package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Revoga o vínculo de um usuário com uma organização, desativando-o (sem excluí-lo) para preservar o
 * histórico de quem já atuou naquela organização e em que papel.
 */
@Service
public class RemoverVinculoUseCase {

    private final MembroOrganizacaoJpaRepository vinculoRepository;

    public RemoverVinculoUseCase(MembroOrganizacaoJpaRepository vinculoRepository) {
        this.vinculoRepository = vinculoRepository;
    }

    /**
     * Desativa o vínculo de um usuário com uma organização para revogar seus acessos e responsabilidades
     * administrativas naquele contexto específico, mantendo o registro histórico do vínculo.
     *
     * @param vinculoId identificador do vínculo a desativar
     * @throws IllegalArgumentException se o vínculo não existir
     */
    @Transactional
    public void execute(UUID vinculoId) {
        MembroOrganizacaoEntity entity = vinculoRepository.findById(vinculoId)
                .orElseThrow(() -> new IllegalArgumentException("Membro nao encontrado: " + vinculoId));

        entity.setAtivo(false);
        vinculoRepository.save(entity);
    }
}
