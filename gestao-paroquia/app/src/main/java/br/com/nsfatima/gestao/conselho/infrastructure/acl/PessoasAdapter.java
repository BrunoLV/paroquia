package br.com.nsfatima.gestao.conselho.infrastructure.acl;

import br.com.nsfatima.gestao.conselho.application.port.out.PessoasPort;
import br.com.nsfatima.gestao.fiel.application.usecase.ObterFielUseCase;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListarOrganizacaoMembrosUseCase;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Camada anticorrupção sobre os contextos de fiéis e organizações. Passa pelos casos de uso públicos desses
 * contextos, nunca pela persistência deles, e devolve ao conselho só identificadores e nomes.
 */
@Component
public class PessoasAdapter implements PessoasPort {

    private final ListarOrganizacaoMembrosUseCase membros;
    private final ObterFielUseCase fieis;

    public PessoasAdapter(ListarOrganizacaoMembrosUseCase membros, ObterFielUseCase fieis) {
        this.membros = membros;
        this.fieis = fieis;
    }

    @Override
    public Set<UUID> membrosAtivos(UUID organizacaoId) {
        return membros.execute(organizacaoId).stream()
                .filter(OrganizacaoMembroResponse::ativo)
                .map(OrganizacaoMembroResponse::fielId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Map<UUID, String> nomes(Collection<UUID> fielIds) {
        Map<UUID, String> nomes = new HashMap<>();
        for (UUID id : fielIds) {
            try {
                nomes.put(id, fieis.executeContato(id).nome());
            } catch (IllegalArgumentException naoEncontrado) {
                continue;
            }
        }
        return nomes;
    }
}
