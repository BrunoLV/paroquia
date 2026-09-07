package br.com.nsfatima.gestao.fiel.api.v1.dto;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
public record CriarRelacionamentoFamiliarRequest(@NotNull UUID fielDestinoId,@NotNull TipoRelacionamentoFamiliar tipo) { }
