package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.api.v1.dto.CreateCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.HomologarCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.application.usecase.CreateCpcDecisaoUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.HomologarCpcDecisaoUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ListCpcDecisoesUseCase;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conselhos/decisoes")
@Tag(name = "Conselhos", description = "Endpoints para atas, decisões e homologações do CPP e CPC")
public class CpcDecisaoController {

    private final CreateCpcDecisaoUseCase createCpcDecisaoUseCase;
    private final HomologarCpcDecisaoUseCase homologarCpcDecisaoUseCase;
    private final ListCpcDecisoesUseCase listCpcDecisoesUseCase;

    public CpcDecisaoController(
            CreateCpcDecisaoUseCase createCpcDecisaoUseCase,
            HomologarCpcDecisaoUseCase homologarCpcDecisaoUseCase,
            ListCpcDecisoesUseCase listCpcDecisoesUseCase) {
        this.createCpcDecisaoUseCase = createCpcDecisaoUseCase;
        this.homologarCpcDecisaoUseCase = homologarCpcDecisaoUseCase;
        this.listCpcDecisoesUseCase = listCpcDecisoesUseCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista as atas e decisões dos conselhos com filtros por comunidade ou status")
    public List<CpcDecisaoResponse> list(
            @RequestParam(required = false) UUID comunidadeId,
            @RequestParam(required = false) StatusHomologacao status) {
        return listCpcDecisoesUseCase.execute(comunidadeId, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'PRESIDENTE_CPC', 'MESA_CPP', 'MEMBRO_CPC', 'MEMBRO_CPP')")
    @Operation(summary = "Registra uma nova ata ou decisão de conselho (padrão: PENDENTE_HOMOLOGACAO)")
    public CpcDecisaoResponse create(@RequestBody @Valid CreateCpcDecisaoRequest request) {
        return createCpcDecisaoUseCase.execute(request);
    }

    @PatchMapping("/{id}/homologacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'MESA_CPP')")
    @Operation(summary = "Homologa ou veta uma ata/decisão de conselho (exclusivo para Pároco, Admin ou Mesa CPP)")
    public CpcDecisaoResponse homologar(
            @PathVariable UUID id,
            @RequestBody @Valid HomologarCpcDecisaoRequest request) {
        return homologarCpcDecisaoUseCase.execute(id, request);
    }
}
