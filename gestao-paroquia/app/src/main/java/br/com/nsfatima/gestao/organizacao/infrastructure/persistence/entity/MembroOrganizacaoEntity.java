package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidade JPA que representa o vínculo (associação) entre um usuário e uma organização, com o
 * {@code papel} exercido nesse vínculo. Um mesmo usuário pode ter múltiplos vínculos ativos (em
 * organizações diferentes, ou papéis diferentes na mesma organização); vínculos desativados
 * ({@code ativo = false}) são preservados para histórico em vez de excluídos. O campo
 * {@code igrejaVinculoId} amarra pastorais/movimentos (que não têm hierarquia territorial própria) à
 * Matriz/Comunidade à qual pertencem, permitindo checagens de escopo territorial.
 */
@Entity
@Table(name = "membros_organizacao", schema = "calendario")
public class MembroOrganizacaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_organizacao", nullable = false, length = 32)
    private TipoOrganizacao tipoOrganizacao;

    @Column(nullable = false, length = 64)
    private String papel;

    @Column(nullable = false)
    private boolean ativo = true;
    @Column(name = "igreja_vinculo_id")
    private UUID igrejaVinculoId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
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

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
