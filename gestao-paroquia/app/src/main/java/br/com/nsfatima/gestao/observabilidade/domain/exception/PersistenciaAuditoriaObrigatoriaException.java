package br.com.nsfatima.gestao.observabilidade.domain.exception;

/**
 * Sinaliza que uma tentativa de persistir um registro de auditoria falhou de forma irrecuperável.
 * Regra de negócio: registros de auditoria elegíveis (ver
 * {@code AuditoriaPersistenceService#isAuditableAction}) são obrigatórios para rastreabilidade;
 * a falha de persistência não deve ser silenciada e é propagada como exceção não checada para
 * que a operação de negócio original também falhe, evitando lacunas na trilha de auditoria.
 */
public class PersistenciaAuditoriaObrigatoriaException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem descritiva do motivo da falha de persistência.
     *
     * @param message descrição do erro de persistência
     */
    public PersistenciaAuditoriaObrigatoriaException(String message) {
        super(message);
    }

    /**
     * Cria a exceção encapsulando a causa raiz da falha de persistência (tipicamente uma
     * exceção de infraestrutura/JPA), preservando a stack trace original para diagnóstico.
     *
     * @param message descrição do erro de persistência
     * @param cause exceção original que causou a falha
     */
    public PersistenciaAuditoriaObrigatoriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
