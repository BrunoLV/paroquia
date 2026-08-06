package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Revoga um papel global de um Fiel, desativando o registro (sem excluí-lo, para preservar histórico).
 * Restrito a `ADMIN`, mesma regra de {@link AtribuirPapelGlobalUseCase}.
 */
@Service
public class RevogarPapelGlobalUseCase {

    private final PapelGlobalJpaRepository repository;

    public RevogarPapelGlobalUseCase(PapelGlobalJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Desativa o papel global informado para o Fiel.
     *
     * @param fielId identificador do Fiel
     * @param papelRaw papel global a revogar
     * @throws IllegalArgumentException se o papel for inválido ou o Fiel não tiver esse papel global ativo
     * @throws org.springframework.security.access.AccessDeniedException se o ator autenticado não tiver
     *                                                                    papel {@code ADMIN}
     */
    @Transactional
    public void execute(UUID fielId, String papelRaw) {
        AtribuirPapelGlobalUseCase.requireAdmin();

        PapelGlobal papel = PapelGlobal.fromStoredValue(papelRaw);
        if (papel == null) {
            throw new IllegalArgumentException("Papel global inválido: " + papelRaw);
        }

        var entity = repository.findByFielIdAndPapel(fielId, papel.storedValue())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não possui este papel global"));

        if (!entity.isAtivo()) {
            throw new IllegalArgumentException("Fiel não possui este papel global ativo");
        }

        entity.setAtivo(false);
        repository.save(entity);
    }
}
