package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;

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
     * Valida que o vínculo (tipo, organização-mãe, pastoral/movimento oficial) respeita a hierarquia.
     *
     * @param id identificador da própria organização ({@code null} na criação) — impede auto-parentesco
     * @throws OrganizacaoNegocioException se a hierarquia informada for inválida
     */
    public void validar(UUID id, TipoOrganizacao tipo, UUID parentId, UUID pastoralOficialId) {
        if (parentId != null && parentId.equals(id)) {
            throw new OrganizacaoNegocioException("Uma organização não pode ser sua própria organização-mãe.");
        }

        if (tipo == TipoOrganizacao.CONSELHO) {
            validarConselho(tipo, parentId);
            return;
        }

        if (tipo == TipoOrganizacao.PASTORAL || tipo == TipoOrganizacao.MOVIMENTO) {
            validarPastoralOuMovimento(tipo, parentId, pastoralOficialId);
        }

        // MINISTERIO não exige organização-mãe (comportamento herdado do antigo tipo OUTRO)
    }

    private void validarConselho(TipoOrganizacao tipo, UUID parentId) {
        if (parentId == null) {
            throw new OrganizacaoNegocioException(tipo.name() + " deve ter uma organização-mãe.");
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

    private void validarPastoralOuMovimento(TipoOrganizacao tipo, UUID parentId, UUID pastoralOficialId) {
        if (parentId == null) {
            throw new OrganizacaoNegocioException(tipo.name() + " deve ter uma organização-mãe.");
        }
        if (pastoralOficialId == null) {
            throw new OrganizacaoNegocioException("Vínculo com Pastoral ou Movimento Oficial é obrigatório.");
        }
        PastoralMovimentoOficial oficial = pastoralOficialRepository.findById(pastoralOficialId)
                .orElseThrow(() -> new OrganizacaoNegocioException(
                        "Pastoral ou Movimento Oficial não encontrado com ID: " + pastoralOficialId));
        if (oficial.getTipo() != tipo) {
            throw new OrganizacaoNegocioException(
                    "O tipo da Pastoral/Movimento Oficial (" + oficial.getTipo()
                            + ") não coincide com o tipo da organização (" + tipo + ").");
        }

        Organizacao parent = organizacaoRepository.findById(parentId)
                .orElseThrow(() -> new OrganizacaoNegocioException(
                        "Organização-mãe não encontrada com ID: " + parentId));
        if (!parent.isAtivo()) {
            throw new OrganizacaoNegocioException("Organização-mãe não está ativa.");
        }
        if (parent.getTipo() != TipoOrganizacao.CONSELHO) {
            throw new OrganizacaoNegocioException(
                    tipo.name() + " deve ter uma organização-mãe do tipo CONSELHO.");
        }
    }
}
