package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.springframework.stereotype.Component;

/**
 * Converte entre o agregado {@link MembroGrupo} (domínio puro) e a {@link MembroGrupoEntity} JPA.
 * O papel é persistido pelo seu {@code storedValue()} (código estável), como no restante do stack
 * {@code membro/*}.
 */
@Component
public class MembroGrupoMapper {

    public MembroGrupo toDomain(MembroGrupoEntity e) {
        if (e == null) {
            return null;
        }
        return new MembroGrupo(
                e.getId(),
                e.getFielId(),
                e.getOrganizacaoId(),
                e.getTipoOrganizacao(),
                PapelOrganizacional.fromStoredValue(e.getPapel()),
                e.getIgrejaVinculoId(),
                e.getDataEntrada(),
                e.isAtivo());
    }

    public MembroGrupoEntity toEntity(MembroGrupo d) {
        MembroGrupoEntity e = new MembroGrupoEntity();
        e.setId(d.getId());
        aplicar(d, e);
        return e;
    }

    public void aplicar(MembroGrupo d, MembroGrupoEntity e) {
        e.setFielId(d.getFielId());
        e.setOrganizacaoId(d.getOrganizacaoId());
        e.setTipoOrganizacao(d.getTipoOrganizacao());
        e.setPapel(d.getPapel().storedValue());
        e.setIgrejaVinculoId(d.getIgrejaVinculoId());
        e.setDataEntrada(d.getDataEntrada());
        e.setAtivo(d.isAtivo());
    }
}
