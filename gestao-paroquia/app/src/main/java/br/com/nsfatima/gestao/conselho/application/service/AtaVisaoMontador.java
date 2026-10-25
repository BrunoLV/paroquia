package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtaVisao;
import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;
import br.com.nsfatima.gestao.conselho.application.port.out.OrganizacoesPort;
import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ConflitoInteresse;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ItemPauta;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Monta a {@link AtaVisao}: acrescenta à ata as ações permitidas ao ator e os nomes que a ata não persiste. */
@Service
public class AtaVisaoMontador {

    private final ConselhoAuthorizationService autorizacao;
    private final PessoasPort pessoas;
    private final OrganizacoesPort organizacoes;

    public AtaVisaoMontador(ConselhoAuthorizationService autorizacao, PessoasPort pessoas,
                            OrganizacoesPort organizacoes) {
        this.autorizacao = autorizacao;
        this.pessoas = pessoas;
        this.organizacoes = organizacoes;
    }

    /** Visão da ata para o ator informado. */
    public AtaVisao montar(AtorAutenticado ator, AtaReuniao ata) {
        UUID convocante = ata.getOrganizacaoConvocanteId();
        UUID associada = ata.getOrganizacaoAssociadaId();
        Set<UUID> orgs = new HashSet<>();
        orgs.add(convocante);
        if (associada != null) {
            orgs.add(associada);
        }
        Map<UUID, String> nomesOrgs = organizacoes.nomes(orgs);
        return new AtaVisao(ata, autorizacao.acoes(ator, ata), pessoas.nomes(fieisCitados(ata)),
                nomesOrgs.get(convocante), associada == null ? null : nomesOrgs.get(associada));
    }

    private static Set<UUID> fieisCitados(AtaReuniao ata) {
        Set<UUID> ids = new HashSet<>();
        for (Presenca presenca : ata.getConteudo().presenca().nominal()) {
            ids.add(presenca.fielId());
        }
        for (ItemPauta item : ata.getConteudo().itens()) {
            for (Deliberacao deliberacao : item.deliberacoes()) {
                deliberacao.conflitos().stream().map(ConflitoInteresse::declaranteFielId).forEach(ids::add);
            }
        }
        return ids;
    }
}
