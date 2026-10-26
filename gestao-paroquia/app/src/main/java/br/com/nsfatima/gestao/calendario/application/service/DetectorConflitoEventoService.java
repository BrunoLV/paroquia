package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Serviço responsável por compilar analiticamente todos os conflitos de agenda,
 * espaços físicos e ativos patrimoniais, permitindo a deliberação do Conselho/Pároco
 * sem o lançamento de exceções punitivas (409).
 */
@Service
public class DetectorConflitoEventoService {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");

    private final ReservaConflitoService reservaConflitoService;
    private final EventoJpaRepository eventoJpaRepository;
    private final LocalRepository localRepository;
    private final AtivoPatrimonialRepository ativoRepository;

    public DetectorConflitoEventoService(
            ReservaConflitoService reservaConflitoService,
            EventoJpaRepository eventoJpaRepository) {
        this(reservaConflitoService, eventoJpaRepository, null, null);
    }

    @Autowired
    public DetectorConflitoEventoService(
            ReservaConflitoService reservaConflitoService,
            EventoJpaRepository eventoJpaRepository,
            @Autowired(required = false) LocalRepository localRepository,
            @Autowired(required = false) AtivoPatrimonialRepository ativoRepository) {
        this.reservaConflitoService = Objects.requireNonNull(
                reservaConflitoService, "ReservaConflitoService é obrigatório");
        this.eventoJpaRepository = Objects.requireNonNull(
                eventoJpaRepository, "EventoJpaRepository é obrigatório");
        this.localRepository = localRepository;
        this.ativoRepository = ativoRepository;
    }

