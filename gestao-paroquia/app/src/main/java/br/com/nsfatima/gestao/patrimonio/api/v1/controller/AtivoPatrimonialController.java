package br.com.nsfatima.gestao.patrimonio.api.v1.controller;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CreateAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.MetricasOciosidadeResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.TransferirAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.application.usecase.AlocarAtivoParaEventoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.CreateAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.GetMetricasOciosidadeUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ListAtivosPorOrganizacaoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.TransferirAtivoPatrimonialUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ativos")
@Tag(name = "Patrimônio", description = "Endpoints para gestão de ativos patrimoniais")
public class AtivoPatrimonialController {

    private static final String AUTHENTICATED = "isAuthenticated()";

    private final CreateAtivoPatrimonialUseCase createUseCase;
    private final ListAtivosPorOrganizacaoUseCase listUseCase;
    private final TransferirAtivoPatrimonialUseCase transferirUseCase;
    private final AlocarAtivoParaEventoUseCase alocarUseCase;
    private final GetMetricasOciosidadeUseCase metricasOciosidadeUseCase;

    public AtivoPatrimonialController(CreateAtivoPatrimonialUseCase createUseCase,
                                       ListAtivosPorOrganizacaoUseCase listUseCase,
                                       TransferirAtivoPatrimonialUseCase transferirUseCase,
                                       AlocarAtivoParaEventoUseCase alocarUseCase,
                                       GetMetricasOciosidadeUseCase metricasOciosidadeUseCase) {
        this.createUseCase = createUseCase;
        this.listUseCase = listUseCase;
        this.transferirUseCase = transferirUseCase;
        this.alocarUseCase = alocarUseCase;
        this.metricasOciosidadeUseCase = metricasOciosidadeUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Cadastra um novo ativo patrimonial (status inicial: DISPONIVEL)")
    public AtivoPatrimonialResponse create(@RequestBody @Valid CreateAtivoPatrimonialRequest request) {
        return createUseCase.execute(request);
    }

    @GetMapping("/organizacao/{organizacaoId}")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista o inventário de ativos patrimoniais de uma organização")
    public List<AtivoPatrimonialResponse> listByOrganizacao(@PathVariable UUID organizacaoId) {
        return listUseCase.execute(organizacaoId);
    }

    @PatchMapping("/{id}/transferir")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Transfere um ativo patrimonial para outra organização")
    public AtivoPatrimonialResponse transferir(@PathVariable UUID id,
                                                @RequestBody @Valid TransferirAtivoRequest request) {
        return transferirUseCase.execute(id, request);
    }

    @PostMapping("/{id}/alocar")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Aloca um ativo patrimonial a um evento, registrando a nota de uso obrigatória")
    public AtivoPatrimonialResponse alocar(@PathVariable UUID id, @RequestBody @Valid AlocarAtivoRequest request) {
        return alocarUseCase.execute(id, request);
    }

    @GetMapping("/metricas/ociosidade")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Métricas de ociosidade/alocação de ativos patrimoniais")
    public MetricasOciosidadeResponse metricasOciosidade() {
        return metricasOciosidadeUseCase.execute();
    }
}
