package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento sem projeto vinculado dentro da janela do filtro. Quando há um projeto ativo da mesma
 * organização responsável cuja vigência cobre o início do evento, esse projeto é apresentado
 * como <em>sugestão</em> de vínculo ({@code projetoSugerido*} preenchidos) — nunca um vínculo
 * automático; a decisão é sempre do coordenador.
 *
 * @param eventoId identificador do evento órfão
 * @param titulo título do evento
 * @param inicio início do evento
 * @param projetoSugeridoId projeto ativo candidato ao vínculo, ou {@code null}
 * @param projetoSugeridoNome nome do projeto candidato, ou {@code null}
 */
@Schema(description = "Evento sem projeto vinculado, com eventual sugestão de vínculo")
public record EventoOrfaoDTO(
        UUID eventoId,
        String titulo,
        LocalDateTime inicio,
        UUID projetoSugeridoId,
        String projetoSugeridoNome
) {
}
