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
 * ou apoio). A chave primária composta ({@code evento_id}, {@code organizacao_id}) impede
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
    @Column(name = "organizacao_id")
    private UUID organizacaoId;

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

    /**
     * Chave primária composta de {@link EventoEnvolvidoEntity}, formada pelo par
     * (identificador do evento, identificador da organização).
     */
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
