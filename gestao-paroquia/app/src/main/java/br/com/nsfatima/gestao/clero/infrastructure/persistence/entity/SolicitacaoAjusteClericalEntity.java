package br.com.nsfatima.gestao.clero.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Pendência moderável; os valores propostos ficam protegidos até a decisão. */
@Entity
@Table(name = "solicitacoes_ajuste_clerical", schema = "calendario")
public class SolicitacaoAjusteClericalEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_solicitante_id", nullable = false)
    private UUID usuarioSolicitanteId;

    @Column(name = "clerigo_id", nullable = false)
    private UUID clerigoId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String celular;

    @Convert(converter = EncryptedStringConverter.class)
    private String telefoneFixo;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String endereco;

    @Column(nullable = false)
    private String status;

    @Convert(converter = EncryptedStringConverter.class)
    private String justificativaAnalise;

    private UUID analisadoPorUsuarioId;
    private Instant analisadoEmUtc;

    @Column(nullable = false)
    private Instant criadoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID valor) {
        id = valor;
    }

    public UUID getUsuarioSolicitanteId() {
        return usuarioSolicitanteId;
    }

    public void setUsuarioSolicitanteId(UUID valor) {
        usuarioSolicitanteId = valor;
    }

    public UUID getClerigoId() {
        return clerigoId;
    }

    public void setClerigoId(UUID valor) {
        clerigoId = valor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String valor) {
        email = valor;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String valor) {
        celular = digitos(valor);
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public void setTelefoneFixo(String valor) {
        telefoneFixo = digitos(valor);
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String valor) {
        endereco = valor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String valor) {
        status = valor;
    }

    public String getJustificativaAnalise() {
        return justificativaAnalise;
    }

    public void setJustificativaAnalise(String valor) {
        justificativaAnalise = valor;
    }

    public UUID getAnalisadoPorUsuarioId() {
        return analisadoPorUsuarioId;
    }

    public void setAnalisadoPorUsuarioId(UUID valor) {
        analisadoPorUsuarioId = valor;
    }

    public Instant getAnalisadoEmUtc() {
        return analisadoEmUtc;
    }

    public void setAnalisadoEmUtc(Instant valor) {
        analisadoEmUtc = valor;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant valor) {
        criadoEmUtc = valor;
    }

    private String digitos(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}
