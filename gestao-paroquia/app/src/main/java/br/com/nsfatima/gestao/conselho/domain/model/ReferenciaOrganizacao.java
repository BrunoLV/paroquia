package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.util.Objects;
import java.util.UUID;

/**
 * Retrato imutável da organização que convoca (ou se associa a) uma reunião: só o que as regras do tipo
 * precisam saber. O agregado {@link AtaReuniao} guarda apenas o {@code id}; tipo e subtipo servem para validar
 * a compatibilidade na criação, sem acoplar a ata ao agregado de Organização. {@code unidadeId} é a organização-mãe,
 * que diz a que unidade (Matriz ou Comunidade) o órgão pertence.
 */
public record ReferenciaOrganizacao(UUID id, TipoOrganizacao tipo, SubtipoConselho subtipo, UUID unidadeId) {

    public ReferenciaOrganizacao {
        Objects.requireNonNull(id, "A organização é obrigatória.");
        Objects.requireNonNull(tipo, "O tipo da organização é obrigatório.");
        if (tipo == TipoOrganizacao.CONSELHO && subtipo == null) {
            throw new AtaInvalidaException("Um conselho deve informar o subtipo (CPP, CPC ou CAEP).");
        }
    }

    /** Retrato sem unidade, para quando a relação com a organização-mãe não importa. */
    public ReferenciaOrganizacao(UUID id, TipoOrganizacao tipo, SubtipoConselho subtipo) {
        this(id, tipo, subtipo, null);
    }
}
