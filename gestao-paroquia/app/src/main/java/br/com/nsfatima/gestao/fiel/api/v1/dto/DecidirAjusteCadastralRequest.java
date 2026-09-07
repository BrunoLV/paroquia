package br.com.nsfatima.gestao.fiel.api.v1.dto;
import jakarta.validation.constraints.NotBlank;
public record DecidirAjusteCadastralRequest(boolean aprovar, @NotBlank String justificativa) { }
