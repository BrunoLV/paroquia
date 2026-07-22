package br.com.nsfatima.gestao.importacao.api.v1.controller;

import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.domain.service.ImportacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/importacao")
@Tag(name = "File Importation", description = "Bulk file import endpoints for Members, Users, Organizations, and Events")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Download import template", description = "Generates CSV or XLSX sample template for batch import")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestParam(defaultValue = "MEMBRO") String tipo,
            @RequestParam(defaultValue = "CSV") String formato) throws IOException {

        byte[] data = importacaoService.gerarTemplate(tipo, formato);
        String ext = "XLSX".equalsIgnoreCase(formato) ? "xlsx" : "csv";
        String mediaType = "XLSX".equalsIgnoreCase(formato)
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "text/csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template_" + tipo.toLowerCase() + "." + ext)
                .contentType(MediaType.parseMediaType(mediaType))
                .body(data);
    }

    @PostMapping("/validar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Validate batch file", description = "Parses and validates uploaded CSV/Excel file line by line")
    public ResponseEntity<ImportacaoValidacaoResultadoDTO> validar(
            @RequestParam(defaultValue = "MEMBRO") String tipo,
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo(tipo, file);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Confirm batch import", description = "Persists validated records into the system")
    public ResponseEntity<Map<String, Object>> confirmar(
            @RequestParam(defaultValue = "MEMBRO") String tipo,
            @RequestBody List<Map<String, String>> validItems,
            @AuthenticationPrincipal UserDetails userDetails) {

        String actor = userDetails != null ? userDetails.getUsername() : "system_admin";
        int totalSalvos = importacaoService.confirmarImportacao(tipo, validItems, actor);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "registrosImportados", totalSalvos));
    }
}
