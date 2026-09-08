package br.com.nsfatima.gestao.local.api.v1.dto;

import br.com.nsfatima.gestao.local.domain.model.EnderecoLocal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload de entrada para criação ou atualização de um Local.
 *
 * <p>Suporta tanto a nomenclatura legada (capacidade, caracteristicas, grupoParoquialId) quanto a nova
 * nomenclatura de domínio (capacidadeMaxima, descricao, igrejaId) e os campos estruturados de endereço.
 */
@Schema(description = "Dados para criação ou atualização de um Local")
public record LocalRequest(
        @Schema(description = "Nome do local", example = "Salão Paroquial São José")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 160, message = "O nome não pode exceder 160 caracteres")
        String nome,

        @Schema(description = "Tipo do local", example = "SALÃO")
        @NotBlank(message = "O tipo é obrigatório")
        @Size(max = 32, message = "O tipo não pode exceder 32 caracteres")
        String tipo,

        @Schema(description = "Endereço textual legado do local", example = "Rua das Flores, 123")
        @Size(max = 500, message = "O endereço não pode exceder 500 caracteres")
        String endereco,

        @Schema(description = "Capacidade máxima legada de pessoas", example = "200")
        Integer capacidade,

        @Schema(description = "Capacidade máxima de pessoas", example = "200")
        @Positive(message = "A capacidade máxima deve ser um número positivo")
        Integer capacidadeMaxima,

        @Schema(description = "Características adicionais legadas", example = "Ar condicionado, Projetor")
        String caracteristicas,

        @Schema(description = "Descrição detalhada e equipamentos", example = "Salão principal para festas e eventos")
        String descricao,

        @Schema(description = "Status do local", example = "true")
        boolean ativo,

        @Schema(description = "ID da organização (Matriz ou Comunidade) de subordinação")
        UUID grupoParoquialId,

        @Schema(description = "ID da Igreja/Comunidade vinculada")
        UUID igrejaId,

        @Schema(description = "CEP formatado", example = "01001-000")
        @Size(max = 9, message = "CEP não pode exceder 9 caracteres")
        String cep,

        @Schema(description = "Logradouro do local", example = "Praça da Sé")
        @Size(max = 150, message = "Logradouro não pode exceder 150 caracteres")
        String logradouro,

        @Schema(description = "Número predial", example = "100")
        @Size(max = 20, message = "Número não pode exceder 20 caracteres")
        String numero,

        @Schema(description = "Complemento", example = "Bloco B")
        @Size(max = 100, message = "Complemento não pode exceder 100 caracteres")
        String complemento,

        @Schema(description = "Bairro", example = "Centro")
        @Size(max = 100, message = "Bairro não pode exceder 100 caracteres")
        String bairro,

        @Schema(description = "Cidade", example = "São Paulo")
        @Size(max = 100, message = "Cidade não pode exceder 100 caracteres")
        String cidade,

        @Schema(description = "Unidade Federativa (UF)", example = "SP")
        @Size(max = 2, message = "UF deve ter no máximo 2 caracteres")
        String uf
) {
    public LocalRequest(String nome, String tipo, String endereco, Integer capacidade,
                        String caracteristicas, boolean ativo) {
        this(nome, tipo, endereco, capacidade, capacidade, caracteristicas, caracteristicas, ativo,
                null, null, null, null, null, null, null, null, null);
    }

    public LocalRequest(String nome, String tipo, String endereco, Integer capacidade,
                        String caracteristicas, boolean ativo, UUID grupoParoquialId) {
        this(nome, tipo, endereco, capacidade, capacidade, caracteristicas, caracteristicas, ativo,
                grupoParoquialId, grupoParoquialId, null, null, null, null, null, null, null);
    }

    public Integer resolvedCapacidadeMaxima() {
        if (capacidadeMaxima != null) {
            return capacidadeMaxima;
        }
        return capacidade;
    }

    public String resolvedDescricao() {
        if (descricao != null) {
            return descricao;
        }
        return caracteristicas;
    }

    public UUID resolvedIgrejaId() {
        if (igrejaId != null) {
            return igrejaId;
        }
        return grupoParoquialId;
    }

    public EnderecoLocal toEnderecoLocal() {
        return new EnderecoLocal(cep, logradouro, numero, complemento, bairro, cidade, uf);
    }
}
