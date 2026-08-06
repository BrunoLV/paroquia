package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Concede um papel global (`PAROCO`, `VIGARIO`, `ADMIN`, `SECRETARIA_PAROQUIAL`) a um {@code Fiel} já
 * cadastrado. Restrito a `ADMIN` — é a única forma de existir um "primeiro" Pároco/Vigário/Secretaria no
 * sistema, já que um `ADMIN` inicial é assumido como pré-existente via seed/configuração.
 */
@Service
public class AtribuirPapelGlobalUseCase {

    private final PapelGlobalJpaRepository repository;
    private final FielJpaRepository fielRepository;

    public AtribuirPapelGlobalUseCase(PapelGlobalJpaRepository repository, FielJpaRepository fielRepository) {
        this.repository = repository;
        this.fielRepository = fielRepository;
    }

    /**
     * Concede o papel global ao Fiel informado, reativando o registro se ele já existir desativado.
     *
     * @param fielId identificador do Fiel
     * @param papelRaw papel global a conceder
     * @return o identificador do registro criado ou reativado
     * @throws IllegalArgumentException se o Fiel não existir, o papel for inválido, ou já houver esse papel
     *                                   ativo para o Fiel
     * @throws AccessDeniedException se o ator autenticado não tiver papel {@code ADMIN}
     */
    @Transactional
    public UUID execute(UUID fielId, String papelRaw) {
        requireAdmin();

        if (fielRepository.findById(fielId).isEmpty()) {
            throw new IllegalArgumentException("Fiel não encontrado: " + fielId);
        }

        PapelGlobal papel = PapelGlobal.fromStoredValue(papelRaw);
        if (papel == null) {
            throw new IllegalArgumentException("Papel global inválido: " + papelRaw);
        }

        var existente = repository.findByFielIdAndPapel(fielId, papel.storedValue());
        if (existente.isPresent()) {
            var entity = existente.get();
            if (entity.isAtivo()) {
                throw new IllegalArgumentException("Fiel já possui este papel global ativo");
            }
            entity.setAtivo(true);
            return repository.save(entity).getId();
        }

        PapelGlobalEntity entity = new PapelGlobalEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setPapel(papel.storedValue());
        entity.setAtivo(true);
        return repository.save(entity).getId();
    }

    static void requireAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Apenas Admin pode conceder/revogar papéis globais");
        }
    }
}
