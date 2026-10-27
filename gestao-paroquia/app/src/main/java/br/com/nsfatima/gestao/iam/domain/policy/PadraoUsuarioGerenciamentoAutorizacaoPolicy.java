package br.com.nsfatima.gestao.iam.domain.policy;

import br.com.nsfatima.gestao.iam.domain.exception.GerenciamentoUsuarioNegadoException;

import java.util.List;
import java.util.UUID;

/**
 * Implementação pura da política de autorização administrativa de gerenciamento de usuários.
 */
public final class PadraoUsuarioGerenciamentoAutorizacaoPolicy implements UsuarioGerenciamentoAutorizacaoPolicy {

    @Override
    public boolean isCoordinatorRole(String papel) {
        if (papel == null) {
            return false;
        }
        String normalized = papel.trim().toLowerCase().replace("_", "-");
        return normalized.equals("coordenador") || normalized.endsWith("-coordenador");
    }

    @Override
    public void assertPodeGerenciarAlvo(
            List<UUID> organizacaoIdsOndeAtorECoordenador, List<UUID> organizacaoIdsDoAlvo) {
        if (organizacaoIdsOndeAtorECoordenador == null || organizacaoIdsOndeAtorECoordenador.isEmpty()) {
            throw new GerenciamentoUsuarioNegadoException("Ator não é um coordenador");
        }

        boolean sharesOrg = organizacaoIdsDoAlvo != null
                && organizacaoIdsDoAlvo.stream().anyMatch(organizacaoIdsOndeAtorECoordenador::contains);

        if (!sharesOrg) {
            throw new GerenciamentoUsuarioNegadoException(
                    "Coordenador só pode gerenciar membros da própria organização");
        }
    }

    @Override
    public void assertECoordenadorDaOrganizacao(boolean atorECoordenadorNaOrganizacao) {
        if (!atorECoordenadorNaOrganizacao) {
            throw new GerenciamentoUsuarioNegadoException(
                    "Apenas Admin ou Coordenador de Organização pode executar esta ação");
        }
    }

    @Override
    public void assertPodeRedefinirSenha(boolean atorEAdmin, boolean atorEParoco, boolean alvoPossuiPapelGlobal) {
        if (atorEAdmin) {
            return;
        }
        if (!atorEParoco) {
            throw new GerenciamentoUsuarioNegadoException(
                    "Apenas Admin ou Pároco pode redefinir a senha de um usuário");
        }
        if (alvoPossuiPapelGlobal) {
            throw new GerenciamentoUsuarioNegadoException(
                    "Apenas Admin pode redefinir a senha de um usuário com papel global");
        }
    }
}
