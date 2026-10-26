package br.com.nsfatima.gestao.iam.domain.policy;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

/**
 * Contrato puro de política para autorização administrativa de gerenciamento de usuários:
 * decide se um ator coordenador pode agir sobre um usuário-alvo ou sobre uma organização,
 * a partir dos vínculos já carregados pelo orquestrador.
 */
public interface UsuarioGerenciamentoAutorizacaoPolicy {

    /**
     * {@code true} se o papel (normalizado) conferir autoridade de coordenação.
     *
     * @param papel papel do vínculo, em formato livre (ex.: {@code "coordenador_pastoral"})
     */
    boolean isCoordinatorRole(String papel);

    /**
     * Garante que um ator coordenador (não admin) só gerencia usuários com quem compartilha
     * ao menos uma organização onde atua como coordenador.
     *
     * @param organizacaoIdsOndeAtorECoordenador organizações onde o ator é coordenador ativo
     * @param organizacaoIdsDoAlvo organizações onde o usuário-alvo possui vínculo ativo
     * @throws AccessDeniedException se o ator não for coordenador em nenhuma organização, ou se
     *     não compartilhar organização com o alvo
     */
    void assertPodeGerenciarAlvo(List<UUID> organizacaoIdsOndeAtorECoordenador, List<UUID> organizacaoIdsDoAlvo);

    /**
     * Garante que um ator coordenador (não admin) atua como coordenador ativo na organização informada.
     *
     * @throws AccessDeniedException se o ator não for coordenador ativo daquela organização
     */
    void assertECoordenadorDaOrganizacao(boolean atorECoordenadorNaOrganizacao);

    /**
     * Garante que a redefinição administrativa de senha (que equivale a assumir a conta) só é feita por quem
     * tem alçada sobre o alvo: o admin global redefine qualquer conta; o Pároco redefine apenas contas sem
     * papel global, de modo que ninguém com papel de coordenação ou secretaria assuma a conta de um superior.
     *
     * @param atorEAdmin {@code true} se o ator é admin global
     * @param atorEParoco {@code true} se o ator é Pároco
     * @param alvoPossuiPapelGlobal {@code true} se o usuário-alvo tem qualquer papel global ativo
     * @throws AccessDeniedException se o ator não tiver alçada sobre o alvo
     */
    void assertPodeRedefinirSenha(boolean atorEAdmin, boolean atorEParoco, boolean alvoPossuiPapelGlobal);
}
