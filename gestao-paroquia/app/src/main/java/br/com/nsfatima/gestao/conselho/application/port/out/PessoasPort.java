package br.com.nsfatima.gestao.conselho.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Porta de saída para pessoas e membros (camada anticorrupção sobre os contextos de fiéis e organizações).
 * A ata guarda só identificadores de fiéis; nomes são resolvidos aqui, na leitura e na exportação.
 */
public interface PessoasPort {

    /**
     * Fiéis com vínculo ativo na organização.
     *
     * @param organizacaoId organização consultada
     * @return identificadores dos fiéis membros
     */
    Set<UUID> membrosAtivos(UUID organizacaoId);

    /**
     * Fiel associado a uma conta de acesso.
     *
     * @param usuarioId identificador do usuário; pode ser nulo
     * @return o fiel do usuário, ou vazio se não há (usuário sem cadastro de fiel, como o clero)
     */
    Optional<UUID> fielDoUsuario(UUID usuarioId);

    /**
     * Nomes de fiéis.
     *
     * @param fielIds fiéis desejados
     * @return nome por identificador; fiéis inexistentes ficam de fora
     */
    Map<UUID, String> nomes(Collection<UUID> fielIds);
}
