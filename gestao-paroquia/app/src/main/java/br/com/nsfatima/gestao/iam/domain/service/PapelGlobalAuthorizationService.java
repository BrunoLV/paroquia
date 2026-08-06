package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

/**
 * Fonte única de verdade para "este Fiel tem um papel de acesso irrestrito?" — substitui os antigos
 * pontos de checagem ad-hoc espalhados pelo código (comparações literais de authority/papel
 * ADMIN/PAROCO/VIGARIO), consultando diretamente {@code calendario.papel_global}.
 */
@Service
public class PapelGlobalAuthorizationService {

    private final PapelGlobalJpaRepository papelGlobalRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public PapelGlobalAuthorizationService(
            PapelGlobalJpaRepository papelGlobalRepository, UsuarioJpaRepository usuarioRepository) {
        this.papelGlobalRepository = papelGlobalRepository;
        this.usuarioRepository = usuarioRepository;
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
     * Verifica se o Fiel informado tem algum papel global ativo (acesso irrestrito).
     *
     * @param fielId identificador do Fiel
     * @return {@code true} se o Fiel tiver ao menos um papel global ativo
     */
    public boolean isGlobal(UUID fielId) {
        return !papeisGlobaisDe(fielId).isEmpty();
    }

    /**
     * Mesma verificação de {@link #isGlobal(UUID)}, mas partindo do {@code usuarioId} — resolve o
     * {@code fielId} do usuário internamente. Conveniência para os pontos de checagem que só têm acesso ao
     * ator autenticado (via {@code usuarioId}), não diretamente ao {@code fielId}.
     *
     * @param usuarioId identificador do usuário autenticado
     * @return {@code true} se o Fiel do usuário tiver ao menos um papel global ativo
     */
    public boolean isGlobalByUsuarioId(UUID usuarioId) {
        if (usuarioId == null) {
            return false;
        }
        return usuarioRepository.findById(usuarioId)
                .map(u -> isGlobal(u.getFielId()))
                .orElse(false);
    }
}
