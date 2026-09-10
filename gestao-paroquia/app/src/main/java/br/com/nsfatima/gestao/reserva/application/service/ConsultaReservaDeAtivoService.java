package br.com.nsfatima.gestao.reserva.application.service;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.repository.ReservaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * API pública do módulo de Reservas para o contexto de patrimônio: responde às três perguntas que
 * patrimônio precisa fazer sobre as reservas de um ativo, sem expor o agregado {@code Reserva}.
 */
@Service
public class ConsultaReservaDeAtivoService {

    private final ReservaJpaRepository reservaRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public ConsultaReservaDeAtivoService(ReservaJpaRepository reservaRepository,
                                         UsuarioJpaRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public boolean temReservaAtivaAgora(UUID ativoId) {
        return reservaRepository.existeReservaAtivaNoInstante(ativoId, Instant.now());
    }

    @Transactional(readOnly = true)
    public boolean temReservaFuturaImpeditiva(UUID ativoId) {
        return reservaRepository.existeReservaFuturaImpeditiva(ativoId, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<ReservaImpeditivaView> reservasImpeditivas(UUID ativoId, Instant inicio, Instant fim) {
        List<ReservaEntity> reservas = reservaRepository.findImpeditivasParaManutencao(ativoId, inicio, fim);
        Map<UUID, String> nomes = resolverNomesSolicitantes(reservas);
        return reservas.stream()
                .map(r -> new ReservaImpeditivaView(
                        r.getId(),
                        r.getDescricao(),
                        r.getDataHoraInicio(),
                        r.getDataHoraFim(),
                        r.getStatus(),
                        nomes.getOrDefault(r.getSolicitanteId(), "Solicitante " + r.getSolicitanteId())))
                .toList();
    }

    private Map<UUID, String> resolverNomesSolicitantes(List<ReservaEntity> reservas) {
        Set<UUID> ids = reservas.stream()
                .map(ReservaEntity::getSolicitanteId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        Map<UUID, String> nomes = new HashMap<>();
        if (!ids.isEmpty()) {
            for (UsuarioEntity u : usuarioRepository.findAllById(ids)) {
                nomes.put(u.getId(), u.getUsername());
            }
        }
        return nomes;
    }
}
