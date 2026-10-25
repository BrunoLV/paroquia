package br.com.nsfatima.gestao.fiel.api.v1.dto;

import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import java.time.LocalDate;
import java.util.UUID;

/** Resposta de consulta com PII formatada para a interface autorizada. */
public record FielResponse(UUID fielId, String nome, Sexo sexo, LocalDate dataNascimento,
        IdentificacaoDto dadosIdentificacao, ContatoDto contato, EnderecoDto endereco,
        IgrejaFrequentaDto igrejaFrequenta, boolean isDizimista,
        boolean permitirDivulgacaoAniversario, boolean isAtivo,
        ConsentimentoLgpdDto consentimento) implements FielConsultaResponse {

    public FielResponse(UUID fielId, String nome, Sexo sexo, LocalDate dataNascimento,
            IdentificacaoDto dadosIdentificacao, ContatoDto contato, EnderecoDto endereco,
            IgrejaFrequentaDto igrejaFrequenta, boolean isDizimista,
            boolean permitirDivulgacaoAniversario, boolean isAtivo) {
        this(fielId, nome, sexo, dataNascimento, dadosIdentificacao, contato, endereco,
                igrejaFrequenta, isDizimista, permitirDivulgacaoAniversario, isAtivo, null);
    }

    public UUID id() {
        return fielId;
    }
}
