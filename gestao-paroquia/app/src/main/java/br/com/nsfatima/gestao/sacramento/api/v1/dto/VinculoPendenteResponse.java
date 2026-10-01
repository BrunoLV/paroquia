package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import java.util.UUID;

/**
 * Vínculo proposto pela Secretaria aguardando decisão do Pároco na tela de Aprovações.
 *
 * @param tipoPessoa {@code FIEL} ou {@code CLERIGO}
 * @param pessoaNome nome civil do cadastro proposto, para conferência com o nome atestado no termo
 */
public record VinculoPendenteResponse(
        UUID assentoId,
        String tipoSacramento,
        String sujeito,
        String livro,
        String folha,
        String termo,
        UUID participanteId,
        String papel,
        String nomeHistorico,
        String tipoPessoa,
        UUID pessoaId,
        String pessoaNome) {
}
