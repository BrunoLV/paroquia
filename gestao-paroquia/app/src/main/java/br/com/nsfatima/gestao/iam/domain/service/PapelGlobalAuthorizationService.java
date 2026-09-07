package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.EncargoClericalJpaRepository;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Fonte única de verdade para "este Fiel ou Clérigo tem um papel de acesso irrestrito?".
 */
@Service
public class PapelGlobalAuthorizationService {

    private final PapelGlobalJpaRepository papelGlobalRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final EncargoClericalJpaRepository encargoClericalRepository;

    public PapelGlobalAuthorizationService(
            PapelGlobalJpaRepository papelGlobalRepository,
            UsuarioJpaRepository usuarioRepository,
            EncargoClericalJpaRepository encargoClericalRepository) {
        this.papelGlobalRepository = papelGlobalRepository;
        this.usuarioRepository = usuarioRepository;
        this.encargoClericalRepository = encargoClericalRepository;
    }

    /**
     * Retorna os papéis globais ativos do Fiel informado.
     *
     * @param fielId identificador do Fiel
     * @return conjunto de papéis globais ativos; vazio se {@code fielId} for {@code null} ou não tiver
     *     nenhum papel global
     */
    public Set<PapelGlobal> papeisGlobaisDe(UUID fielId) {
        if (fielId == null) {
            return Set.of();
        }
        Set<PapelGlobal> papeis = EnumSet.noneOf(PapelGlobal.class);
        for (var entity : papelGlobalRepository.findByFielIdAndAtivoTrue(fielId)) {
            PapelGlobal papel = PapelGlobal.fromStoredValue(entity.getPapel());
            if (papel != null) {
                papeis.add(papel);
            }
        }
        return papeis;
    }

    /**
     * Retorna os papéis globais ativos derivados dos encargos do Clérigo informado.
     */
    public Set<PapelGlobal> papeisGlobaisDeClerigo(UUID clerigoId) {
        if (clerigoId == null) {
            return Set.of();
        }
        Set<PapelGlobal> papeis = EnumSet.noneOf(PapelGlobal.class);
        for (var encargo : encargoClericalRepository.findByClerigoIdAndAtualTrue(clerigoId)) {
            PapelGlobal papel = mapFuncaoToPapelGlobal(encargo.getFuncao());
            if (papel != null) {
                papeis.add(papel);
            }
        }
        return papeis;
    }

    /**
     * Verifica se o Fiel informado tem algum papel global ativo (acesso irrestrito).
     *
     * @param fielId identificador do Fiel
     * @return {@code true} se o Fiel tiver ao menos um papel global ativo
     */
    public boolean isGlobal(UUID fielId) {
        return !papeisGlobaisDe(fielId).isEmpty();
    }

    /**
     * Verifica se o Clérigo informado tem encargo institucional com papel global ativo.
     */
    public boolean isGlobalClerigo(UUID clerigoId) {
        return !papeisGlobaisDeClerigo(clerigoId).isEmpty();
    }

    /**
     * Mesma verificação de acesso global, partindo do {@code usuarioId}.
     *
     * @param usuarioId identificador do usuário autenticado
     * @return {@code true} se o usuário tiver ao menos um papel global ativo
     */
    public boolean isGlobalByUsuarioId(UUID usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return usuarioRepository.findById(usuarioId)
                .map(u -> {
                    if (u.getFielId() != null && isGlobal(u.getFielId())) {
                        return true;
                    }
                    if (u.getClerigoId() != null && isGlobalClerigo(u.getClerigoId())) {
                        return true;
                    }
                    return hasGlobalRole(u.getRoles());
                })
                .orElse(false);
    }

    private PapelGlobal mapFuncaoToPapelGlobal(FuncaoEclesiastica funcao) {
        if (funcao == null) {
            return null;
        }
        return switch (funcao) {
            case BISPO_DIOCESANO -> PapelGlobal.BISPO_DIOCESANO;
            case PAROCO, ADMINISTRADOR_PAROQUIAL -> PapelGlobal.PAROCO;
            case VIGARIO_PAROQUIAL -> PapelGlobal.VIGARIO;
            default -> null;
        };
    }

    private boolean hasGlobalRole(String roles) {
        if (roles == null || roles.isBlank()) {
            return false;
        }
        return Arrays.stream(roles.split(","))
                .map(String::trim)
                .anyMatch(r -> r.equalsIgnoreCase("ADMIN")
                        || r.equalsIgnoreCase("PAROCO")
                        || r.equalsIgnoreCase("VIGARIO")
                        || r.equalsIgnoreCase("BISPO_DIOCESANO")
                        || r.equalsIgnoreCase("ROLE_ADMIN")
                        || r.equalsIgnoreCase("ROLE_PAROCO")
                        || r.equalsIgnoreCase("ROLE_VIGARIO")
                        || r.equalsIgnoreCase("ROLE_BISPO_DIOCESANO"));
    }
}
