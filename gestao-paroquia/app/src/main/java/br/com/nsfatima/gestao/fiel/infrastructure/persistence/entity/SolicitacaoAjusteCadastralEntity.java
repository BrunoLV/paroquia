package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Proposta de alteração que só altera o Fiel após decisão administrativa. */
@Entity
@Table(name = "solicitacoes_ajuste_cadastral", schema = "calendario")
public class SolicitacaoAjusteCadastralEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    private UUID fielId;
    private UUID usuarioSolicitanteId;

    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    private String celular;

    @Convert(converter = EncryptedStringConverter.class)
    private String telefoneFixo;

    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoLogradouro;

    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoNumero;

    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoComplemento;

    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoBairro;

    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoCep;

    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoCidade;

    private String enderecoEstado;
    private String status;
    private String justificativaAnalise;
    private UUID analisadoPorUsuarioId;
    private Instant analisadoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID v) {
        id = v;
    }

    public UUID getFielId() {
        return fielId;
    }

    public void setFielId(UUID v) {
        fielId = v;
    }

    public UUID getUsuarioSolicitanteId() {
        return usuarioSolicitanteId;
    }

    public void setUsuarioSolicitanteId(UUID v) {
        usuarioSolicitanteId = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String v) {
        celular = v;
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public void setTelefoneFixo(String v) {
        telefoneFixo = v;
    }

    public String getEnderecoLogradouro() {
        return enderecoLogradouro;
    }

    public void setEnderecoLogradouro(String v) {
        enderecoLogradouro = v;
    }

    public String getEnderecoNumero() {
        return enderecoNumero;
    }

    public void setEnderecoNumero(String v) {
        enderecoNumero = v;
    }

    public String getEnderecoComplemento() {
        return enderecoComplemento;
    }

    public void setEnderecoComplemento(String v) {
        enderecoComplemento = v;
    }

    public String getEnderecoBairro() {
        return enderecoBairro;
    }

    public void setEnderecoBairro(String v) {
        enderecoBairro = v;
    }

    public String getEnderecoCep() {
        return enderecoCep;
    }

    public void setEnderecoCep(String v) {
        enderecoCep = v;
    }

    public String getEnderecoCidade() {
        return enderecoCidade;
    }

    public void setEnderecoCidade(String v) {
        enderecoCidade = v;
    }

    public String getEnderecoEstado() {
        return enderecoEstado;
    }

    public void setEnderecoEstado(String v) {
        enderecoEstado = v;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String v) {
        status = v;
    }

    public String getJustificativaAnalise() {
        return justificativaAnalise;
    }

    public void setJustificativaAnalise(String v) {
        justificativaAnalise = v;
    }

    public UUID getAnalisadoPorUsuarioId() {
        return analisadoPorUsuarioId;
    }

    public void setAnalisadoPorUsuarioId(UUID v) {
        analisadoPorUsuarioId = v;
    }

    public Instant getAnalisadoEmUtc() {
        return analisadoEmUtc;
    }

    public void setAnalisadoEmUtc(Instant v) {
        analisadoEmUtc = v;
    }
}
