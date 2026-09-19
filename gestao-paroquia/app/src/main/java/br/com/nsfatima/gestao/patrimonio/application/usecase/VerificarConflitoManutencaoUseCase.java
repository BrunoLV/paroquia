package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.ReservaImpeditivaResponse;
import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNaoEncontradoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Pré-checagem read-only usada pela interface antes de confirmar o envio para manutenção: retorna
 * as reservas que impediriam a manutenção no intervalo informado.
 */
@Service
public class VerificarConflitoManutencaoUseCase {

    private final AtivoPatrimonialRepository ativoRepository;
    private final ConsultaReservaDeAtivoPort consultaReservaPort;

    public VerificarConflitoManutencaoUseCase(AtivoPatrimonialRepository ativoRepository,
                                              ConsultaReservaDeAtivoPort consultaReservaPort) {
        this.ativoRepository = ativoRepository;
        this.consultaReservaPort = consultaReservaPort;
    }

    @Transactional(readOnly = true)
    public List<ReservaImpeditivaResponse> execute(UUID ativoId, Instant dataInicio, Instant dataFimPrevista) {
        ativoRepository.findById(new AtivoPatrimonialId(ativoId))
                .filter(a -> !a.estaExcluido())
                .orElseThrow(() -> new AtivoPatrimonialNaoEncontradoException("Ativo não encontrado: " + ativoId));

        return consultaReservaPort.reservasImpeditivas(ativoId, dataInicio, dataFimPrevista).stream()
                .map(ReservaImpeditivaResponse::de)
                .toList();
    }
}
