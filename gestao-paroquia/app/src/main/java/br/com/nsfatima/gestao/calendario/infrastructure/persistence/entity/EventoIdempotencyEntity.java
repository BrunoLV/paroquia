package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Registra chaves de idempotência de requisições de criação de evento, permitindo que uma
 * repetição da mesma requisição (mesma chave) retorne a resposta original em vez de criar
 * um evento duplicado — essencial para clientes que reenviam requisições após timeout ou
 * falha de rede sem risco de duplicar agendamentos.
 */
@Entity
@Table(name = "evento_idempotency", schema = "calendario")
public class EventoIdempotencyEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    /** Hash do corpo da requisição original; usado para detectar reuso indevido da mesma chave com payload diferente.
     * */
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    /** Código de status HTTP da resposta original, devolvido novamente em requisições repetidas com a mesma chave. */
    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }
}
