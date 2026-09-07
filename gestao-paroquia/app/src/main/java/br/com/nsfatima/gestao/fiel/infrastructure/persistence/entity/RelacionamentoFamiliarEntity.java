package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="fiel_relacionamentos_familiares",schema="calendario") public class RelacionamentoFamiliarEntity {
 @Id private UUID id; @Column(name="par_id") private UUID parId; @Column(name="fiel_origem_id") private UUID fielOrigemId; @Column(name="fiel_destino_id") private UUID fielDestinoId; @Enumerated(EnumType.STRING) @Column(name="tipo_relacao") private TipoRelacionamentoFamiliar tipo; @Column(name="criado_em_utc") private Instant criadoEmUtc; @Column(name="deleted_at") private Instant deletedAt;
 public UUID getId(){return id;} public void setId(UUID v){id=v;} public UUID getParId(){return parId;} public void setParId(UUID v){parId=v;} public UUID getFielOrigemId(){return fielOrigemId;} public void setFielOrigemId(UUID v){fielOrigemId=v;} public UUID getFielDestinoId(){return fielDestinoId;} public void setFielDestinoId(UUID v){fielDestinoId=v;} public TipoRelacionamentoFamiliar getTipo(){return tipo;} public void setTipo(TipoRelacionamentoFamiliar v){tipo=v;} public Instant getCriadoEmUtc(){return criadoEmUtc;} public void setCriadoEmUtc(Instant v){criadoEmUtc=v;} public Instant getDeletedAt(){return deletedAt;} public void setDeletedAt(Instant v){deletedAt=v;}
}
