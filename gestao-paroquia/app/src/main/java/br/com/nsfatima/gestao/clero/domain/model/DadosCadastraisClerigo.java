package br.com.nsfatima.gestao.clero.domain.model;

import java.time.LocalDate;

/**
 * Dados civis e canônicos de um clérigo informados no cadastro/atualização. Não inclui o ciclo de
 * vida ({@code ativo}/exclusão) nem a auditoria — esses são responsabilidade do agregado {@link Clerigo}.
 */
public record DadosCadastraisClerigo(
        String nomeCivil,
        String nomeEclesiastico,
        LocalDate dataNascimento,
        String paisOrigem,
        String cpf,
        String rg,
        String documentoEstrangeiro,
        String email,
        String celular,
        boolean whatsapp,
        String telefoneFixo,
        String endereco,
        GrauOrdem grauOrdem,
        String dioceseIncardinacao,
        LocalDate dataOrdenacaoDiaconal,
        LocalDate dataOrdenacaoPresbiteral,
        LocalDate dataOrdenacaoEpiscopal) {
}
