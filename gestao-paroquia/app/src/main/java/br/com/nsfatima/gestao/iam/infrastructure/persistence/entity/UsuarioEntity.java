package br.com.nsfatima.gestao.iam.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.iam.domain.model.Papeis;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Registro persistente de uma conta de usuário: credenciais de autenticação e papéis globais ({@code roles}).
 * Não carrega mais dados pessoais/pastorais (nome, telefone, data de nascimento, sacramentos) — esses vivem
 * exclusivamente no {@code Fiel} referenciado por {@link #fielId}, obrigatório desde a track
 * {@code usuario_fiel_decoupling}. O {@code username} (login) é independente e imutável frente ao e-mail de
 * contato do Fiel.
 */
@Entity
@Table(name = "usuarios", schema = "calendario")
public class UsuarioEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    /** Papéis globais do usuário, persistidos como string única separada por vírgulas (ex.: "ROLE_ADMIN,ROLE_USER"). */
    @Column
    private String roles;

    /** Igreja/comunidade (MATRIZ ou COMUNIDADE) informada no cadastro; distinta dos vínculos organizacionais. */
    @Column(name = "igreja_id")
    private UUID igrejaId;

    /**
     * Referência ao {@code Fiel} (pessoa física) correspondente no Contexto Paroquial — obrigatória: todo
     * {@code Usuario} deve estar vinculado a um {@code Fiel} já existente.
     */
    @Column(name = "fiel_id")
    private UUID fielId;

    /** Referência exclusiva ao clérigo quando a conta pertence a ministro ordenado. */
    @Column(name = "clerigo_id")
    private UUID clerigoId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public UUID getIgrejaId() {
        return igrejaId;
    }

    public void setIgrejaId(UUID igrejaId) {
        this.igrejaId = igrejaId;
    }

    public UUID getFielId() {
        return fielId;
    }

    public void setFielId(UUID fielId) {
        this.fielId = fielId;
    }

    public UUID getClerigoId() {
        return clerigoId;
    }

    public void setClerigoId(UUID clerigoId) {
        this.clerigoId = clerigoId;
    }

    /** Papéis globais como Value Object. */
    public Papeis papeis() {
        return Papeis.deCsv(roles);
    }

    public void substituirPapeis(Papeis papeis) {
        this.roles = papeis.paraCsv();
    }

    /**
     * Aprova um usuário pendente: habilita o login, garante o papel padrão {@code ROLE_USER} (sem
     * duplicar) e registra o ator. A ativação dos vínculos organizacionais é orquestrada à parte.
     */
    public void aprovar(String actor) {
        this.enabled = true;
        this.roles = papeis().adicionar("ROLE_USER").adicionar("ROLE_FIEL").paraCsv();
        setAtualizadoPor(actor);
    }

    public void ativar() {
        this.enabled = true;
    }

    public void desativar() {
        this.enabled = false;
    }

    public void alterarUsername(String novoUsername) {
        this.username = novoUsername;
    }

    /** Recebe o hash já produzido pelo {@code PasswordEncoder} (a cifragem é responsabilidade da app). */
    public void redefinirSenhaHash(String hash) {
        this.passwordHash = hash;
    }
}
