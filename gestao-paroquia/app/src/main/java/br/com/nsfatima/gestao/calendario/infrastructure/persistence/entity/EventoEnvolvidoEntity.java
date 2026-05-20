package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "eventos_envolvidos", schema = "calendario")
@IdClass(EventoEnvolvidoEntity.Key.class)
public class EventoEnvolvidoEntity {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Id
    @Column(name = "organizacao_id")
    private UUID organizacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel_participacao", length = 64)
    private PapelEnvolvido papelParticipacao;

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public PapelEnvolvido getPapelParticipacao() {
        return papelParticipacao;
    }

    public void setPapelParticipacao(PapelEnvolvido papelParticipacao) {
        this.papelParticipacao = papelParticipacao;
    }

    public static class Key implements Serializable {
        private UUID eventoId;
        private UUID organizacaoId;

        public Key() {
        }

        public Key(UUID eventoId, UUID organizacaoId) {
            this.eventoId = eventoId;
            this.organizacaoId = organizacaoId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key key)) {
                return false;
            }
            return Objects.equals(eventoId, key.eventoId) && Objects.equals(organizacaoId, key.organizacaoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventoId, organizacaoId);
        }
    }
}
