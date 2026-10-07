package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class EnriquecedorReservaService {

    private final LocalJpaRepository localRepository;
    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final EventoJpaRepository eventoRepository;

    public EnriquecedorReservaService(
            LocalJpaRepository localRepository,
            AtivoPatrimonialJpaRepository ativoRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            UsuarioJpaRepository usuarioRepository,
            EventoJpaRepository eventoRepository
    ) {
        this.localRepository = localRepository;
        this.ativoRepository = ativoRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventoRepository = eventoRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservaDetalheDto> enriquecer(List<Reserva> reservas) {
        if (reservas == null || reservas.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> localIds = new HashSet<>();
        Set<UUID> ativoIds = new HashSet<>();
        Set<UUID> orgIds = new HashSet<>();
        Set<UUID> usuarioIds = new HashSet<>();
        Set<UUID> eventoIds = new HashSet<>();

        for (Reserva r : reservas) {
            if (r.getTipoItem() == TipoItemReserva.LOCAL) {
                localIds.add(r.getItemId());
            } else if (r.getTipoItem() == TipoItemReserva.ATIVO) {
                ativoIds.add(r.getItemId());
            }
            if (r.getPastoralMovimentoId() != null) {
                orgIds.add(r.getPastoralMovimentoId());
            }
            if (r.getSolicitanteId() != null) {
                usuarioIds.add(r.getSolicitanteId());
            }
            if (r.getAprovadorId() != null) {
                usuarioIds.add(r.getAprovadorId());
            }
            if (r.getCanceladoPorId() != null) {
                usuarioIds.add(r.getCanceladoPorId());
            }
            if (r.getEventoId() != null) {
                eventoIds.add(r.getEventoId());
            }
        }

        Map<UUID, String> nomesLocais = new HashMap<>();
        if (!localIds.isEmpty()) {
            for (LocalEntity e : localRepository.findAllById(localIds)) {
                nomesLocais.put(e.getId(), e.getNome());
            }
        }

        Map<UUID, String> nomesAtivos = new HashMap<>();
        if (!ativoIds.isEmpty()) {
            for (AtivoPatrimonialEntity e : ativoRepository.findAllById(ativoIds)) {
                nomesAtivos.put(e.getId(), e.getNome());
            }
        }

        Map<UUID, String> nomesOrgs = new HashMap<>();
        if (!orgIds.isEmpty()) {
            for (OrganizacaoEntity e : organizacaoRepository.findAllById(orgIds)) {
                nomesOrgs.put(e.getId(), e.getNome());
            }
        }

        Map<UUID, String> nomesUsuarios = new HashMap<>();
        if (!usuarioIds.isEmpty()) {
            for (UsuarioEntity e : usuarioRepository.findAllById(usuarioIds)) {
                nomesUsuarios.put(e.getId(), e.getUsername());
            }
        }

        Map<UUID, String> titulosEventos = new HashMap<>();
        if (!eventoIds.isEmpty()) {
            for (EventoEntity e : eventoRepository.findAllById(eventoIds)) {
                titulosEventos.put(e.getId(), e.getTitulo());
            }
        }

        List<ReservaDetalheDto> dtos = new ArrayList<>(reservas.size());
        for (Reserva r : reservas) {
            String itemNome = r.getTipoItem() == TipoItemReserva.LOCAL
                    ? nomesLocais.getOrDefault(r.getItemId(), "Local " + r.getItemId())
                    : nomesAtivos.getOrDefault(r.getItemId(), "Ativo " + r.getItemId());

            dtos.add(new ReservaDetalheDto(
                    r.getId().value(),
                    r.getTipoItem(),
                    r.getItemId(),
                    itemNome,
                    r.getIntervalo().dataHoraInicio(),
                    r.getIntervalo().dataHoraFim(),
                    r.getIntervalo().bufferMinutos(),
                    r.getIntervalo().fimComBuffer(),
                    r.getDescricao(),
                    r.getSolicitanteId(),
                    nomesUsuarios.getOrDefault(r.getSolicitanteId(), "Usuário"),
                    r.getPastoralMovimentoId(),
                    nomesOrgs.getOrDefault(r.getPastoralMovimentoId(), "Pastoral"),
                    r.getEventoId(),
                    r.getEventoId() != null ? titulosEventos.getOrDefault(r.getEventoId(), "Evento") : null,
                    r.getGrupoRecorrenciaId(),
                    r.getStatus(),
                    r.getAprovadorId(),
                    r.getAprovadorId() != null ? nomesUsuarios.get(r.getAprovadorId()) : null,
                    r.getAprovadoEm(),
                    r.getMotivoCancelamento(),
                    r.getCanceladoPorId(),
                    r.getCanceladoPorId() != null ? nomesUsuarios.get(r.getCanceladoPorId()) : null,
                    r.getCheckAvarias(),
                    r.getCreatedAt(),
                    r.getUpdatedAt()
            ));
        }

        return dtos;
    }

    @Transactional(readOnly = true)
    public ReservaDetalheDto enriquecer(Reserva reserva) {
        if (reserva == null) {
            return null;
        }
        List<ReservaDetalheDto> lista = enriquecer(List.of(reserva));
        return lista.isEmpty() ? null : lista.get(0);
    }
}
