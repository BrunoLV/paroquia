package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoAutorInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoTipoImutavelException;
import br.com.nsfatima.gestao.calendario.domain.exception.ObservacaoTipoManualInvalidoException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ObservacaoMutationPolicyService {

    private static final String TIPO_NOTA = "NOTA";

    public void assertManualCreationAllowed(TipoObservacaoInput tipo) {
        if (tipo != TipoObservacaoInput.NOTA) {
            throw new ObservacaoTipoManualInvalidoException("Apenas NOTA é aceito no endpoint manual de observação");
        }
    }

    public void assertCanEditOrDelete(ObservacaoEventoEntity observacao, UUID solicitanteId) {
        if (!TIPO_NOTA.equalsIgnoreCase(observacao.getTipo())) {
            throw new ObservacaoTipoImutavelException("Tipos de observação de sistema são imutáveis em endpoints manuais");
        }

        if (observacao.isRemovida()) {
            throw new ObservacaoTipoImutavelException("Notas removidas não podem ser editadas ou excluídas");
        }

        if (!observacao.getUsuarioId().equals(solicitanteId)) {
            throw new ObservacaoAutorInvalidoException("Usuário não é o autor da observação");
        }
    }
}
