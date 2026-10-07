package br.com.nsfatima.gestao.dashboard.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardComunidadeDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.RetencaoPastoralDTO;
import br.com.nsfatima.gestao.dashboard.application.service.EscopoDashboardResolver;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Calcula a seção de vida comunitária do dashboard: pastorais/movimentos/ministérios em situação
 * de atenção quanto à retenção — número crítico de voluntários ativos (menos de
 * {@value #MINIMO_VOLUNTARIOS}) e/ou sem coordenador designado. Só os grupos em alerta entram no
 * resultado.
 */
@Service
public class ObterRetencaoPastoraisUseCase {

    private static final int MINIMO_VOLUNTARIOS = 3;
    private static final Set<TipoOrganizacao> TIPOS_COMUNITARIOS =
            EnumSet.of(TipoOrganizacao.PASTORAL, TipoOrganizacao.MOVIMENTO, TipoOrganizacao.MINISTERIO);

    private final OrganizacaoJpaRepository grupoRepository;
    private final MembroGrupoJpaRepository membroRepository;
    private final EscopoDashboardResolver escopoResolver;

    public ObterRetencaoPastoraisUseCase(
            OrganizacaoJpaRepository grupoRepository,
            MembroGrupoJpaRepository membroRepository,
            EscopoDashboardResolver escopoResolver) {
        this.grupoRepository = grupoRepository;
        this.membroRepository = membroRepository;
        this.escopoResolver = escopoResolver;
    }

    /**
     * Monta o índice de retenção de pastorais para o ator e a organização informados.
     *
     * @param actorContext contexto do ator autenticado
     * @param organizacaoIdSolicitado organização a filtrar, ou {@code null}
     * @return grupos em situação de atenção, do mais crítico ao menos
     * @throws org.springframework.security.access.AccessDeniedException se o ator não puder ver a
     *     organização solicitada
     */
    @Transactional(readOnly = true)
    public DashboardComunidadeDTO execute(EventoActorContext actorContext, UUID organizacaoIdSolicitado) {
        Optional<UUID> escopo = escopoResolver.resolver(actorContext, organizacaoIdSolicitado);
        List<RetencaoPastoralDTO> alertas = grupoRepository.findByTipoInAndAtivoTrue(TIPOS_COMUNITARIOS).stream()
                .filter(grupo -> escopo.isEmpty() || escopo.get().equals(grupo.getId()))
                .map(this::avaliar)
                .filter(dto -> !dto.motivos().isEmpty())
                .sorted(Comparator.comparingLong(RetencaoPastoralDTO::totalVoluntarios))
                .toList();
        return new DashboardComunidadeDTO(alertas);
    }

    private RetencaoPastoralDTO avaliar(OrganizacaoEntity grupo) {
        long total = membroRepository.countByOrganizacaoIdAndAtivoTrue(grupo.getId());
        boolean temLideranca = grupo.getCoordenador() != null && !grupo.getCoordenador().isBlank();
        List<String> motivos = new ArrayList<>();
        if (total < MINIMO_VOLUNTARIOS) {
            motivos.add("Menos de " + MINIMO_VOLUNTARIOS + " voluntários ativos");
        }
        if (!temLideranca) {
            motivos.add("Sem liderança designada");
        }
        return new RetencaoPastoralDTO(grupo.getId(), grupo.getNome(), total, temLideranca, motivos);
    }
}
