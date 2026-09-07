package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Suspende somente contas pastorais sem vínculo ativo; perfis administrativos permanecem independentes. */
@Service
public class AvaliarAcessoPastoralUseCase {
    private static final Set<String> ADMINISTRATIVOS = Set.of(
            "ADMIN",
            "GLOBAL_ADMIN",
            "GLOBAL_PAROCO",
            "GLOBAL_SECRETARIA_PAROQUIAL");

    private final UsuarioJpaRepository usuarios;
    private final MembroGrupoJpaRepository vinculos;

    public AvaliarAcessoPastoralUseCase(UsuarioJpaRepository usuarios, MembroGrupoJpaRepository vinculos) {
        this.usuarios = usuarios;
        this.vinculos = vinculos;
    }

    @Transactional
    public void suspenderSemVinculo(UUID fielId) {
        usuarios.findByFielId(fielId)
                .filter(usuario -> !administrativo(usuario.getRoles()))
                .ifPresent(usuario -> {
                    boolean participa = vinculos.findByFielIdAndAtivoTrue(fielId).stream()
                            .anyMatch(v -> v.getTipoGrupo() == TipoGrupo.PASTORAL
                                    || v.getTipoGrupo() == TipoGrupo.MOVIMENTO);
                    if (!participa) {
                        usuario.setEnabled(false);
                        usuarios.save(usuario);
                    }
                });
    }

    private boolean administrativo(String roles) {
        return roles != null && ADMINISTRATIVOS.stream().anyMatch(roles::contains);
    }
}
