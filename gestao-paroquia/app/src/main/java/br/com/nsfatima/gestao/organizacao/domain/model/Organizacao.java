package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.UUID;

public class Organizacao {
    private final UUID id;
    private String nome;
    private TipoOrganizacao tipo;
    private String contato;
    private String coordenador;
    private String viceCoordenador;
    private boolean ativo;
    private UUID parentId;
    private UUID pastoralOficialId;

    public Organizacao(UUID id,
                       String nome, TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador,
                       boolean ativo, UUID parentId, UUID pastoralOficialId) {

        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.coordenador = coordenador;
        this.viceCoordenador = viceCoordenador;
        this.ativo = ativo;
        this.parentId = parentId;
        this.pastoralOficialId = pastoralOficialId;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoOrganizacao getTipo() {
        return tipo;
    }

    public String getContato() {
        return contato;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public String getViceCoordenador() {
        return viceCoordenador;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public UUID getPastoralOficialId() {
        return pastoralOficialId;
    }

    public void setPastoralOficialId(UUID pastoralOficialId) {
        this.pastoralOficialId = pastoralOficialId;
    }

    public void update(String nome,
                       TipoOrganizacao tipo, String contato, String coordenador, String viceCoordenador, boolean ativo,
                       UUID parentId, UUID pastoralOficialId) {

        this.nome = nome;
        this.tipo = tipo;
        this.contato = contato;
        this.coordenador = coordenador;
        this.viceCoordenador = viceCoordenador;
        this.ativo = ativo;
        this.parentId = parentId;
        this.pastoralOficialId = pastoralOficialId;
    }
}
