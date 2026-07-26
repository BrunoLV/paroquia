package br.com.nsfatima.gestao.calendario.application.export;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CalendarioExporter {
    boolean supports(String formato);

    byte[] export(List<EventoEntity> eventos,
                  Map<UUID, String> localMap,
                  Map<UUID, String> orgMap,
                  Instant start,
                  Instant end,
                  ParoquiaConfiguracaoEntity config);
}
