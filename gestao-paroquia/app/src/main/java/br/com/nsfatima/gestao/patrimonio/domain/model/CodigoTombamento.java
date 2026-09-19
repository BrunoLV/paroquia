package br.com.nsfatima.gestao.patrimonio.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object do código de tombamento patrimonial. É gerado exclusivamente pelo sistema no ato do
 * cadastro ({@code GeradorNumeroTombamento}) no padrão sequencial anual {@code PAT-AAAA-NNNN} e é
 * terminantemente imutável — a inserção ou edição manual é vedada tanto na interface quanto nas
 * camadas de validação. Validar o formato aqui garante que nenhum caminho de persistência aceite um
 * código fora do padrão.
 */
public record CodigoTombamento(String valor) implements Serializable {

    private static final Pattern PADRAO = Pattern.compile("^PAT-\\d{4}-\\d{4,}$");

    public CodigoTombamento {
        Objects.requireNonNull(valor, "O código de tombamento não pode ser nulo.");
        if (!PADRAO.matcher(valor).matches()) {
            throw new IllegalArgumentException(
                    "Código de tombamento fora do padrão PAT-AAAA-NNNN: " + valor);
        }
    }

    public static CodigoTombamento de(int ano, int sequencial) {
        return new CodigoTombamento("PAT-%d-%04d".formatted(ano, sequencial));
    }

    @Override
    public String toString() {
        return valor;
    }
}
