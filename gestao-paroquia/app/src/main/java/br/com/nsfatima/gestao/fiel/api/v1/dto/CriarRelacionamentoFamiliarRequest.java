package br.com.nsfatima.gestao.fiel.api.v1.dto;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Destino familiar; o construtor legado mantém consumidores de Fiel compatíveis. */
public record CriarRelacionamentoFamiliarRequest(
        @NotNull UUID pessoaDestinoId,
        TipoPessoaFamiliar tipoPessoaDestino,
        @NotNull TipoRelacionamentoFamiliar tipo) {
    public CriarRelacionamentoFamiliarRequest(UUID fielDestinoId, TipoRelacionamentoFamiliar tipo) {
        this(fielDestinoId, TipoPessoaFamiliar.FIEL, tipo);
    }

    public UUID fielDestinoId() {
        return pessoaDestinoId;
    }

    public TipoPessoaFamiliar tipoPessoaDestinoEfetivo() {
        return tipoPessoaDestino == null ? TipoPessoaFamiliar.FIEL : tipoPessoaDestino;
    }
}

