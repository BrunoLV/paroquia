package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Restringe quais unidades territoriais podem ser donas de ativos patrimoniais: apenas MATRIZ ou
 * COMUNIDADE — grupos paroquiais (pastorais, movimentos, conselhos) não possuem patrimônio próprio. Desde a
 * track {@code linguagem_ubiqua_dominio}, MATRIZ/COMUNIDADE são exclusivamente {@code UnidadeTerritorial},
 * nunca {@code GrupoParoquial} — a propriedade de um ativo é sempre sobre uma unidade territorial. Aplicada
 * tanto no cadastro quanto na transferência de posse de um ativo.
 */
@Service
public class AtivoGrupoParoquialPolicy {

    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    public AtivoGrupoParoquialPolicy(UnidadeTerritorialJpaRepository unidadeTerritorialRepository) {
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
    }

    /**
     * Valida que a unidade territorial informada existe e é de um tipo elegível para possuir patrimônio.
     *
     * @param grupoParoquialId identificador da unidade territorial candidata a proprietária do ativo
     * @throws AtivoGrupoParoquialInvalidaException se a unidade não existir ou não for MATRIZ/COMUNIDADE
     */
    public void assertGrupoParoquialProprietariaPermitida(UUID grupoParoquialId) {
        var unidade = unidadeTerritorialRepository.findById(grupoParoquialId)
                .orElseThrow(() -> new AtivoGrupoParoquialInvalidaException(
                        "Unidade territorial não encontrada: " + grupoParoquialId));

        var tipo = unidade.getTipo();
        if (tipo != TipoUnidadeTerritorial.MATRIZ && tipo != TipoUnidadeTerritorial.COMUNIDADE) {
            throw new AtivoGrupoParoquialInvalidaException(
                    "Ativos patrimoniais só podem pertencer a unidades territoriais do tipo MATRIZ ou COMUNIDADE");
        }
    }
}
