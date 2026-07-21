package br.com.nsfatima.gestao.organizacao.domain.exception;

import java.util.UUID;

public class PastoralMovimentoOficialBusinessException extends RuntimeException {
    public PastoralMovimentoOficialBusinessException(String message) {
        super(message);
    }

    public static PastoralMovimentoOficialBusinessException notFound(UUID id) {
        return new PastoralMovimentoOficialBusinessException("Pastoral or Movimento Oficial not found with ID: " + id);
    }
}
