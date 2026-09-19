package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import org.springframework.http.HttpStatus;

/**
 * Interface selada para resultados de operações sobre eventos, garantindo tipos explícitos
 * para os desfechos de sucesso imediato e pendência de aprovação.
 */
public sealed interface ResultadoOperacaoEvento {

    HttpStatus status();

    Object corpo();

    record Sucesso(Object resposta, HttpStatus status) implements ResultadoOperacaoEvento {
        @Override
        public Object corpo() {
            return resposta;
        }
    }

    record Pendente(Object resposta, HttpStatus status) implements ResultadoOperacaoEvento {
        @Override
        public Object corpo() {
            return resposta;
        }
    }
}
