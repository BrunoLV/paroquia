package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoIdempotencyEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoIdempotencyJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class EventoIdempotencyService {

    public record IdempotencyResult(EventoResponse response, boolean replay) {
    }

    private final EventoIdempotencyJpaRepository idempotencyRepository;
    private final EventoJpaRepository eventoRepository;
    private final EventoMapper eventoMapper;

    public EventoIdempotencyService(
            EventoIdempotencyJpaRepository idempotencyRepository,
            EventoJpaRepository eventoRepository,
            EventoMapper eventoMapper) {
        this.idempotencyRepository = idempotencyRepository;
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
    }

    public IdempotencyResult execute(String idempotencyKey, CreateEventoRequest request,
            Supplier<EventoResponse> createAction) {
        String key = Objects.requireNonNull(idempotencyKey);
        String requestHash = hashRequest(request);
        Optional<EventoIdempotencyEntity> existing = idempotencyRepository.findById(key);
        if (existing.isPresent()) {
            EventoIdempotencyEntity existingRecord = existing.get();
            if (!existingRecord.getRequestHash().equals(requestHash)) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key already used with a different payload");
            }

            EventoEntity evento = eventoRepository.findById(Objects.requireNonNull(existingRecord.getEventoId()))
                    .orElseThrow(() -> new IllegalStateException("Idempotent event not found"));
            return new IdempotencyResult(eventoMapper.toResponse(evento), true);
        }

        EventoResponse created = createAction.get();

        EventoIdempotencyEntity record = new EventoIdempotencyEntity();
        record.setIdempotencyKey(key);
        record.setRequestHash(requestHash);
        record.setEventoId(created.id());
        record.setResponseStatus(201);
        record.setCreatedAt(Instant.now());
        idempotencyRepository.save(record);

        return new IdempotencyResult(created, false);
    }

    private String hashRequest(CreateEventoRequest request) {
        String canonical = String.join("|",
                nullSafe(request.titulo()),
                nullSafe(request.descricao()),
                request.organizacaoResponsavelId() == null ? "" : request.organizacaoResponsavelId().toString(),
                request.inicio() == null ? "" : request.inicio().toString(),
                request.fim() == null ? "" : request.fim().toString(),
                request.status() == null ? "" : request.status().name(),
                nullSafe(request.adicionadoExtraJustificativa()));

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to compute request hash", ex);
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
