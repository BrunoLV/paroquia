package br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.governanca.domain.model.StatusSolicitacaoDpo;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade para gestão e atendimento de solicitações de exercício de direitos dos titulares (DSAR - Arts. 18 e 19).
 */
@Entity
@Table(name = "solicitacoes_dpo", schema = "calendario")
public class SolicitacaoDpoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 36)
    private String protocolo;

    @Column(name = "titular_nome", nullable = false)
    private String titularNome;

    @Column(name = "titular_email", nullable = false)
    private String titularEmail;

    @Column(name = "titular_cpf", length = 14)
    private String titularCpf;

    @Column(name = "tipo_solicitacao", nullable = false, length = 50)
    private String tipoSolicitacao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusSolicitacaoDpo status = StatusSolicitacaoDpo.RECEBIDA;

    @Column(columnDefinition = "TEXT")
    private String resposta;

    @Column(name = "prazo_resposta_utc", nullable = false)
    private Instant prazoRespostaUtc;

    @Column(name = "respondido_em_utc")
    private Instant respondidoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public String getTitularNome() {
        return titularNome;
    }

    public void setTitularNome(String titularNome) {
        this.titularNome = titularNome;
    }

    public String getTitularEmail() {
        return titularEmail;
    }

    public void setTitularEmail(String titularEmail) {
        this.titularEmail = titularEmail;
    }

    public String getTitularCpf() {
        return titularCpf;
    }

    public void setTitularCpf(String titularCpf) {
        this.titularCpf = titularCpf;
    }

    public String getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(String tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public StatusSolicitacaoDpo getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacaoDpo status) {
        this.status = status;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public Instant getPrazoRespostaUtc() {
        return prazoRespostaUtc;
    }

    public void setPrazoRespostaUtc(Instant prazoRespostaUtc) {
        this.prazoRespostaUtc = prazoRespostaUtc;
    }

    public Instant getRespondidoEmUtc() {
        return respondidoEmUtc;
    }

    public void setRespondidoEmUtc(Instant respondidoEmUtc) {
        this.respondidoEmUtc = respondidoEmUtc;
    }
}
