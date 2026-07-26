package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.application.export.CalendarioExporter;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExportacaoCalendarioUseCase {

    private static final ZoneId BR_ZONE = ZoneId.of("America/Sao_Paulo");
    private final EventoJpaRepository eventRepository;
    private final LocalJpaRepository localRepository;
    private final OrganizacaoJpaRepository organizationRepository;
    private final ParoquiaConfiguracaoRepository paroquiaConfigRepository;
    private final List<CalendarioExporter> exporters;

    public ExportacaoCalendarioUseCase(
            EventoJpaRepository eventRepository,
            LocalJpaRepository localRepository,
            OrganizacaoJpaRepository organizationRepository,
            ParoquiaConfiguracaoRepository paroquiaConfigRepository,
            List<CalendarioExporter> exporters) {
        this.eventRepository = eventRepository;
        this.localRepository = localRepository;
        this.organizationRepository = organizationRepository;
        this.paroquiaConfigRepository = paroquiaConfigRepository;
        this.exporters = exporters;
    }

    @Transactional(readOnly = true)
    public byte[] execute(String formato, String opcao, Integer ano, Integer mes, Instant dataInicio, Instant dataFim,
                          List<String> categorias, List<UUID> organizacaoIds) {
        // 1. Determina o período de tempo (UTC)
        Instant start;
        Instant end;

        if ("ANO".equalsIgnoreCase(opcao)) {
            int targetYear = ano != null ? ano : LocalDate.now(BR_ZONE).getYear();
            start = LocalDate.of(targetYear, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();
            end = LocalDate.of(targetYear, 12, 31).atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        } else if ("MES".equalsIgnoreCase(opcao)) {
            int targetYear = ano != null ? ano : LocalDate.now(BR_ZONE).getYear();
            int targetMonth = mes != null ? mes : LocalDate.now(BR_ZONE).getMonthValue();
            LocalDate firstDay = LocalDate.of(targetYear, targetMonth, 1);
            start = firstDay.atStartOfDay(ZoneOffset.UTC).toInstant();
            end = firstDay.plusMonths(1).minusDays(1).atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();
        } else if ("PERIODO".equalsIgnoreCase(opcao)) {
            start = dataInicio != null ? dataInicio : Instant.EPOCH;
            end = dataFim != null ? dataFim : Instant.now().plusSeconds(31536000); // +1 ano default
        } else {
            throw new IllegalArgumentException("Opção de exportação inválida: " + opcao);
        }

        // 2. Busca os eventos no período com filtros adicionais
        Pageable pageable = PageRequest.of(
                0,
                100000,
                Sort.by(Sort.Direction.ASC, "inicioUtc")
        );

        List<EventoEntity> eventos = eventRepository.findAllWithFilters(
                start,
                true,
                end,
                true,
                organizacaoIds,
                organizacaoIds != null && !organizacaoIds.isEmpty(),
                null,
                false,
                null,
                false,
                categorias,
                categorias != null && !categorias.isEmpty(),
                null,
                false,
                pageable
        ).getContent();

        // 3. Carrega mapeamentos auxiliares para resolver nomes
        Map<UUID, String> localMap = localRepository.findAll().stream()
                .collect(Collectors.toMap(LocalEntity::getId, LocalEntity::getNome));

        Map<UUID, String> orgMap = organizationRepository.findAll().stream()
                .collect(Collectors.toMap(OrganizacaoEntity::getId, OrganizacaoEntity::getNome));

        // 4. Carrega a configuração da paróquia
        ParoquiaConfiguracaoEntity config = paroquiaConfigRepository.findAll().stream()
                .findFirst()
                .orElse(null);

        // 5. Executa a exportação baseada no formato desejado
        CalendarioExporter exporter = exporters.stream()
                .filter(e -> e.supports(formato))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Formato de exportação não suportado: " + formato));

        return exporter.export(eventos, localMap, orgMap, start, end, config);
    }
}
