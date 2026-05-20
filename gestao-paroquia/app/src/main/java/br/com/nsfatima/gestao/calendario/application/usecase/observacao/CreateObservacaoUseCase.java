package br.com.nsfatima.gestao.calendario.application.usecase.observacao;

import java.util.UUID;
import java.time.Instant;
import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoResponse;
import org.springframework.stereotype.Service;

@Service
public class CreateObservacaoUseCase {

    /**
     * Instantiates a transient observation response object, primarily used for returning newly created data structures in a consistent format.
     * 
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
