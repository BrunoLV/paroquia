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

/**
 * Vincula um usuário a uma organização com um determinado {@link PapelOrganizacional}, aplicando as regras de
 * negócio que protegem a integridade da hierarquia organizacional:
 * <ul>
 *   <li>o usuário não pode ser vinculado a uma organização de outra igreja/comunidade que não seja a sua;</li>
 *   <li>pastorais e movimentos exigem uma igreja de vínculo (Matriz/Comunidade) explícita;</li>
 *   <li>definir a liderança de um conselho (CONSELHO_PASTORAL/CONSELHO_ECONOMICO/CPP/CPC) exige que quem
 *       executa a ação seja ADMIN ou PAROCO;</li>
 *   <li>definir o PAROCO do CLERO exige que quem executa a ação seja ADMIN;</li>
 *   <li>vínculos duplicados (mesma combinação usuário/organização/papel/igreja) são reativados em vez de
 *       duplicados;</li>
 *   <li>papéis de coordenação (únicos por organização/contexto de igreja) rebaixam automaticamente o
 *       coordenador anterior para um papel de membro comum ao assumir um novo coordenador.</li>
 * </ul>
 */
@Service
public class AdicionarVinculoUseCase {

    private final MembroOrganizacaoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public AdicionarVinculoUseCase(
            MembroOrganizacaoJpaRepository vinculoRepository,
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    /**
     * Cria (ou reativa, se já existir um vínculo idêntico desativado) o vínculo do usuário com a organização
     * informada, validando escopo territorial, obrigatoriedade de igreja de vínculo para pastorais/movimentos,
     * hierarquia de quem pode atribuir papéis de liderança, e rebaixando automaticamente o coordenador
     * anterior quando o papel atribuído for de coordenação exclusiva.
     *
     * @param usuarioId identificador do usuário a vincular
     * @param organizacaoId identificador da organização de destino
     * @param tipo tipo da organização de destino
     * @param papel papel organizacional a atribuir ao usuário
     * @param igrejaVinculoId identificador da igreja/comunidade de contexto; obrigatório para pastorais e
     *     movimentos
     * @return identificador do vínculo criado ou reativado
     * @throws IllegalArgumentException se o usuário não existir, pertencer a outra igreja/comunidade, ou a
     *     igreja de vínculo informada for inválida/ausente quando exigida
     * @throws org.springframework.security.access.AccessDeniedException se não houver usuário autenticado ou
     *     o usuário autenticado não tiver permissão hierárquica para atribuir o papel solicitado
     */
    @Transactional
    public UUID execute(UUID usuarioId,
                        UUID organizacaoId, TipoOrganizacao tipo, PapelOrganizacional papel, UUID igrejaVinculoId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario nao encontrado: " + usuarioId);
        }
        var usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isPresent() && usuarioOpt.get().getIgrejaId() != null) {
            UUID igrejaUsuarioId = usuarioOpt.get().getIgrejaId();
            UUID igrejaAlvoId = resolveTargetChurchId(organizacaoId, igrejaVinculoId);
            if (igrejaAlvoId != null && !igrejaUsuarioId.equals(igrejaAlvoId)) {
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

        // Regra de hierarquia: liderança de CONSELHO / CPP / CPC / CONSELHO_ECONOMICO
        // só pode ser definida por PAROCO ou ADMIN
        if ((tipo == TipoOrganizacao.CONSELHO_PASTORAL
                || tipo == TipoOrganizacao.CPP
                || tipo == TipoOrganizacao.CPC
                || tipo == TipoOrganizacao.CONSELHO_ECONOMICO)
                && (papel == PapelOrganizacional.PRESIDENTE_CPC
                || papel == PapelOrganizacional.MESA_CPP
                || papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || papel == PapelOrganizacional.COORD_PASTORAL)) {
            if (!isAdmin && !isParoco) {
                throw new AccessDeniedException(
                        "Apenas o Pároco ou Admin podem definir coordenador e vice do Conselho");
            }
        }

        // Regra de hierarquia: PAROCO só pode ser definido por ADMIN
        if (tipo == TipoOrganizacao.CLERO && papel == PapelOrganizacional.PAROCO) {
            if (!isAdmin) {
                throw new AccessDeniedException("Apenas o Admin pode definir o Pároco");
            }
        }

        // Verificação de duplicidade: evitar vínculos duplicados para exatamente a mesma combinação de {usuarioId,
        // organizacaoId, papel, igrejaVinculoId}
        var existingVinculo = vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(
                usuarioId, organizacaoId, papel.name(), igrejaVinculoId);

        if (existingVinculo.isPresent()) {
            var vinculo = existingVinculo.get();
            if (vinculo.isAtivo()) {
                return vinculo.getId();
            } else {
                vinculo.setAtivo(true);
                return vinculoRepository.save(vinculo).getId();
            }
        }

        // Regra de unicidade: organizações só podem ter 1 coordenador ativo por vez, por contexto de igreja
        boolean isPapelCoordenador = papel == PapelOrganizacional.COORD_PASTORAL_GLOBAL
                || papel == PapelOrganizacional.COORD_PASTORAL_LOCAL
                || papel == PapelOrganizacional.COORD_MOVIMENTO
                || papel == PapelOrganizacional.COORD_PASTORAL
                || papel == PapelOrganizacional.PRESIDENTE_CPC;

        if (isPapelCoordenador) {
            java.util.Optional<MembroOrganizacaoEntity> existing;
            if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
                existing = vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                        papel.name(), igrejaVinculoId);

            } else {
                existing = vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdNullAndAtivoTrue(
                        organizacaoId, papel.name());

            }
            existing.ifPresent(old -> {
                old.setAtivo(false);
                vinculoRepository.save(old);

                // Transição: rebaixar coordenador anterior para papel de membro
                String papelRebaixado =
                        tipo == TipoOrganizacao.MOVIMENTO ? "MEMBRO_MOVIMENTO" : "VOLUNTARIO_PASTORAL_LOCAL";
                var membroAnteriorOpt = vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(
                        old.getUsuarioId(), organizacaoId, papelRebaixado, igrejaVinculoId);
                if (membroAnteriorOpt.isPresent()) {
                    var m = membroAnteriorOpt.get();
                    m.setAtivo(true);
                    vinculoRepository.save(m);
                } else {
                    MembroOrganizacaoEntity demoted = new MembroOrganizacaoEntity();
                    demoted.setId(UUID.randomUUID());
                    demoted.setUsuarioId(old.getUsuarioId());
                    demoted.setOrganizacaoId(organizacaoId);
                    demoted.setTipoOrganizacao(tipo);
                    demoted.setPapel(papelRebaixado);
                    demoted.setIgrejaVinculoId(igrejaVinculoId);
                    demoted.setAtivo(true);
                    vinculoRepository.save(demoted);
                }
            });
        }

        MembroOrganizacaoEntity entity = new MembroOrganizacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setTipoOrganizacao(tipo);
        entity.setPapel(papel.name());
        entity.setIgrejaVinculoId(igrejaVinculoId);
        entity.setAtivo(true);

        return vinculoRepository.save(entity).getId();
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
