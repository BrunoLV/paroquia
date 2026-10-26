package br.com.nsfatima.gestao.conselho.infrastructure.acl;

import br.com.nsfatima.gestao.conselho.application.port.out.OrganizacoesPort;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Camada anticorrupção sobre o contexto de organizações: lê pela porta de domínio pública e devolve ao
 * conselho apenas id, tipo, subtipo e organização-mãe, sem expor o agregado de Organização.
 */
@Component
public class OrganizacoesAdapter implements OrganizacoesPort {

    private final OrganizacaoRepository organizacoes;

    public OrganizacoesAdapter(OrganizacaoRepository organizacoes) {
        this.organizacoes = organizacoes;
    }

    @Override
    public Optional<ReferenciaOrganizacao> buscar(UUID id) {
        return organizacoes.findById(id)
                .filter(Organizacao::isAtivo)
                .map(o -> new ReferenciaOrganizacao(o.getId(), o.getTipo(), o.getSubtipoConselho(), o.getParentId()));
    }

    @Override
    public Map<UUID, String> nomes(Collection<UUID> ids) {
        Map<UUID, String> nomes = new HashMap<>();
        for (UUID id : ids) {
            organizacoes.findById(id).ifPresent(o -> nomes.put(id, o.getNome()));
        }
        return nomes;
    }
}
