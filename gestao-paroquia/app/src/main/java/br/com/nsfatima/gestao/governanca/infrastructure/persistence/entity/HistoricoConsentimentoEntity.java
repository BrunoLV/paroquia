package br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.governanca.domain.model.TipoEventoConsentimento;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Registro indelével e imutável de auditoria de consentimento sob a LGPD (Arts. 8º § 2º e 6º X).
 */
@Entity
@Table(name = "historico_consentimentos", schema = "calendario")
public class HistoricoConsentimentoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "titular_tipo", nullable = false, length = 20)
    private TipoTitularConsentimento titularTipo;

    @Column(name = "titular_id", nullable = false)
    private UUID titularId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEventoConsentimento tipoEvento;

    @Column(nullable = false, length = 30)
    private String canal = "PORTAL_AUTOATENDIMENTO";

    @Column(name = "metodo_autenticacao", nullable = false, length = 30)
    private String metodoAutenticacao = "OTP_EMAIL";

    @Column(name = "termo_versao", nullable = false, length = 20)
    private String termoVersao = "v1.0";

    @Column(name = "termo_hash", nullable = false, length = 64)
    private String termoHash;

    @Column(name = "termo_aceito", nullable = false)
    private boolean termoAceito;

    @Column(name = "uso_imagem_autorizado", nullable = false)
    private boolean usoImagemAutorizado;

    @Column(name = "permitir_divulgacao_aniversario", nullable = false)
    private boolean permitirDivulgacaoAniversario;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "responsavel_menor_nome")
    private String responsavelMenorNome;

    @Column(name = "responsavel_menor_cpf", length = 14)
    private String responsavelMenorCpf;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoTitularConsentimento getTitularTipo() {
        return titularTipo;
    }

    public void setTitularTipo(TipoTitularConsentimento titularTipo) {
        this.titularTipo = titularTipo;
    }

    public UUID getTitularId() {
        return titularId;
    }

    public void setTitularId(UUID titularId) {
        this.titularId = titularId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public TipoEventoConsentimento getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoConsentimento tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getMetodoAutenticacao() {
        return metodoAutenticacao;
    }

    public void setMetodoAutenticacao(String metodoAutenticacao) {
        this.metodoAutenticacao = metodoAutenticacao;
    }

    public String getTermoVersao() {
        return termoVersao;
    }

    public void setTermoVersao(String termoVersao) {
        this.termoVersao = termoVersao;
    }

    public String getTermoHash() {
        return termoHash;
    }

    public void setTermoHash(String termoHash) {
        this.termoHash = termoHash;
    }

    public boolean isTermoAceito() {
        return termoAceito;
    }

    public void setTermoAceito(boolean termoAceito) {
        this.termoAceito = termoAceito;
    }

    public boolean isUsoImagemAutorizado() {
        return usoImagemAutorizado;
    }

    public void setUsoImagemAutorizado(boolean usoImagemAutorizado) {
        this.usoImagemAutorizado = usoImagemAutorizado;
    }

    public boolean isPermitirDivulgacaoAniversario() {
        return permitirDivulgacaoAniversario;
    }

    public void setPermitirDivulgacaoAniversario(boolean permitirDivulgacaoAniversario) {
        this.permitirDivulgacaoAniversario = permitirDivulgacaoAniversario;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public void setIpOrigem(String ipOrigem) {
        this.ipOrigem = ipOrigem;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getResponsavelMenorNome() {
        return responsavelMenorNome;
    }

    public void setResponsavelMenorNome(String responsavelMenorNome) {
        this.responsavelMenorNome = responsavelMenorNome;
    }

    public String getResponsavelMenorCpf() {
        return responsavelMenorCpf;
    }

    public void setResponsavelMenorCpf(String responsavelMenorCpf) {
        this.responsavelMenorCpf = responsavelMenorCpf;
    }
}
