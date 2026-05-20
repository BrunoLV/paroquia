package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "observacoes_nota_revisoes", schema = "calendario")
public class ObservacaoNotaRevisaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "observacao_id", nullable = false)
    private UUID observacaoId;

    @Column(name = "conteudo_anterior", nullable = false, length = 4000)
    private String conteudoAnterior;

    @Column(name = "conteudo_novo", nullable = false, length = 4000)
    private String conteudoNovo;

    @Column(name = "revisado_por_usuario_id", nullable = false)
    private UUID revisadoPorUsuarioId;

    @Column(name = "revisado_em_utc", nullable = false)
    private Instant revisadoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getObservacaoId() {
        return observacaoId;
    }

    public void setObservacaoId(UUID observacaoId) {
        this.observacaoId = observacaoId;
    }

    public String getConteudoAnterior() {
        return conteudoAnterior;
    }

    public void setConteudoAnterior(String conteudoAnterior) {
        this.conteudoAnterior = conteudoAnterior;
    }

    public String getConteudoNovo() {
        return conteudoNovo;
    }

    public void setConteudoNovo(String conteudoNovo) {
        this.conteudoNovo = conteudoNovo;
    }

    public UUID getRevisadoPorUsuarioId() {
        return revisadoPorUsuarioId;
    }

    public void setRevisadoPorUsuarioId(UUID revisadoPorUsuarioId) {
        this.revisadoPorUsuarioId = revisadoPorUsuarioId;
    }

    public Instant getRevisadoEmUtc() {
        return revisadoEmUtc;
    }

    public void setRevisadoEmUtc(Instant revisadoEmUtc) {
        this.revisadoEmUtc = revisadoEmUtc;
    }
}
