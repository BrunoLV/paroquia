package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.GrupoParoquial;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;

import java.util.UUID;

/**
 * Política de negócio da hierarquia organizacional da paróquia: qual {@link TipoGrupo} pode ter qual
 * tipo de organização-mãe e qual vínculo é obrigatório com o catálogo oficial de Pastorais/Movimentos.
 *
 * <p>Antes vivia como o bloco privado {@code validateHierarchy} de {@code GrupoParoquialService} (62
 * linhas de invariante misturadas à orquestração). Isolada aqui, é testável sozinha e reutilizável.</p>
 */
public class ValidadorHierarquiaOrganizacional {

    private final GrupoParoquialRepository grupoParoquialRepository;
    private final PastoralMovimentoOficialRepository pastoralOficialRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    public ValidadorHierarquiaOrganizacional(
            GrupoParoquialRepository grupoParoquialRepository,
            PastoralMovimentoOficialRepository pastoralOficialRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository) {
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.pastoralOficialRepository = pastoralOficialRepository;
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
    }

    /**
     * Valida que o vínculo (tipo, organização-mãe, pastoral/movimento oficial) respeita a hierarquia.
     *
     * @param id identificador da própria organização ({@code null} na criação) — impede auto-parentesco
     * @throws GrupoParoquialNegocioException se a hierarquia informada for inválida
     */
    public void validar(UUID id, TipoGrupo tipo, UUID parentId, UUID pastoralOficialId) {
        if (parentId != null && parentId.equals(id)) {
            throw new GrupoParoquialNegocioException("Uma organização não pode ser sua própria organização-mãe.");
        }

        if (tipo == TipoGrupo.CONSELHO) {
            validarConselho(tipo, parentId);
            return;
        }

        if (tipo == TipoGrupo.PASTORAL || tipo == TipoGrupo.MOVIMENTO) {
            validarPastoralOuMovimento(tipo, parentId, pastoralOficialId);
        }

        // MINISTERIO não exige organização-mãe (comportamento herdado do antigo tipo OUTRO)
    }

    private void validarConselho(TipoGrupo tipo, UUID parentId) {
        if (parentId == null) {
            throw new GrupoParoquialNegocioException(tipo.name() + " deve ter uma organização-mãe.");
        }
        var unidade = unidadeTerritorialRepository.findById(parentId)
                .orElseThrow(() -> new GrupoParoquialNegocioException(
                        "Organização-mãe não encontrada com ID: " + parentId));
        if (!unidade.isAtivo()) {
            throw new GrupoParoquialNegocioException("Organização-mãe não está ativa.");
        }
        if (unidade.getTipo() != TipoUnidadeTerritorial.MATRIZ
                && unidade.getTipo() != TipoUnidadeTerritorial.COMUNIDADE) {
            throw new GrupoParoquialNegocioException(
                    tipo.name() + " deve ter uma organização-mãe do tipo MATRIZ ou COMUNIDADE.");
        }
    }

    private void validarPastoralOuMovimento(TipoGrupo tipo, UUID parentId, UUID pastoralOficialId) {
        if (parentId == null) {
            throw new GrupoParoquialNegocioException(tipo.name() + " deve ter uma organização-mãe.");
        }
        if (pastoralOficialId == null) {
            throw new GrupoParoquialNegocioException("Vínculo com Pastoral ou Movimento Oficial é obrigatório.");
        }
        PastoralMovimentoOficial oficial = pastoralOficialRepository.findById(pastoralOficialId)
                .orElseThrow(() -> new GrupoParoquialNegocioException(
                        "Pastoral ou Movimento Oficial não encontrado com ID: " + pastoralOficialId));
        if (oficial.getTipo() != tipo) {
            throw new GrupoParoquialNegocioException(
                    "O tipo da Pastoral/Movimento Oficial (" + oficial.getTipo()
                            + ") não coincide com o tipo da organização (" + tipo + ").");
        }

        GrupoParoquial parent = grupoParoquialRepository.findById(parentId)
                .orElseThrow(() -> new GrupoParoquialNegocioException(
                        "Organização-mãe não encontrada com ID: " + parentId));
        if (!parent.isAtivo()) {
            throw new GrupoParoquialNegocioException("Organização-mãe não está ativa.");
        }
        if (parent.getTipo() != TipoGrupo.CONSELHO) {
            throw new GrupoParoquialNegocioException(
                    tipo.name() + " deve ter uma organização-mãe do tipo CONSELHO.");
        }
    }
}
