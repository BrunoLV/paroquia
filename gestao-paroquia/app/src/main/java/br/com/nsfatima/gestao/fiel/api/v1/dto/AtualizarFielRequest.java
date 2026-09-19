package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import java.time.LocalDate;
import java.util.UUID;

/** Contrato administrativo de substituição do cadastro civil do fiel. */
public record AtualizarFielRequest(
        String nome, Sexo sexo, LocalDate dataNascimento, String paisOrigem, String cpf, String rg,
        String tipoDocumentoEstrangeiro, String numeroDocumentoEstrangeiro, String email, String celular,
        Boolean isWhatsapp, String telefoneFixo, EnderecoDto endereco, UUID unidadeTerritorialId,
        Boolean isDizimista, Boolean permitirDivulgacaoAniversario) { }
