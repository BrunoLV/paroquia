package br.com.nsfatima.gestao.local.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Mapeamento JPA do agregado de domínio {@code Local} na tabela {@code calendario.locais}.
 *
 * <p>Invariantes de persistência: {@code nome} é obrigatório e único (impede cadastro de dois locais
 * com o mesmo nome); {@code tipo} é obrigatório; {@code grupoParoquialId} é opcional e vincula o local a
 * uma organização (Matriz/Comunidade) para fins de escopo/autorização — um local sem organização fica
 * disponível de forma geral. O campo {@code ativo} controla a desativação lógica usada pelo fluxo de
 * exclusão do serviço de domínio.
 */
@Entity
@Table(name = "locais", schema = "calendario")
public class LocalEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160, unique = true)
    private String nome;

    @Column(nullable = false, length = 32)
    private String tipo;

    @Column(length = 500)
    private String endereco;

    private Integer capacidade;

    @Column(columnDefinition = "TEXT")
    private String caracteristicas;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "grupo_paroquial_id")
    private UUID grupoParoquialId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(Integer capacidade) {
        this.capacidade = capacidade;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }

    public void setGrupoParoquialId(UUID grupoParoquialId) {
        this.grupoParoquialId = grupoParoquialId;
    }
}
