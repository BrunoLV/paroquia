package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddMembershipUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public AddMembershipUseCase(
            MembroOrganizacaoJpaRepository membershipRepository,
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.membershipRepository = membershipRepository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional
    public UUID execute(UUID usuarioId,
                        UUID organizacaoId, TipoOrganizacao tipo, PapelOrganizacional papel, UUID igrejaVinculoId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario nao encontrado: " + usuarioId);
        }
        var usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getIgrejaId() != null) {
            UUID userChurchId = usuarioOpt.get().getIgrejaId();
            UUID targetChurchId = resolveTargetChurchId(organizacaoId, igrejaVinculoId);
            if (targetChurchId != null && !userChurchId.equals(targetChurchId)) {
                throw new IllegalArgumentException(
                        "O usuário pertence a outra igreja/comunidade"
                                + " e não pode ser vinculado a esta organização");
            }
        }

        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            if (igrejaVinculoId == null) {
                throw new IllegalArgumentException("Igreja de vinculo e obrigatoria para pastorais e movimentos");
            }
            var igrejaOpt = organizacaoRepository.findById(igrejaVinculoId);
            if (igrejaOpt.isEmpty()) {
                throw new IllegalArgumentException("Igreja de vinculo nao encontrada: " + igrejaVinculoId);
            }
            var igreja = igrejaOpt.get();
            if (igreja.getTipo() != TipoOrganizacao.MATRIZ && igreja.getTipo() != TipoOrganizacao.COMUNIDADE) {
                throw new IllegalArgumentException("Igreja de vinculo deve ser do tipo MATRIZ ou COMUNIDADE");
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        boolean isParoco = auth.getAuthorities().stream().anyMatch(a -> "ROLE_CLERO_PAROCO".equals(a.getAuthority()));

        // Hierarchy rule: COORDENADOR and VICE of CONSELHO / CONSELHO_ECONOMICO can only be defined by PAROCO or ADMIN
        if ((tipo == TipoOrganizacao.CONSELHO_PASTORAL || tipo == TipoOrganizacao.CONSELHO_ECONOMICO)
                && (papel == PapelOrganizacional.COORDENADOR
                || papel == PapelOrganizacional.VICE_COORDENADOR)) {
            if (!isAdmin && !isParoco) {
                throw new AccessDeniedException(
                        "Apenas o Pároco ou Admin podem definir coordenador e vice do Conselho");
            }
        }

        // Hierarchy rule: PAROCO can only be defined by ADMIN
        if (tipo == TipoOrganizacao.CLERO && papel == PapelOrganizacional.PAROCO) {
            if (!isAdmin) {
                throw new AccessDeniedException("Apenas o Admin pode definir o Pároco");
            }
        }

        // Duplicate check: Prevent duplicate memberships for the exact same combination of {usuarioId,
        // organizacaoId, papel, igrejaVinculoId}
        var existingMembership = membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(
                usuarioId, organizacaoId, papel.name(), igrejaVinculoId);

        if (existingMembership.isPresent()) {
            var membership = existingMembership.get();
            if (membership.isAtivo()) {
                return membership.getId();
            } else {
                membership.setAtivo(true);
                return membershipRepository.save(membership).getId();
            }
        }

        // Unicity rule: Organizations can only have 1 coordinator and 1 vice-coordinator active at a time per church
        // context
        if (papel == PapelOrganizacional.COORDENADOR || papel == PapelOrganizacional.VICE_COORDENADOR) {
            java.util.Optional<MembroOrganizacaoEntity> existing;
            if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
                existing = membershipRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                        papel.name(), igrejaVinculoId);

            } else {
                existing = membershipRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdNullAndAtivoTrue(
                        organizacaoId, papel.name());

            }
            existing.ifPresent(old -> {
                old.setAtivo(false);
                membershipRepository.save(old);

                // Transition: Demote previous coordinator to MEMBRO role
                var oldMembroOpt = membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(
                        old.getUsuarioId(), organizacaoId, "MEMBRO", igrejaVinculoId);
                if (oldMembroOpt.isPresent()) {
                    var m = oldMembroOpt.get();
                    m.setAtivo(true);
                    membershipRepository.save(m);
                } else {
                    MembroOrganizacaoEntity demoted = new MembroOrganizacaoEntity();
                    demoted.setId(UUID.randomUUID());
                    demoted.setUsuarioId(old.getUsuarioId());
                    demoted.setOrganizacaoId(organizacaoId);
                    demoted.setTipoOrganizacao(tipo.name());
                    demoted.setPapel("MEMBRO");
                    demoted.setIgrejaVinculoId(igrejaVinculoId);
                    demoted.setAtivo(true);
                    membershipRepository.save(demoted);
                }
            });
        }

        MembroOrganizacaoEntity entity = new MembroOrganizacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setTipoOrganizacao(tipo.name());
        entity.setPapel(papel.name());
        entity.setIgrejaVinculoId(igrejaVinculoId);
        entity.setAtivo(true);

        return membershipRepository.save(entity).getId();
    }

    private UUID resolveTargetChurchId(UUID organizacaoId, UUID igrejaVinculoId) {
        if (igrejaVinculoId != null) {
            return igrejaVinculoId;
        }
        var orgOpt = organizacaoRepository.findById(organizacaoId);
        if (orgOpt.isEmpty()) {
            return null;
        }
        var org = orgOpt.get();
        if (org.getTipo() == TipoOrganizacao.MATRIZ || org.getTipo() == TipoOrganizacao.COMUNIDADE) {
            return org.getId();
        }
        return org.getParentId();
    }
}
