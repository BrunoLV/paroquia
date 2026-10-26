package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoIdempotenciaException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoIdempotencyEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoIdempotencyJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Serviço de idempotência para a criação de eventos: garante que uma requisição de
 * criação repetida com a mesma {@code Idempotency-Key} (ex.: retry de cliente após
 * timeout de rede) não gere um evento duplicado, retornando a resposta já persistida
 * na primeira execução em vez de reexecutar a ação de criação.
 */
@Service
public class EventoIdempotencyService {

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

    /**
     * Executa a ação de criação de evento de forma idempotente: se a chave já foi usada
     * com um payload idêntico (mesmo hash), retorna o evento previamente criado sem
     * reexecutar {@code createAction}; se a chave já foi usada com um payload diferente,
     * rejeita a requisição para evitar sobrescrever a intenção original do cliente.
     *
     * @param idempotencyKey chave de idempotência fornecida pelo cliente (não nula)
     * @param request payload da requisição de criação, usado para calcular o hash de
     *                 comparação entre chamadas repetidas
     * @param createAction ação que efetivamente cria o evento, executada apenas na
     *                       primeira chamada para uma dada chave
     * @return resultado contendo a resposta do evento e um indicador de replay (se a
     *         resposta veio de uma execução anterior em vez de uma nova criação)
     * @throws ConflitoIdempotenciaException se a chave já foi usada com um payload
     *                                        de hash diferente do atual
     */
    /**
     * Verifica previamente se a requisição já foi executada para a mesma {@code Idempotency-Key}.
     * Se existir com payload idêntico, retorna o resultado salvo para replay; se o payload divergir,
     * lança {@link ConflitoIdempotenciaException}.
     *
     * @param idempotencyKey chave de idempotência fornecida pelo cliente
     * @param request payload da requisição de criação
     * @return {@link Optional} com o resultado pré-existente ou vazio se for a primeira submissão
     */
    public Optional<IdempotencyResult> findExisting(String idempotencyKey, CriarEventoRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || request == null) {
            return Optional.empty();
        }
        String requestHash = hashRequest(request);
        Optional<EventoIdempotencyEntity> existing = idempotencyRepository.findById(idempotencyKey);
        if (existing.isPresent()) {
            EventoIdempotencyEntity existingRecord = existing.get();
            if (!existingRecord.getRequestHash().equals(requestHash)) {
                throw new ConflitoIdempotenciaException(
                        "Idempotency-Key already used with a different payload");
            }

            EventoEntity evento = eventoRepository.findById(Objects.requireNonNull(existingRecord.getEventoId()))
                    .orElseThrow(() -> new IllegalStateException("Idempotent event not found"));
            return Optional.of(new IdempotencyResult(eventoMapper.toResponse(evento), true));
        }
        return Optional.empty();
    }

    public IdempotencyResult execute(String idempotencyKey, CriarEventoRequest request,
                                     Supplier<EventoResponse> createAction) {
        String key = Objects.requireNonNull(idempotencyKey);
        Optional<IdempotencyResult> existing = findExisting(key, request);
        if (existing.isPresent()) {
            return existing.get();
        }

        String requestHash = hashRequest(request);
        EventoResponse created = createAction.get();

        EventoIdempotencyEntity record = new EventoIdempotencyEntity();
        record.setIdempotencyKey(key);
        record.setRequestHash(requestHash);
        record.setEventoId(created.id());
        record.setResponseStatus(201);
        idempotencyRepository.save(record);

        return new IdempotencyResult(created, false);
    }

    private String hashRequest(CriarEventoRequest request) {
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

    /**
     * Resultado da execução idempotente: a resposta do evento e se ela foi obtida por
     * replay de uma chamada anterior ({@code replay = true}) ou por uma criação nova.
     *
     * @param response evento criado (ou recuperado, em caso de replay)
     * @param replay {@code true} se a resposta veio de uma execução anterior com a
     *                mesma chave de idempotência
     */
    public record IdempotencyResult(EventoResponse response, boolean replay) {
    }
}
