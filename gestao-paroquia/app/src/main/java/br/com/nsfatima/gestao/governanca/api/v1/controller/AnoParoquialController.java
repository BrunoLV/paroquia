package br.com.nsfatima.gestao.governanca.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.governanca.api.v1.dto.AnoParoquialResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.UpdateAnoParoquialRequest;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.domain.service.AnoParoquialAuthorizationService;
import br.com.nsfatima.gestao.governanca.domain.service.AnoParoquialRolloverService;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Expõe a governança do Ano Paroquial: consulta do histórico/estado de anos e a transição controlada de status
 * ({@code EM_PLANEJAMENTO} -&gt; {@code FECHADO} -&gt; {@code CONCLUIDO}) que trava ou libera o planejamento anual do
 * calendário para a Matriz e as Comunidades. Apenas o Pároco/Admin ou coordenadores do Conselho podem acionar
 * transições de status (ver {@link AnoParoquialAuthorizationService}); ao concluir um ano, delega ao
 * {@link AnoParoquialRolloverService} o provisionamento automático do ano seguinte.
 */
@RestController
@RequestMapping("/api/v1/anos-paroquiais")
@Tag(name = "Gestão de Ano Paroquial", description = "Endpoints para controlar a trava do calendário anual")
public class AnoParoquialController {

    private final AnoParoquialJpaRepository repository;
    private final AnoParoquialAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;
    private final AnoParoquialRolloverService rolloverService;

    /**
     * Inicializa o controller de governança anual com as dependências para controle de acesso, persistência e
     * transição de ano.
     *
     * @param repository           Repositório JPA para gestão de anos paroquiais
     * @param authorizationService Serviço para validação de privilégios administrativos
     * @param actorContextResolver Resolvedor de contexto do ator da requisição
     * @param rolloverService      Serviço para rollover de ano paroquial
     */
    public AnoParoquialController(
            AnoParoquialJpaRepository repository,
            AnoParoquialAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver,
            AnoParoquialRolloverService rolloverService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
        this.rolloverService = rolloverService;
    }

    /**
     * Recupera o histórico de todos os anos paroquiais para permitir a análise de períodos passados e o planejamento
     * de futuros.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/anos-paroquiais}
     *
     * @return Lista de anos paroquiais registrados
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todos os anos paroquiais registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de anos paroquiais retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AnoParoquialResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @Cacheable(value = CacheConfig.ANO_PAROQUIAL_CACHE)
    public List<AnoParoquialResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Consulta o estado de um ano específico para verificar se o calendário está aberto para novas edições ou
     * travado para governança.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/anos-paroquiais/2026}
     *
     * @param ano O ano paroquial desejado
     * @return Dados do ano solicitado ou 404 se não encontrado
     */
    @GetMapping("/{ano}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consulta o estado de um ano específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ano paroquial retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = AnoParoquialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Ano paroquial não encontrado")
    })
    @Cacheable(value = CacheConfig.ANO_PAROQUIAL_CACHE, key = "#ano")
    public ResponseEntity<AnoParoquialResponse> get(
            @Parameter(description = "Ano paroquial (ex: 2026)", required = true)
            @PathVariable Integer ano) {
        return repository.findByAno(ano)
                .map(entity -> ResponseEntity.ok(toResponse(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Altera o estado de governança de um ano para controlar o ciclo de vida do planejamento paroquial
     * (abertura/fechamento).
     *
     * <p>Usage Example:
     * {@code PATCH /api/v1/anos-paroquiais/2026} com corpo {@code { "status": "FECHADO" }}
     *
     * @param ano     O ano a ser atualizado
     * @param request Novo status pretendido
     * @return Dados atualizados do ano paroquial
     * @throws SecurityException se o ator não tiver privilégios de coordenação
     */
    @PatchMapping("/{ano}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Altera o status de um ano (Trava/Destrava)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status do ano atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AnoParoquialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou transição ilegal"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @Transactional
    @CacheEvict(value = CacheConfig.ANO_PAROQUIAL_CACHE, allEntries = true)
    public ResponseEntity<AnoParoquialResponse> update(
            @Parameter(description = "Ano paroquial a ser atualizado", required = true)
            @PathVariable Integer ano,
            @Valid @RequestBody UpdateAnoParoquialRequest request) {

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanManage(actorContext);

        AnoParoquialEntity entity = repository.findByAno(ano)
                .orElseGet(() -> {
                    AnoParoquialEntity newEntity = new AnoParoquialEntity();
                    newEntity.setAno(ano);
                    return newEntity;
                });

        AnoParoquialStatus newStatus = AnoParoquialStatus.fromJson(request.status());

        AnoParoquialStatus currentStatus = entity.getStatus();

        // 1. Concluded years are immutable
        if (currentStatus == AnoParoquialStatus.CONCLUIDO) {
            throw new IllegalArgumentException("Um ano concluído não pode ser modificado.");
        }
        // 2. Succession Rule: N can only transition to FECHADO or CONCLUIDO if all prior years are CONCLUIDO
        if (newStatus == AnoParoquialStatus.FECHADO || newStatus == AnoParoquialStatus.CONCLUIDO) {
            if (repository.existsByAnoLessThanAndStatusNot(ano, AnoParoquialStatus.CONCLUIDO)) {
                throw new IllegalArgumentException(
                        "O ano anterior precisa estar concluído antes que este ano possa ser fechado ou concluído.");
            }
        }

        // 3. Temporal Enforcement: can only transition out of EM_PLANEJAMENTO during corresponding calendar year
        if ((currentStatus == null || currentStatus == AnoParoquialStatus.EM_PLANEJAMENTO)
                && (newStatus == AnoParoquialStatus.FECHADO || newStatus == AnoParoquialStatus.CONCLUIDO)) {
            int currentCivilYear = LocalDate.now().getYear();
            if (ano != currentCivilYear) {
                throw new IllegalArgumentException("Apenas o ano corrente pode ser fechado ou concluído.");
            }
        }

        if (newStatus == AnoParoquialStatus.CONCLUIDO) {
            rolloverService.concludeYear(ano);
            entity = repository.findByAno(ano).orElse(entity);
        } else {
            entity.setStatus(newStatus);

            if (newStatus == AnoParoquialStatus.FECHADO && entity.getDataFechamentoUtc() == null) {
                entity.setDataFechamentoUtc(Instant.now());
            } else if (newStatus == AnoParoquialStatus.EM_PLANEJAMENTO) {
                entity.setDataFechamentoUtc(null);
            }
            entity = repository.save(entity);
        }

        return ResponseEntity.ok(toResponse(entity));
    }

    private AnoParoquialResponse toResponse(AnoParoquialEntity entity) {
        return new AnoParoquialResponse(
                entity.getAno(),
                entity.getStatus().name(),
                entity.getDataFechamentoUtc(),
                entity.getUpdatedAt());
    }
}
