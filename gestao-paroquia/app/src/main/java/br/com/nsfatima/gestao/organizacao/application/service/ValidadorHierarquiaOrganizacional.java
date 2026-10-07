package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.policy.ContextoTipologia;
import br.com.nsfatima.gestao.organizacao.domain.policy.RegrasTipologiaOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Política de negócio da hierarquia organizacional da paróquia: qual {@link TipoOrganizacao} pode ter qual
 * tipo de organização-mãe e qual vínculo é obrigatório com o catálogo oficial de Pastorais/Movimentos.
 *
 * <p>Antes vivia como o bloco privado {@code validateHierarchy} de {@code OrganizacaoService} (62
 * linhas de invariante misturadas à orquestração). Isolada aqui, é testável sozinha e reutilizável.</p>
 */
public class ValidadorHierarquiaOrganizacional {

    private final OrganizacaoRepository organizacaoRepository;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    public ValidadorHierarquiaOrganizacional(
            OrganizacaoRepository organizacaoRepository,
            PastoralMovimentoOficialRepository pastoralOficialRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository) {
        this.organizacaoRepository = organizacaoRepository;
        this.pastoralOficialRepository = pastoralOficialRepository;
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
    }

    /**
     * Valida o vínculo (tipo, organização-mãe, catálogo oficial) e a classificação eclesial (múnus e
     * subtipo de conselho). Carrega os dados e delega as invariantes por tipo às
     * {@link RegrasTipologiaOrganizacao}.
     *
     * @param id identificador da própria organização ({@code null} na criação) — impede auto-parentesco
     * @throws OrganizacaoNegocioException se a organização informada violar a tipologia ou a hierarquia
     */
    public void validar(UUID id, TipoOrganizacao tipo, UUID parentId, UUID pastoralOficialId,
                        SetorPastoral setorPastoral, SubtipoConselho subtipoConselho) {
        if (parentId != null && parentId.equals(id)) {
            throw new OrganizacaoNegocioException("Uma organização não pode ser sua própria organização-mãe.");
        }

        PastoralMovimentoOficial oficial = pastoralOficialId == null ? null
                : pastoralOficialRepository.findById(pastoralOficialId)
                        .orElseThrow(() -> new OrganizacaoNegocioException(
                                "Pastoral ou Movimento Oficial não encontrado com ID: " + pastoralOficialId));

        if (tipo == TipoOrganizacao.CONSELHO) {
            validarMaeTerritorial(tipo, parentId);
        }
        Organizacao mae = tipo == TipoOrganizacao.CONSELHO || parentId == null ? null
                : organizacaoRepository.findById(parentId).orElse(null);
        if (tipo != TipoOrganizacao.CONSELHO && parentId != null && mae == null) {
            throw new OrganizacaoNegocioException("Organização-mãe não encontrada com ID: " + parentId);
        }

        List<String> violacoes = RegrasTipologiaOrganizacao.avaliar(tipo,
                new ContextoTipologia(setorPastoral, subtipoConselho, oficial, mae, parentId != null));
        if (!violacoes.isEmpty()) {
            throw new OrganizacaoNegocioException(String.join(" ", violacoes));
        }
    }

    private void validarMaeTerritorial(TipoOrganizacao tipo, UUID parentId) {
        if (parentId == null) {
            return;
        }
        var unidade = unidadeTerritorialRepository.findById(parentId)
                .orElseThrow(() -> new OrganizacaoNegocioException(
                        "Organização-mãe não encontrada com ID: " + parentId));
        if (!unidade.isAtivo()) {
            throw new OrganizacaoNegocioException("Organização-mãe não está ativa.");
        }
        if (unidade.getTipo() != TipoUnidadeTerritorial.MATRIZ
                && unidade.getTipo() != TipoUnidadeTerritorial.COMUNIDADE) {
            throw new OrganizacaoNegocioException(
                    tipo.name() + " deve ter uma organização-mãe do tipo MATRIZ ou COMUNIDADE.");
        }
    }
}
