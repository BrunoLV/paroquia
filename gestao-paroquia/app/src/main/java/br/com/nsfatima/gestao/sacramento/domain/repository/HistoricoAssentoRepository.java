package br.com.nsfatima.gestao.sacramento.domain.repository;

import br.com.nsfatima.gestao.sacramento.domain.model.HistoricoEntrada;

import java.util.List;
import java.util.UUID;

/** Porta append-only da trilha estrita do assento: inclui e consulta, nunca altera ou remove. */
public interface HistoricoAssentoRepository {

    void registrar(HistoricoEntrada entrada);

    List<HistoricoEntrada> listarPorAssento(UUID assentoId);
}
