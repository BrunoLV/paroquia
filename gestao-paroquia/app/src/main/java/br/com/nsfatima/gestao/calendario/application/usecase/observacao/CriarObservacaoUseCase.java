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
public class CriarObservacaoUseCase {

    /**
     * Registra uma nova observação sobre um evento, retornando a estrutura de dados recém-criada em formato
     * consistente.
     * <p>
     * Exemplo de uso:
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
