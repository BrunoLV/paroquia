package br.com.nsfatima.gestao.governanca.api.v1.controller;

import br.com.nsfatima.gestao.governanca.api.v1.dto.AnoParoquialResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.UpdateAnoParoquialRequest;
import br.com.nsfatima.gestao.governanca.domain.service.AnoParoquialAuthorizationService;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/anos-paroquiais")
@Tag(name = "Gestão de Ano Paroquial", description = "Endpoints para controlar a trava do calendário anual")
public class AnoParoquialController {

    private final AnoParoquialJpaRepository repository;
    private final AnoParoquialAuthorizationService authorizationService;
    private final EventoActorContextResolver actorContextResolver;

    /**
     * Inicializa o controller de governança anual com as dependências para controle de acesso e persistência.
     *
     * @param repository Repositório JPA para gestão de anos paroquiais
     * @param authorizationService Serviço para validação de privilégios administrativos
     * @param actorContextResolver Resolvedor de contexto do ator da requisição
     */
    public AnoParoquialController(
            AnoParoquialJpaRepository repository,
            AnoParoquialAuthorizationService authorizationService,
            EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.authorizationService = authorizationService;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Recupera o histórico de todos os anos paroquiais para permitir a análise de períodos passados e o planejamento de futuros.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/anos-paroquiais}
     *
     * @return Lista de anos paroquiais registrados
     */
    @GetMapping
    @Operation(summary = "Lista todos os anos paroquiais registrados")
    @Cacheable(value = CacheConfig.ANO_PAROQUIAL_CACHE)
    public List<AnoParoquialResponse> list() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Consulta o estado de um ano específico para verificar se o calendário está aberto para novas edições ou travado para governança.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/anos-paroquiais/2026}
     *
     * @param ano O ano paroquial desejado
     * @return Dados do ano solicitado ou 404 se não encontrado
     */
    @GetMapping("/{ano}")
    @Operation(summary = "Consulta o estado de um ano específico")
    @Cacheable(value = CacheConfig.ANO_PAROQUIAL_CACHE, key = "#ano")
    public ResponseEntity<AnoParoquialResponse> get(@PathVariable Integer ano) {
        return repository.findByAno(ano)
                .map(entity -> ResponseEntity.ok(toResponse(entity)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Altera o estado de governança de um ano para controlar o ciclo de vida do planejamento paroquial (abertura/fechamento).
     *
     * <p>Usage Example:
     * {@code PATCH /api/v1/anos-paroquiais/2026} com corpo {@code { "status": "FECHADO" }}
     *
     * @param ano O ano a ser atualizado
     * @param request Novo status pretendido
     * @return Dados atualizados do ano paroquial
     * @throws SecurityException se o ator não tiver privilégios de coordenação
     */
    @PatchMapping("/{ano}")
    @Operation(summary = "Altera o status de um ano (Trava/Destrava)")
    @Transactional
    @CacheEvict(value = CacheConfig.ANO_PAROQUIAL_CACHE, allEntries = true)
    public ResponseEntity<AnoParoquialResponse> update(
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
        entity.setStatus(newStatus.name());

        if (newStatus == AnoParoquialStatus.FECHADO && entity.getDataFechamentoUtc() == null) {
            entity.setDataFechamentoUtc(Instant.now());
        } else if (newStatus == AnoParoquialStatus.PLANEJAMENTO) {
            entity.setDataFechamentoUtc(null);
        }

        AnoParoquialEntity saved = repository.save(entity);
        return ResponseEntity.ok(toResponse(saved));
    }

    private AnoParoquialResponse toResponse(AnoParoquialEntity entity) {
        return new AnoParoquialResponse(
                entity.getAno(),
                entity.getStatus(),
                entity.getDataFechamentoUtc(),
                entity.getUpdatedAt());
    }
}
