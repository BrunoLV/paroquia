package br.com.nsfatima.gestao.conselho.domain.repository;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.NumeradorAta;

import java.util.Optional;

/**
 * Porta de persistência do agregado {@link AtaReuniao}. Também é o {@link NumeradorAta}: a série numérica
 * pertence ao armazenamento, que a serializa por organização e ano.
 */
public interface AtaRepository extends NumeradorAta {

    Optional<AtaReuniao> buscarPorId(AtaReuniaoId id);

    AtaReuniao salvar(AtaReuniao ata);

    /**
     * Lista atas conforme o filtro, da reunião mais recente para a mais antiga.
     *
     * @param filtro critérios e escopo de leitura
     * @param pagina índice da página, iniciado em zero
     * @param tamanho quantidade de atas por página
     * @return a página e o total que atende ao filtro
     */
    PaginaAtas listar(FiltroAtas filtro, int pagina, int tamanho);
}
