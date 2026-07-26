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
            throw new ObservacaoTipoManualInvalidoException("Only NOTA is accepted on the manual observation endpoint");
        }
    }

    public void assertCanEditOrDelete(ObservacaoEventoEntity observacao, UUID solicitanteId) {
        if (!TIPO_NOTA.equalsIgnoreCase(observacao.getTipo())) {
            throw new ObservacaoTipoImutavelException("System observation types are immutable on manual endpoints");
        }

        if (observacao.isRemovida()) {
            throw new ObservacaoTipoImutavelException("Removed notes cannot be edited or deleted");
        }

        if (!observacao.getUsuarioId().equals(solicitanteId)) {
            throw new ObservacaoAutorInvalidoException("User is not the observation author");
        }
    }
}
