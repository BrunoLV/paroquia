package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.api.v1.dto.GovernoEclesiasticoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.GovernoEclesiasticoResponse;
import br.com.nsfatima.gestao.clero.domain.model.GovernoEclesiastico;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.DioceseEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.GovernoEclesiasticoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.DioceseJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.GovernoEclesiasticoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class GerenciarGovernoEclesiasticoUseCase {

    private final GovernoEclesiasticoJpaRepository repository;
    private final DioceseJpaRepository dioceseRepository;
    private final ClerigoJpaRepository clerigoRepository;
    private final AuditoriaPersistenceService auditLogService;

    public GerenciarGovernoEclesiasticoUseCase(
            GovernoEclesiasticoJpaRepository repository,
            DioceseJpaRepository dioceseRepository,
            ClerigoJpaRepository clerigoRepository,
            AuditoriaPersistenceService auditLogService) {
        this.repository = repository;
        this.dioceseRepository = dioceseRepository;
        this.clerigoRepository = clerigoRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Optional<GovernoEclesiasticoResponse> obter() {
        return repository.findFirstByOrderByCriadoEmUtcDesc()
                .map(this::toResponse);
    }

    public GovernoEclesiasticoResponse salvar(GovernoEclesiasticoRequest request) {
        DioceseEntity diocese = dioceseRepository.findById(request.dioceseId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Diocese não encontrada com ID: " + request.dioceseId()));

        if (request.parocoId() != null && !clerigoRepository.existsById(request.parocoId())) {
            throw new IllegalArgumentException("Clérigo não encontrado com ID: " + request.parocoId());
        }

        GovernoEclesiasticoEntity entity = repository.findFirstByOrderByCriadoEmUtcDesc()
                .orElseGet(() -> {
                    GovernoEclesiasticoEntity novo = new GovernoEclesiasticoEntity();
                    novo.setId(UUID.randomUUID());
                    return novo;
                });

        GovernoEclesiastico domain = new GovernoEclesiastico(
                entity.getId(),
                request.dioceseId(),
                request.parocoId(),
                request.dataPosseParoco(),
                request.decretoErecaoParoquia(),
                request.dataInstalacaoParoquia(),
                request.livroTomboNumero()
        );

        entity.setDioceseId(domain.getDioceseId());
        entity.setParocoId(domain.getParocoId());
        entity.setDataPosseParoco(domain.getDataPosseParoco());
        entity.setDecretoErecaoParoquia(domain.getDecretoErecaoParoquia());
        entity.setDataInstalacaoParoquia(domain.getDataInstalacaoParoquia());
        entity.setLivroTomboNumero(domain.getLivroTomboNumero());

        repository.save(entity);

        auditLogService.log("admin", "salvar-governo-eclesiastico", "governo_eclesiastico", "sucesso", Map.of(
                "governoId", entity.getId(),
                "dioceseId", diocese.getId(),
                "dioceseNome", diocese.getNome()
        ));

        return toResponse(entity);
    }

    private GovernoEclesiasticoResponse toResponse(GovernoEclesiasticoEntity entity) {
        String dioceseNome = null;
        String bispoDiocesano = null;
        var dioceseOpt = dioceseRepository.findById(entity.getDioceseId());
        if (dioceseOpt.isPresent()) {
            dioceseNome = dioceseOpt.get().getNome();
            bispoDiocesano = dioceseOpt.get().getBispoDiocesano();
        }

        String parocoNome = null;
        if (entity.getParocoId() != null) {
            parocoNome = clerigoRepository.findById(entity.getParocoId())
                    .map(c -> c.getNomeEclesiastico())
                    .orElse(null);
        }

        return new GovernoEclesiasticoResponse(
                entity.getId(),
                entity.getDioceseId(),
                dioceseNome,
                bispoDiocesano,
                entity.getParocoId(),
                parocoNome,
                parocoNome,
                entity.getDataPosseParoco(),
                entity.getDecretoErecaoParoquia(),
                entity.getDataInstalacaoParoquia(),
                entity.getLivroTomboNumero()
        );
    }
}
