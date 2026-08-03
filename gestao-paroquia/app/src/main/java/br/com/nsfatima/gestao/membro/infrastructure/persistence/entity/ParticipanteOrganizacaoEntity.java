package br.com.nsfatima.gestao.membro.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Registra o período de participação de um {@code MembroEntity} (vínculo usuário-organização) em uma
 * {@code Organizacao} adicional, distinta daquela para a qual o vínculo original foi criado — permite rastrear
 * que uma pessoa participa de mais de uma pastoral/movimento simultaneamente. Uma participação {@code ativa}
 * (sem {@code dataFim}) impede nova inscrição duplicada na mesma organização (ver
 * {@link br.com.nsfatima.gestao.membro.application.usecase.AddParticipanteUseCase}).
 */
@Entity
@Table(name = "membros_organizacoes", schema = "calendario")
public class ParticipanteOrganizacaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "membro_id", nullable = false)
    private UUID membroId;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(nullable = false)
    private boolean ativo = true;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMembroId() {
        return membroId;
    }

    public void setMembroId(UUID membroId) {
        this.membroId = membroId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
