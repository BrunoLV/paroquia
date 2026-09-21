package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Associação (tabela de junção) entre um {@link EventoEntity} e um espaço físico/local alocado,
 * permitindo que eventos como retiros, assembleias e festividades ocupem múltiplos locais
 * simultaneamente (ex.: Igreja Principal e Salão Paroquial).
 */
@Entity
@Table(name = "eventos_locais", schema = "calendario")
@IdClass(EventoLocalEntity.Key.class)
public class EventoLocalEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Id
    @Column(name = "local_id", nullable = false)
    private UUID localId;

    public EventoLocalEntity() {
    }

    public EventoLocalEntity(UUID eventoId, UUID localId) {
        this.eventoId = Objects.requireNonNull(eventoId, "eventoId não deve ser nulo");
        this.localId = Objects.requireNonNull(localId, "localId não deve ser nulo");
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public UUID getLocalId() {
        return localId;
    }

    public void setLocalId(UUID localId) {
        this.localId = localId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventoLocalEntity that = (EventoLocalEntity) o;
        return Objects.equals(eventoId, that.eventoId) && Objects.equals(localId, that.localId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventoId, localId);
    }

    /**
     * Chave primária composta para a associação {@link EventoLocalEntity}.
     */
    public static class Key implements Serializable {
        private UUID eventoId;
        private UUID localId;

        public Key() {
        }

        public Key(UUID eventoId, UUID localId) {
            this.eventoId = eventoId;
            this.localId = localId;
        }

        public UUID getEventoId() {
            return eventoId;
        }

        public void setEventoId(UUID eventoId) {
            this.eventoId = eventoId;
        }

        public UUID getLocalId() {
            return localId;
        }

        public void setLocalId(UUID localId) {
            this.localId = localId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(eventoId, key.eventoId) && Objects.equals(localId, key.localId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventoId, localId);
        }
    }
}
