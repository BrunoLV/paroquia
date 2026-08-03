package br.com.nsfatima.gestao.calendario.domain.exception;

/**
 * Lançada quando se tenta editar ou excluir uma observação de evento cujo tipo é
 * imutável em endpoints manuais — observações geradas pelo sistema ou notas já
 * removidas não podem ser alteradas por ação direta do usuário.
 */
public class ObservacaoTipoImutavelException extends RuntimeException {

    /**
     * Cria a exceção com a mensagem descrevendo por que a observação é imutável.
     *
     * @param message descrição da regra de imutabilidade violada
     */
    public ObservacaoTipoImutavelException(String message) {
        super(message);
    }
}
