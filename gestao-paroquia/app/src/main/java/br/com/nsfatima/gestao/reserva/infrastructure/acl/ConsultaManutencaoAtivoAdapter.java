package br.com.nsfatima.gestao.reserva.infrastructure.acl;

import br.com.nsfatima.gestao.patrimonio.application.service.ConsultaManutencaoAtivoService;
import br.com.nsfatima.gestao.patrimonio.application.service.JanelaManutencaoAtivo;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer: implementa a porta do módulo de Reservas delegando à API pública do
 * contexto de patrimônio e traduzindo {@link JanelaManutencaoAtivo} para {@link JanelaIndisponivel}.
 */
@Component
public class ConsultaManutencaoAtivoAdapter implements ConsultaManutencaoAtivoPort {

    private final ConsultaManutencaoAtivoService consultaManutencaoService;

    public ConsultaManutencaoAtivoAdapter(ConsultaManutencaoAtivoService consultaManutencaoService) {
        this.consultaManutencaoService = consultaManutencaoService;
    }

    @Override
    public List<JanelaIndisponivel> janelasDeManutencao(UUID ativoId, Instant inicio, Instant fim) {
        return consultaManutencaoService.janelasBloqueantes(ativoId, inicio, fim).stream()
                .map(j -> new JanelaIndisponivel(j.inicio(), j.fim()))
                .toList();
    }
}
