package br.com.nsfatima.gestao.observabilidade.domain.exception;

public class PersistenciaAuditoriaObrigatoriaException extends RuntimeException {

    public PersistenciaAuditoriaObrigatoriaException(String message) {
        super(message);
    }

    public PersistenciaAuditoriaObrigatoriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
