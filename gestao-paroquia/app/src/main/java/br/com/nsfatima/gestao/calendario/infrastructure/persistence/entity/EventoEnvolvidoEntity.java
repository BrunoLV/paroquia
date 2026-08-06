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

/**
 * Associação (tabela de junção) entre um {@link EventoEntity} e uma organização envolvida
 * na sua realização, registrando o papel de participação dessa organização (responsável
 * ou apoio). A chave primária composta ({@code evento_id}, {@code grupoParoquial_id}) impede
 * que a mesma organização seja registrada mais de uma vez para o mesmo evento.
 */
@Entity
@Table(name = "eventos_envolvidos", schema = "calendario")
@IdClass(EventoEnvolvidoEntity.Key.class)
public class EventoEnvolvidoEntity {

    @Id
    @Column(name = "evento_id")
    private UUID eventoId;

    @Id
    @Column(name = "grupoParoquial_id")
    private UUID grupoParoquialId;

    /** Papel desta organização na realização do evento: responsável principal ou apenas apoio. */
    @Enumerated(EnumType.STRING)
    @Column(name = "papel_participacao", length = 64)
    private PapelEnvolvido papelParticipacao;

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }

    public void setGrupoParoquialId(UUID grupoParoquialId) {
        this.grupoParoquialId = grupoParoquialId;
    }

    public PapelEnvolvido getPapelParticipacao() {
        return papelParticipacao;
    }

    public void setPapelParticipacao(PapelEnvolvido papelParticipacao) {
        this.papelParticipacao = papelParticipacao;
    }

    /**
     * Chave primária composta de {@link EventoEnvolvidoEntity}, formada pelo par
     * (identificador do evento, identificador da organização).
     */
    public static class Key implements Serializable {
        private UUID eventoId;
        private UUID grupoParoquialId;

        public Key() {
        }

        public Key(UUID eventoId, UUID grupoParoquialId) {
            this.eventoId = eventoId;
            this.grupoParoquialId = grupoParoquialId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key key)) {
                return false;
            }
            return Objects.equals(eventoId, key.eventoId) && Objects.equals(grupoParoquialId, key.grupoParoquialId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventoId, grupoParoquialId);
        }
    }
}
