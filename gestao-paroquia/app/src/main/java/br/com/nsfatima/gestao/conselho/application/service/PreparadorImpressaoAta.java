package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtaParaImpressao;
import br.com.nsfatima.gestao.conselho.application.port.out.OrganizacoesPort;
import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ConflitoInteresse;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ItemPauta;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Resolve, para um conjunto de atas, o que a ata não persiste e a impressão exige: nomes de pessoas e de
 * organizações e a identificação da ata original das retificadoras. As consultas são feitas uma vez para
 * todo o conjunto, evitando repetir a busca do mesmo fiel em cada ata de uma compilação.
 */
@Service
public class PreparadorImpressaoAta {

    private static final String SEM_NUMERO = "sem número";

    private final PessoasPort pessoas;
    private final OrganizacoesPort organizacoes;
    private final AtaRepository repository;

    public PreparadorImpressaoAta(PessoasPort pessoas, OrganizacoesPort organizacoes, AtaRepository repository) {
        this.pessoas = pessoas;
        this.organizacoes = organizacoes;
        this.repository = repository;
    }

    /**
     * Prepara as atas para impressão.
     *
     * @param atas atas a imprimir
     * @return as atas com nomes resolvidos, na mesma ordem
     */
    public List<AtaParaImpressao> preparar(List<AtaReuniao> atas) {
        Set<UUID> fieis = new HashSet<>();
        Set<UUID> orgs = new HashSet<>();
        for (AtaReuniao ata : atas) {
            coletar(ata, fieis, orgs);
        }
        Map<UUID, String> nomesFieis = pessoas.nomes(fieis);
        Map<UUID, String> nomesOrgs = organizacoes.nomes(orgs);
        return atas.stream().map(ata -> new AtaParaImpressao(ata, nomesFieis,
                nomesOrgs.getOrDefault(ata.getOrganizacaoConvocanteId(), "Organização"),
                ata.getOrganizacaoAssociadaId() == null ? null
                        : nomesOrgs.getOrDefault(ata.getOrganizacaoAssociadaId(), "Organização"),
                identificacaoDaRetificada(ata))).toList();
    }

    private static void coletar(AtaReuniao ata, Set<UUID> fieis, Set<UUID> orgs) {
        orgs.add(ata.getOrganizacaoConvocanteId());
        if (ata.getOrganizacaoAssociadaId() != null) {
            orgs.add(ata.getOrganizacaoAssociadaId());
        }
        ata.getConteudo().presenca().nominal().stream().map(Presenca::fielId).forEach(fieis::add);
        for (ItemPauta item : ata.getConteudo().itens()) {
            for (Deliberacao deliberacao : item.deliberacoes()) {
                deliberacao.conflitos().stream().map(ConflitoInteresse::declaranteFielId).forEach(fieis::add);
            }
        }
    }

    private String identificacaoDaRetificada(AtaReuniao ata) {
        if (ata.getAtaRetificadaId() == null) {
            return null;
        }
        return repository.buscarPorId(ata.getAtaRetificadaId())
                .flatMap(AtaReuniao::identificacao).orElse(SEM_NUMERO);
    }
}
