package br.com.nsfatima.gestao.conselho.application.port.out;

import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída para o contexto de organizações (camada anticorrupção): traduz o agregado de Organização
 * para o retrato mínimo que a ata usa, sem acoplar o domínio do conselho ao modelo alheio.
 */
public interface OrganizacoesPort {

    /**
     * Busca uma organização ativa.
     *
     * @param id identificador da organização
     * @return o retrato da organização, ou vazio se inexistente ou inativa
     */
    Optional<ReferenciaOrganizacao> buscar(UUID id);

    /**
     * Nomes de organizações, para exibição e exportação.
     *
     * @param ids organizações desejadas
     * @return nome por identificador; organizações desconhecidas ficam de fora
     */
    Map<UUID, String> nomes(Collection<UUID> ids);
}
