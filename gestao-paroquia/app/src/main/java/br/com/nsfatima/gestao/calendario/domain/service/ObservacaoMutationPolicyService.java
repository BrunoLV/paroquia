package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoAutorInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoTipoImutavelException;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoTipoManualInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Serviço de domínio que impõe as regras de mutação manual de observações de
 * evento: apenas notas (tipo {@code NOTA}) podem ser criadas, editadas ou excluídas
 * pelo usuário via endpoint manual; observações geradas pelo sistema (justificativa,
 * aprovação, reprovação, cancelamento, ajuste de horário) são imutáveis nesse fluxo,
 * assim como notas já removidas; e apenas o autor original de uma nota pode
 * editá-la ou excluí-la.
 */
@Service
public class ObservacaoMutationPolicyService {

    private static final String TIPO_NOTA = "NOTA";

    /**
     * Garante que a criação manual de observação só é permitida para o tipo
     * {@code NOTA}, rejeitando qualquer tentativa de criar diretamente um tipo
     * reservado ao sistema.
     *
     * @param tipo tipo de observação informado na requisição
     * @throws ObservacaoTipoManualInvalidoException se o tipo informado não for {@code NOTA}
     */
    public void assertManualCreationAllowed(TipoObservacaoInput tipo) {
        if (tipo != TipoObservacaoInput.NOTA) {
            throw new ObservacaoTipoManualInvalidoException("Apenas NOTA é aceito no endpoint manual de observação");
        }
    }

    /**
     * Garante que a observação pode ser editada ou excluída pelo solicitante:
     * precisa ser do tipo {@code NOTA}, não pode já estar removida, e o solicitante
     * precisa ser o autor original da observação.
     *
     * @param observacao observação de evento a ser editada ou excluída
     * @param solicitanteId identificador do usuário que solicita a alteração
     * @throws ObservacaoTipoImutavelException se a observação não for do tipo {@code NOTA} ou já estiver removida
     * @throws ObservacaoAutorInvalidoException se o solicitante não for o autor da observação
     */
    public void assertCanEditOrDelete(ObservacaoEventoEntity observacao, UUID solicitanteId) {
        if (!TIPO_NOTA.equalsIgnoreCase(observacao.getTipo())) {
            throw new ObservacaoTipoImutavelException(
                    "Tipos de observação de sistema são imutáveis em endpoints manuais");
        }

        if (observacao.isRemovida()) {
            throw new ObservacaoTipoImutavelException("Notas removidas não podem ser editadas ou excluídas");
        }

        if (!observacao.getUsuarioId().equals(solicitanteId)) {
            throw new ObservacaoAutorInvalidoException("Usuário não é o autor da observação");
        }
    }
}
