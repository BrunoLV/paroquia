package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/** Contrato administrativo para criação de cadastro civil do fiel. */
public record CriarFielRequest(
        @NotBlank String nome,
        @NotNull Sexo sexo,
        @NotNull LocalDate dataNascimento,
        @NotBlank String paisOrigem,
        String cpf,
        String rg,
        String tipoDocumentoEstrangeiro,
        String numeroDocumentoEstrangeiro,
        @NotBlank String email,
        @NotBlank String celular,
        boolean isWhatsapp,
        String telefoneFixo,
        @NotNull EnderecoDto endereco,
        @NotNull UUID unidadeTerritorialId,
        boolean isDizimista,
        boolean permitirDivulgacaoAniversario,
        boolean termoConsentimentoAceito,
        boolean usoImagemAutorizado) {

    public CriarFielRequest(
            String nome,
            Sexo sexo,
            LocalDate dataNascimento,
            String paisOrigem,
            String cpf,
            String rg,
            String tipoDocumentoEstrangeiro,
            String numeroDocumentoEstrangeiro,
            String email,
            String celular,
            boolean isWhatsapp,
            String telefoneFixo,
            EnderecoDto endereco,
            UUID unidadeTerritorialId,
            boolean isDizimista,
            boolean permitirDivulgacaoAniversario) {
        this(
                nome,
                sexo,
                dataNascimento,
                paisOrigem,
                cpf,
                rg,
                tipoDocumentoEstrangeiro,
                numeroDocumentoEstrangeiro,
                email,
                celular,
                isWhatsapp,
                telefoneFixo,
                endereco,
                unidadeTerritorialId,
                isDizimista,
                permitirDivulgacaoAniversario,
                false,
                false);
    }

    public CriarFielRequest(
            String nome,
            String cpf,
            LocalDate nascimento,
            String email,
            String telefone,
            String endereco,
            Vocacao vocacao,
            Boolean batizado,
            Boolean primeira,
            Boolean crisma,
            Boolean casamento) {
        this(
                nome,
                Sexo.MASCULINO,
                nascimento,
                "BRA",
                cpf,
                null,
                null,
                null,
                email,
                telefone,
                true,
                null,
                new EnderecoDto(null, endereco, null, null, null, null, null),
                UUID.fromString("00000000-0000-0000-0000-000000000100"),
                false,
                false);
    }
}
