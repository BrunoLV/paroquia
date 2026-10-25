package br.com.nsfatima.gestao.conselho.api.v1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import java.util.List;

/** Presença da reunião: lista nominal ou apenas o total de participantes, conforme o tipo de reunião. */
public record PresencaRequest(List<@Valid PresencaItemRequest> nominal, @Min(0) Integer total) {
}
