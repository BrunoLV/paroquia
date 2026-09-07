package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra uma nova observação ou nota descritiva associada a um evento da agenda.
 */
@Service
public class CreateObservacaoUseCase {

    /**
     * Instantiates a transient observation response object, primarily used for returning newly created data
     * structures in a consistent format.
     * <p>
     * Usage Example:
     * {@code
     * useCase.execute(eventoId, usuarioId, TipoObservacaoInput.SISTEMA, "Log entry");
     * }
     */
    public ObservacaoResponse execute(UUID eventoId, UUID usuarioId, TipoObservacaoInput tipo, String conteudo) {
        return new ObservacaoResponse(
                UUID.randomUUID(),
                eventoId,
                usuarioId,
                TipoObservacaoResponse.fromInput(tipo),
                conteudo,
                Instant.now());
    }
}
