package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.domain.model.IdoneidadeAgente;
import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.domain.policy.PoliticaIdoneidadeFuncao;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Vincula o {@code Fiel} de um usuário a uma organização com um determinado {@link PapelOrganizacional},
 * aplicando as regras de negócio que protegem a integridade da hierarquia organizacional:
 * <ul>
 *   <li>pastorais e movimentos exigem uma igreja de vínculo (Matriz/Comunidade) explícita;</li>
 *   <li>vincular membros a um conselho exige que quem executa a ação seja ADMIN ou PAROCO;</li>
 *   <li>atribuir a coordenação exige o PAROCO (ou BISPO); coordenação e vice-coordenação (secretário de grupo)
 *       exigem a idoneidade do Fiel (RN-MEM-04), que já deve ser membro ativo da organização;</li>
 *   <li>definir o PAROCO do CLERO exige que quem executa a ação seja ADMIN;</li>
 *   <li>vínculos duplicados (mesma combinação fiel/organização/papel/igreja) são reativados em vez de
 *       duplicados;</li>
 *   <li>papéis de coordenação (únicos por organização/contexto de igreja) rebaixam automaticamente o
 *       coordenador anterior para um papel de membro comum ao assumir um novo coordenador.</li>
 * </ul>
 * Recebe {@code usuarioId} (mantendo o contrato externo usado por {@code /usuarios/{id}/membros}) e resolve
 * o {@code fielId} correspondente internamente, já que o vínculo em si é sempre por {@code Fiel} (ver track
 * {@code membro_grupo_por_fiel}). A checagem de escopo territorial que existia aqui ("usuário pertence a
 * outra igreja/comunidade") foi removida: dependia de {@code Usuario.igrejaId}, um dado de escopo de login
 * que não existe no Fiel — e um Fiel pode nem ter {@code Usuario}. As demais regras foram preservadas 100%.
 */
@Service
public class AdicionarVinculoUseCase {

    private final MembroGrupoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final MembroGrupoMapper membroGrupoMapper;

    public AdicionarVinculoUseCase(
            MembroGrupoJpaRepository vinculoRepository,
            UsuarioJpaRepository usuarioRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            MembroGrupoMapper membroGrupoMapper) {
        this.organizacaoRepository = organizacaoRepository;
        this.membroGrupoMapper = membroGrupoMapper;
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
    }

    /**
     * Cria (ou reativa, se já existir um vínculo idêntico desativado) o vínculo do Fiel do usuário informado
     * com a organização, validando obrigatoriedade de igreja de vínculo para pastorais/movimentos, hierarquia
     * de quem pode atribuir papéis de liderança, e rebaixando automaticamente o coordenador anterior quando
     * o papel atribuído for de coordenação exclusiva.
     *
     * @param usuarioId identificador do usuário cujo Fiel será vinculado
     * @param organizacaoId identificador da organização de destino
     * @param tipo tipo da organização de destino
     * @param papel papel organizacional a atribuir
     * @param igrejaVinculoId identificador da igreja/comunidade de contexto; obrigatório para pastorais e
     *     movimentos
     * @return identificador do vínculo criado ou reativado
     * @throws IllegalArgumentException se o usuário não existir, ou a igreja de vínculo informada for
     *     inválida/ausente quando exigida
     * @throws org.springframework.security.access.AccessDeniedException se não houver usuário autenticado ou
     *     o usuário autenticado não tiver permissão hierárquica para atribuir o papel solicitado
     */
    @Transactional
    public UUID execute(UUID usuarioId,
                        UUID organizacaoId, TipoOrganizacao tipo, PapelOrganizacional papel, UUID igrejaVinculoId) {

        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + usuarioId));
        UUID fielId = usuario.getFielId();
        if (fielId == null) {
            throw new IllegalArgumentException("Usuario nao possui vinculo com Fiel: " + usuarioId);
        }
        PapeisPorTipoOrganizacao.exigir(tipo, papel);

        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            if (igrejaVinculoId == null) {
                throw new IllegalArgumentException("Igreja de vinculo e obrigatoria para pastorais e movimentos");
            }
            var igrejaOpt = unidadeTerritorialRepository.findById(igrejaVinculoId);
            if (igrejaOpt.isEmpty()) {
                throw new IllegalArgumentException("Igreja de vinculo nao encontrada: " + igrejaVinculoId);
            }
            var igreja = igrejaOpt.get();
            if (igreja.getTipo() != TipoUnidadeTerritorial.MATRIZ
                    && igreja.getTipo() != TipoUnidadeTerritorial.COMUNIDADE) {
                throw new IllegalArgumentException("Igreja de vinculo deve ser do tipo MATRIZ ou COMUNIDADE");
            }
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Não autenticado");
        }

        boolean isParoco = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_CLERO_PAROCO".equals(a.getAuthority())
                        || "ROLE_PAROCO".equals(a.getAuthority())
                        || "ROLE_GLOBAL_PAROCO".equals(a.getAuthority())
                        || "ROLE_BISPO_DIOCESANO".equals(a.getAuthority())
                        || "ROLE_GLOBAL_BISPO_DIOCESANO".equals(a.getAuthority()));
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_GLOBAL_ADMIN".equals(a.getAuthority()));

        if (papel == PapelOrganizacional.COORDENADOR && !isParoco) {
            throw new AccessDeniedException(
                    "Apenas o Pároco possui provisão canônica para definir coordenador");
        }

        // Regra de hierarquia: vinculação a CONSELHO só pode ser definida por PAROCO ou ADMIN
        if (tipo == TipoOrganizacao.CONSELHO && !isParoco && !isAdmin) {
            throw new AccessDeniedException(
                    "Apenas o Pároco ou Admin podem definir membros e secretários do Conselho");
        }

        if (papel.exigeIdoneidadeDeCoordenacao()) {
            exigirIdoneidade(fielId, organizacaoId);
        }

        // Nota: a regra "PAROCO do CLERO só pode ser definido por ADMIN" deixou de se aplicar aqui — CLERO
        // não é mais um TipoOrganizacao (virou UnidadeTerritorial, track linguagem_ubiqua_dominio) e PAROCO virou
        // papel global atribuído via AtribuirPapelGlobalUseCase, já restrito a ADMIN (track
        // roles_globais_vs_escopo). Este caminho de vínculo nunca mais recebe tipo=CLERO nem papel=PAROCO.

        // Verificação de duplicidade: evitar vínculos duplicados para exatamente a mesma combinação de {fielId,
        // organizacaoId, papel, igrejaVinculoId}
        var existingVinculo = vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(
                fielId, organizacaoId, papel.name(), igrejaVinculoId);

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
        boolean isPapelCoordenador = papel == PapelOrganizacional.COORDENADOR;

        if (isPapelCoordenador) {
            Optional<MembroGrupoEntity> existing;
            if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
                existing = vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(
                        organizacaoId, papel.name(), igrejaVinculoId);

            } else {
                existing = vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdNullAndAtivoTrue(
                        organizacaoId, papel.name());

            }
            existing.ifPresent(old -> {
                old.setAtivo(false);
                vinculoRepository.save(old);

                // Transição: rebaixar coordenador anterior para papel de membro ativo
                String papelRebaixado = PapeisPorTipoOrganizacao.papelDeMembro(tipo, subtipoDe(organizacaoId)).name();
                var membroAnteriorOpt = vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(
                        old.getFielId(), organizacaoId, papelRebaixado, igrejaVinculoId);
                if (membroAnteriorOpt.isPresent()) {
                    var m = membroAnteriorOpt.get();
                    m.setAtivo(true);
                    vinculoRepository.save(m);
                } else {
                    MembroGrupoEntity demoted = new MembroGrupoEntity();
                    demoted.setId(UUID.randomUUID());
                    demoted.setFielId(old.getFielId());
                    demoted.setOrganizacaoId(organizacaoId);
                    demoted.setTipoOrganizacao(tipo);
                    demoted.setPapel(papelRebaixado);
                    demoted.setIgrejaVinculoId(igrejaVinculoId);
                    demoted.setAtivo(true);
                    vinculoRepository.save(demoted);
                }
            });
        }

        MembroGrupoEntity entity = new MembroGrupoEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setTipoOrganizacao(tipo);
        entity.setPapel(papel.name());
        entity.setIgrejaVinculoId(igrejaVinculoId);
        entity.setAtivo(true);

        return vinculoRepository.save(entity).getId();
    }

    /**
     * A coordenação exige idoneidade comprovada (RN-MEM-04): o Fiel precisa já ser membro ativo da organização,
     * pois é no vínculo que a formação, a vivência sacramental, o termo de proteção e o envio ficam registrados.
     */
    private void exigirIdoneidade(UUID fielId, UUID organizacaoId) {
        IdoneidadeAgente idoneidade = vinculoRepository.findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, organizacaoId)
                .map(membroGrupoMapper::toDomain)
                .map(MembroGrupo::getIdoneidade)
                .orElse(IdoneidadeAgente.vazia());
        SetorPastoral setor = organizacaoRepository.findById(organizacaoId)
                .map(OrganizacaoEntity::getSetorPastoral)
                .orElse(null);
        List<String> pendencias = PoliticaIdoneidadeFuncao.pendencias(true, setor, idoneidade, LocalDate.now());
        if (!pendencias.isEmpty()) {
            throw new IllegalArgumentException("Agente sem idoneidade para a função: " + String.join(" ", pendencias));
        }
    }

    private SubtipoConselho subtipoDe(UUID organizacaoId) {
        return organizacaoRepository.findById(organizacaoId).map(OrganizacaoEntity::getSubtipoConselho).orElse(null);
    }
}
