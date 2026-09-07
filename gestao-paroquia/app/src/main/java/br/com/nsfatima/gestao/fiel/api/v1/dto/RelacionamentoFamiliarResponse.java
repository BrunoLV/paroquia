package br.com.nsfatima.gestao.fiel.api.v1.dto;
import java.util.UUID;
public record RelacionamentoFamiliarResponse(UUID id,UUID fielId,String nome,String relacao) { }
