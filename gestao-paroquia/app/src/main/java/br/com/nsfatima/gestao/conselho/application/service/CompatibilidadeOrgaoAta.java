package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.port.out.OrganizacoesPort;
import br.com.nsfatima.gestao.conselho.domain.exception.TipoReuniaoIncompativelException;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Reconfere, quando a ata avança, o que a criação já conferiu: os órgãos continuam ativos e compatíveis com o
 * tipo de reunião. Um órgão desativado, ou que mudou de natureza, não pode mais levar a ata adiante.
 */
@Service
public class CompatibilidadeOrgaoAta {

    private final OrganizacoesPort organizacoes;

    public CompatibilidadeOrgaoAta(OrganizacoesPort organizacoes) {
        this.organizacoes = organizacoes;
    }

    /**
     * Exige órgãos ativos e compatíveis com o tipo da ata.
     *
     * @throws TipoReuniaoIncompativelException se algum órgão foi desativado ou deixou de ser compatível
     */
    public void exigir(AtaReuniao ata) {
        ReferenciaOrganizacao convocante = ativo(ata.getOrganizacaoConvocanteId(), "convocante");
        ReferenciaOrganizacao associada = ata.getOrganizacaoAssociadaId() == null
                ? null : ativo(ata.getOrganizacaoAssociadaId(), "associado");
        AtaReuniao.exigirOrganizacoesCompativeis(ata.getTipo(), convocante, associada);
    }

    private ReferenciaOrganizacao ativo(UUID id, String papel) {
        return organizacoes.buscar(id).orElseThrow(() -> new TipoReuniaoIncompativelException(
                "O órgão " + papel + " da ata está inativo ou não existe mais."));
    }
}