    /**
     * Mapeia exaustivamente conflitos de local, ativos e agenda eclesial para o evento proposto.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitos(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            UUID eventoAtualId) {
        return detectarConflitos(
                inicioUtc, fimUtc, localId, ativoIds, categoria, eventoAtualId, TipoHorarioEvento.PERSONALIZADO);
    }

    /**
     * Mapeia exaustivamente conflitos de local, ativos e agenda eclesial para o evento proposto,
     * considerando o formato de horário (com buffer zerado para períodos fixos e dia inteiro).
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitos(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            UUID eventoAtualId,
            TipoHorarioEvento tipoHorario) {
        return detectarConflitos(inicioUtc, fimUtc, localId, ativoIds, categoria, eventoAtualId, tipoHorario, null);
    }

    /**
     * Mapeia exaustivamente conflitos de local, ativos e agenda eclesial para o evento proposto,
     * considerando o formato de horário e o vínculo de igreja para verificação de mesmo espaço físico.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitos(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            UUID eventoAtualId,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId) {
        return detectarConflitos(inicioUtc, fimUtc, localId, localId != null ? List.of(localId) : List.of(),
                ativoIds, categoria, eventoAtualId, tipoHorario, igrejaVinculoId);
    }

    /**
     * Mapeia exaustivamente conflitos de múltiplos locais, ativos e agenda eclesial para o evento proposto.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitos(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            UUID eventoAtualId,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId) {
        return detectarConflitos(inicioUtc, fimUtc, localId, localIds, ativoIds, categoria, eventoAtualId,
                tipoHorario, igrejaVinculoId, null);
    }

    /**
     * Mapeia exaustivamente conflitos de múltiplos locais, ativos e agenda eclesial considerando turnos
     * múltiplos (períodos).
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitos(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            UUID eventoAtualId,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId,
            List<TipoHorarioEvento> periodos) {

        if (inicioUtc == null || fimUtc == null) {
            return List.of();
        }

        if (periodos != null && !periodos.isEmpty()) {
            List<TipoHorarioEvento> turnos = periodos.stream()
                    .filter(Objects::nonNull)
                    .filter(TipoHorarioEvento::isPredefinido)
                    .distinct()
                    .toList();
            if (!turnos.isEmpty()) {
                LocalDate data = inicioUtc.atZone(BR_ZONE).toLocalDate();
                List<ConflitoEventoItemDto> todosConflitos = new ArrayList<>();
                for (TipoHorarioEvento turno : turnos) {
                    var faixa = turno.calcularIntervalo(data, BR_ZONE);
                    List<ConflitoEventoItemDto> parciais = detectarConflitosSimples(
                            faixa.inicioUtc(),
                            faixa.fimUtc(),
                            localId,
                            localIds,
                            ativoIds,
                            categoria,
                            eventoAtualId,
                            turno,
                            igrejaVinculoId
                    );
                    for (ConflitoEventoItemDto c : parciais) {
                        boolean duplicado = todosConflitos.stream().anyMatch(existente ->
                                Objects.equals(existente.tipoConflito(), c.tipoConflito())
                                        && Objects.equals(existente.recursoId(), c.recursoId())
                                        && Objects.equals(existente.eventoConcorrenteId(), c.eventoConcorrenteId())
                                        && Objects.equals(existente.inicioUtc(), c.inicioUtc()));
                        if (!duplicado) {
                            todosConflitos.add(c);
                        }
                    }
                }
                return todosConflitos;
            }
        }

        return detectarConflitosSimples(
                inicioUtc, fimUtc, localId, localIds, ativoIds, categoria, eventoAtualId, tipoHorario, igrejaVinculoId);
    }

    private List<ConflitoEventoItemDto> detectarConflitosSimples(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            UUID eventoAtualId,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId) {

        List<ConflitoEventoItemDto> conflitos = new ArrayList<>();
        int buffer = (tipoHorario != null) ? tipoHorario.getBufferMinutos() : 30;
        IntervaloReserva intervalo = IntervaloReserva.de(inicioUtc, fimUtc, buffer);
        int prioridadeSolicitada = categoria == null ? 4 : categoria.getNivelPrioridade();

        Set<UUID> todosLocais = new LinkedHashSet<>();
        if (localIds != null) {
            todosLocais.addAll(localIds);
        }
        if (localId != null) {
            todosLocais.add(localId);
        }

        // 1. Choques de Local (para cada local envolvido)
        for (UUID locId : todosLocais) {
            if (locId == null) {
                continue;
            }
            String nomeLocal = localRepository != null
                    ? localRepository.findById(locId).map(Local::getNome).orElse("Espaço Físico / Sala")
                    : "Espaço Físico / Sala";
            List<ConflitoDetalhe> choquesLocal = reservaConflitoService.detectarConflitos(
                    TipoItemReserva.LOCAL,
                    locId,
                    nomeLocal,
                    intervalo,
                    null
            );
            for (ConflitoDetalhe choque : choquesLocal) {
                conflitos.add(new ConflitoEventoItemDto(
                        "LOCAL",
                        choque.itemId(),
                        choque.itemNome(),
                        null,
                        null,
                        choque.inicioOcupado(),
                        choque.fimOcupadoComBuffer(),
                        choque.solicitanteNome(),
                        1,
                        prioridadeSolicitada,
                        choque.descricaoReserva()
                ));
            }
        }

        // 2. Choques de Ativos Patrimoniais
        if (ativoIds != null) {
            for (UUID ativoId : ativoIds) {
                if (ativoId == null) {
                    continue;
                }
                String nomeAtivo = ativoRepository != null
                        ? ativoRepository.findById(new AtivoPatrimonialId(ativoId))
                                .map(AtivoPatrimonial::getNome)
                                .orElse("Ativo Patrimonial")
                        : "Ativo Patrimonial";
                List<ConflitoDetalhe> choquesAtivo = reservaConflitoService.detectarConflitos(
                    TipoItemReserva.ATIVO,
                    ativoId,
                    nomeAtivo,
                    intervalo,
                    null
                );
                for (ConflitoDetalhe choque : choquesAtivo) {
                    conflitos.add(new ConflitoEventoItemDto(
                            "ATIVO",
                            choque.itemId(),
                            choque.itemNome(),
                            null,
                            null,
                            choque.inicioOcupado(),
                            choque.fimOcupadoComBuffer(),
                            choque.solicitanteNome(),
                            1,
                            prioridadeSolicitada,
                            choque.descricaoReserva()
                    ));
                }
            }
        }

        // 3. Conflitos de Agenda Eclesial e Prioridades
        List<EventoEntity> sobrepostos = eventoJpaRepository.findOverlappingEvents(inicioUtc, fimUtc, eventoAtualId);
        for (EventoEntity existente : sobrepostos) {
            int prioridadeExistente = existente.getCategoria() == null
                    ? 4 : existente.getCategoria().getNivelPrioridade();
            boolean mesmoEspaco = (!todosLocais.isEmpty() && (todosLocais.contains(existente.getLocalId())
                    || (existente.getLocalIds() != null
                            && existente.getLocalIds().stream().anyMatch(todosLocais::contains))))
                    || (igrejaVinculoId != null && Objects.equals(igrejaVinculoId, existente.getIgrejaVinculoId()));
            boolean isSolenidadeGlobal = prioridadeExistente == 1;

            if (isSolenidadeGlobal || mesmoEspaco) {
                if (prioridadeExistente <= prioridadeSolicitada) {
                    conflitos.add(new ConflitoEventoItemDto(
                            "AGENDA_ECLESIAL",
                            existente.getLocalId(),
                            existente.getTitulo(),
                            existente.getId(),
                            existente.getTitulo(),
                            existente.getInicioUtc(),
                            existente.getFimUtc(),
                            existente.getCriadoPor(),
                            prioridadeExistente,
                            prioridadeSolicitada,
                            existente.getDescricao()
                    ));
                }
            }
        }

        return conflitos;
    }

    /**
     * Mapeia exaustivamente conflitos para todas as ocorrências de uma série de recorrência até o fim do ano.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitosRecorrencia(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            RegraRecorrencia regra) {
        return detectarConflitosRecorrencia(
                inicioUtc, fimUtc, localId, ativoIds, categoria, regra, TipoHorarioEvento.PERSONALIZADO, null);
    }

    /**
     * Mapeia exaustivamente conflitos para todas as ocorrências de uma série de recorrência até o fim do ano,
     * respeitando o formato de horário do evento (dia inteiro, períodos ou horário específico).
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitosRecorrencia(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            RegraRecorrencia regra,
            TipoHorarioEvento tipoHorario) {
        return detectarConflitosRecorrencia(inicioUtc, fimUtc, localId, ativoIds, categoria, regra, tipoHorario, null);
    }

    /**
     * Mapeia exaustivamente conflitos para todas as ocorrências de uma série de recorrência até o fim do ano,
     * respeitando o formato de horário do evento e o vínculo territorial/eclesial de igreja.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitosRecorrencia(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            RegraRecorrencia regra,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId) {
        return detectarConflitosRecorrencia(inicioUtc, fimUtc, localId, localId != null ? List.of(localId) : List.of(),
                ativoIds, categoria, regra, tipoHorario, igrejaVinculoId);
    }

    /**
     * Mapeia exaustivamente conflitos para todas as ocorrências de uma série de recorrência até o fim do ano,
     * suportando múltiplos locais simultâneos.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitosRecorrencia(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            RegraRecorrencia regra,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId) {
        return detectarConflitosRecorrencia(inicioUtc, fimUtc, localId, localIds, ativoIds, categoria, regra,
                tipoHorario, igrejaVinculoId, null);
    }

    /**
     * Mapeia exaustivamente conflitos para todas as ocorrências de uma série de recorrência com múltiplos
     * períodos até o fim do ano.
     */
    @Transactional(readOnly = true)
    public List<ConflitoEventoItemDto> detectarConflitosRecorrencia(
            Instant inicioUtc,
            Instant fimUtc,
            UUID localId,
            List<UUID> localIds,
            List<UUID> ativoIds,
            CategoriaEvento categoria,
            RegraRecorrencia regra,
            TipoHorarioEvento tipoHorario,
            UUID igrejaVinculoId,
            List<TipoHorarioEvento> periodos) {
        if (inicioUtc == null || fimUtc == null || regra == null) {
            return detectarConflitos(inicioUtc, fimUtc, localId, localIds, ativoIds, categoria, null,
                    tipoHorario, igrejaVinculoId, periodos);
        }

        LocalDate startOfSeries = inicioUtc.atZone(BR_ZONE).toLocalDate();
        int year = startOfSeries.getYear();
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        List<LocalDate> dates = regra.gerarDatas(startOfSeries, endOfYear);

        Duration duration = Duration.between(inicioUtc, fimUtc);

        List<ConflitoEventoItemDto> todosConflitos = new ArrayList<>();
        for (LocalDate date : dates) {
            Instant occStart;
            Instant occEnd;
            if (tipoHorario != null && tipoHorario.isPredefinido()) {
                var faixa = tipoHorario.calcularIntervalo(date, BR_ZONE);
                occStart = faixa.inicioUtc();
                occEnd = faixa.fimUtc();
            } else {
                occStart = RelogioNegocio.mesmoHorarioNoDia(inicioUtc, date, BR_ZONE);
                occEnd = occStart.plus(duration);
            }
            List<ConflitoEventoItemDto> choquesOcorrencia = detectarConflitos(
                    occStart, occEnd, localId, localIds, ativoIds, categoria, null,
                    tipoHorario, igrejaVinculoId, periodos);
            todosConflitos.addAll(choquesOcorrencia);
        }
        return todosConflitos;
    }
}
