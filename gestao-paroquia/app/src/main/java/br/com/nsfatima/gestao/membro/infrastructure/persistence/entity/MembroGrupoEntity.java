package br.com.nsfatima.gestao.membro.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Vínculo de um {@code Fiel} (pessoa física) a uma organização, com o papel que exerce ali. Substitui as três
 * tabelas legadas ({@code membros}, {@code membros_organizacao}, {@code membros_organizacoes}) unificadas pela
 * track {@code membro_grupo_por_fiel}: participação pastoral não exige mais que o Fiel possua um {@code Usuario}
 * (login) — ver {@code AdicionarMembroUseCase}, que nunca toca {@code UsuarioEntity}. Um mesmo Fiel pode ter
 * múltiplas linhas (uma por organização/papel); múltiplas participações são simplesmente múltiplas linhas.
 */
@Entity
@Table(name = "membro_grupo", schema = "calendario")
public class MembroGrupoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "fiel_id", nullable = false)
    private UUID fielId;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_organizacao", nullable = false, length = 32)
    private TipoOrganizacao tipoOrganizacao;

    @Column(nullable = false, length = 64)
    private String papel;

    @Column(name = "igreja_vinculo_id")
    private UUID igrejaVinculoId;

    @Column(name = "data_entrada")
    private LocalDate dataEntrada;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "formacao_concluida_em")
    private LocalDate formacaoConcluidaEm;

    @Column(name = "vivencia_sacramental_atestada_em")
    private LocalDate vivenciaSacramentalAtestadaEm;

    @Column(name = "termo_protecao_menores_em")
    private LocalDate termoProtecaoMenoresEm;

    @Column(name = "termo_protecao_menores_valido_ate")
    private LocalDate termoProtecaoMenoresValidoAte;

    @Column(name = "enviado_em")
    private LocalDate enviadoEm;

    @Column(name = "enviado_por", length = 128)
    private String enviadoPor;

    public LocalDate getFormacaoConcluidaEm() {
        return formacaoConcluidaEm;
    }

    public void setFormacaoConcluidaEm(LocalDate formacaoConcluidaEm) {
        this.formacaoConcluidaEm = formacaoConcluidaEm;
    }

    public LocalDate getVivenciaSacramentalAtestadaEm() {
        return vivenciaSacramentalAtestadaEm;
    }

    public void setVivenciaSacramentalAtestadaEm(LocalDate vivenciaSacramentalAtestadaEm) {
        this.vivenciaSacramentalAtestadaEm = vivenciaSacramentalAtestadaEm;
    }

    public LocalDate getTermoProtecaoMenoresEm() {
        return termoProtecaoMenoresEm;
    }

    public void setTermoProtecaoMenoresEm(LocalDate termoProtecaoMenoresEm) {
        this.termoProtecaoMenoresEm = termoProtecaoMenoresEm;
    }

    public LocalDate getTermoProtecaoMenoresValidoAte() {
        return termoProtecaoMenoresValidoAte;
    }

    public void setTermoProtecaoMenoresValidoAte(LocalDate termoProtecaoMenoresValidoAte) {
        this.termoProtecaoMenoresValidoAte = termoProtecaoMenoresValidoAte;
    }

    public LocalDate getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDate enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public String getEnviadoPor() {
        return enviadoPor;
    }

    public void setEnviadoPor(String enviadoPor) {
        this.enviadoPor = enviadoPor;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getFielId() {
        return fielId;
    }

    public void setFielId(UUID fielId) {
        this.fielId = fielId;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public TipoOrganizacao getTipoOrganizacao() {
        return tipoOrganizacao;
    }

    public void setTipoOrganizacao(TipoOrganizacao tipoOrganizacao) {
        this.tipoOrganizacao = tipoOrganizacao;
    }

    public String getPapel() {
        return papel;
    }

    public void setPapel(String papel) {
        this.papel = papel;
    }

    public UUID getIgrejaVinculoId() {
        return igrejaVinculoId;
    }

    public void setIgrejaVinculoId(UUID igrejaVinculoId) {
        this.igrejaVinculoId = igrejaVinculoId;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
