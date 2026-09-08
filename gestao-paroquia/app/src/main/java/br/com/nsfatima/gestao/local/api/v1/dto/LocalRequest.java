package br.com.nsfatima.gestao.local.api.v1.dto;

import br.com.nsfatima.gestao.local.domain.model.EnderecoLocal;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload de entrada para criação ou atualização de um Local paroquial.
 */
@Schema(description = "Dados para criação ou atualização de um Local")
public record LocalRequest(
        @Schema(description = "Nome do local", example = "Salão Paroquial São José")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 160, message = "O nome não pode exceder 160 caracteres")
        String nome,

        @Schema(description = "Tipo do local", example = "SALAO_PAROQUIAL")
        @NotBlank(message = "O tipo é obrigatório")
        @Size(max = 32, message = "O tipo não pode exceder 32 caracteres")
        String tipo,

        @Schema(description = "Capacidade máxima de pessoas", example = "200")
        @NotNull(message = "A capacidade máxima é obrigatória")
        @Positive(message = "A capacidade máxima deve ser um número positivo")
        @JsonAlias("capacidade")
        Integer capacidadeMaxima,

        @Schema(description = "Descrição detalhada e equipamentos", example = "Salão principal para festas e eventos")
        @JsonAlias("caracteristicas")
        String descricao,

        @Schema(description = "Status de ativação do local", example = "true")
        boolean ativo,

        @Schema(description = "ID da Igreja/Comunidade vinculada")
        @JsonAlias({"organizacaoId", "grupoParoquialId"})
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
    /**
     * Construtor simplificado para testes e criação rápida.
     */
    public LocalRequest(String nome, String tipo, Integer capacidadeMaxima, String descricao, boolean ativo) {
        this(nome, tipo, capacidadeMaxima, descricao, ativo, null, null, null, null, null, null, null, null);
    }

    public LocalRequest(String nome, String tipo, Integer capacidadeMaxima, String descricao, boolean ativo, UUID igrejaId) {
        this(nome, tipo, capacidadeMaxima, descricao, ativo, igrejaId, null, null, null, null, null, null, null);
    }

    public EnderecoLocal toEnderecoLocal() {
        return new EnderecoLocal(cep, logradouro, numero, complemento, bairro, cidade, uf);
    }
}
