package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Conteúdo documental do assento. Em rascunho, qualquer campo pode estar pendente até a homologação. */
public record TeorAssentoRequest(
        @Size(max = 32) String livro,
        @Size(max = 32) String folha,
        @Size(max = 32) String termo,
        @Size(max = 255) String paroquiaCustodiante,
        LocalDateTime dataCelebracao,
        @Size(max = 255) String localCelebracao,
        @Valid List<ParticipanteRequest> participantes,
        Map<String, String> dadosEspecificos,
        Boolean dadoSensivel) {
}
