package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Recalcula o status {@code RESERVADO}/{@code DISPONIVEL} de um ativo a partir das reservas ativas
 * no momento. Ativos {@code EM_MANUTENCAO} ou excluídos são ignorados (manutenção tem precedência).
 * Disparado por eventos de reserva e pelo job de varredura.
 */
@Service
public class SincronizarStatusReservadoAtivoUseCase {

    private final AtivoPatrimonialRepository repository;
    private final ConsultaReservaDeAtivoPort consultaReservaPort;

    public SincronizarStatusReservadoAtivoUseCase(AtivoPatrimonialRepository repository,
                                                  ConsultaReservaDeAtivoPort consultaReservaPort) {
        this.repository = repository;
        this.consultaReservaPort = consultaReservaPort;
    }

    @Transactional
    public void execute(UUID ativoId) {
        repository.findById(new AtivoPatrimonialId(ativoId)).ifPresent(this::sincronizar);
    }

    private void sincronizar(AtivoPatrimonial ativo) {
        if (ativo.estaExcluido() || ativo.getStatus() == AtivoStatus.EM_MANUTENCAO) {
            return;
        }
        AtivoStatus anterior = ativo.getStatus();
        Instant agora = Instant.now();
        if (consultaReservaPort.temReservaAtivaAgora(ativo.getId().value())) {
            ativo.marcarComoReservado(agora);
        } else {
            ativo.marcarComoDisponivel(agora);
        }
        if (ativo.getStatus() != anterior) {
            repository.save(ativo);
        }
    }
}
