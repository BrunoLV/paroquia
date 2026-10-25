package br.com.nsfatima.gestao.conselho.application.port.out;

import br.com.nsfatima.gestao.conselho.application.dto.ClerigoAtivo;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Porta de saída para pessoas e membros (camada anticorrupção sobre os contextos de fiéis e organizações).
 * A ata guarda só identificadores de pessoas (fiéis ou clérigos); nomes são resolvidos aqui, na leitura e na exportação.
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
     * Clérigos ativos da paróquia, que não são cadastrados como fiéis.
     *
     * @return os clérigos, em ordem de nome
     */
    List<ClerigoAtivo> clerigosAtivos();

    /**
     * Fiel associado a uma conta de acesso.
     *
     * @param usuarioId identificador do usuário; pode ser nulo
     * @return o fiel do usuário, ou vazio se não há (usuário sem cadastro de fiel, como o clero)
     */
    Optional<UUID> fielDoUsuario(UUID usuarioId);

    /**
     * Nomes de fiéis e de clérigos.
     *
     * @param fielIds pessoas desejadas
     * @return nome por identificador; pessoas inexistentes ficam de fora
     */
    Map<UUID, String> nomes(Collection<UUID> fielIds);
}
