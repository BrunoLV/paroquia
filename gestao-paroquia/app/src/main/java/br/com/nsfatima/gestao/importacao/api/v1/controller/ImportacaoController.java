package br.com.nsfatima.gestao.importacao.api.v1.controller;

import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.application.service.ImportacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Expõe o fluxo de importação em lote em três passos — baixar template, validar arquivo
 * (dry-run) e confirmar persistência — restrito a papéis com poder administrativo/coordenativo
 * sobre a paróquia. A separação entre validar e confirmar existe para que o usuário corrija
 * erros de linha antes de qualquer gravação em banco.
 */
@RestController
@RequestMapping("/api/v1/importacao")
@Tag(name = "Importação de Arquivos", description = "Endpoints de importação em lote para Membros, Usuários, Organizações e Eventos")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    /**
     * Gera um arquivo modelo (CSV ou XLSX) com o cabeçalho de colunas e linhas de exemplo
     * esperados para o tipo de entidade informado, servindo de guia para preenchimento do
     * arquivo de importação.
     *
     * @param tipo código de {@code TipoEntidadeImportacao} (ex.: MEMBRO, GRUPO_PAROQUIAL)
     * @param formato "CSV" ou "XLSX"; qualquer outro valor recai em CSV
     * @return arquivo binário para download, com nome e content-type apropriados ao formato
     */
    @GetMapping("/modelo")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'SECRETARIO_CONSELHO')")
    @Operation(summary = "Baixa o modelo de importação", description = "Gera modelo CSV ou XLSX de exemplo para importação em lote")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo de modelo gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Tipo de entidade ou formato inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<byte[]> baixarModelo(
            @Parameter(description = "Tipo de entidade (MEMBRO, GRUPO_PAROQUIAL, etc.)")
            @RequestParam(defaultValue = "MEMBRO") String tipo,
            @Parameter(description = "Formato do arquivo (CSV ou XLSX)")
            @RequestParam(defaultValue = "CSV") String formato) throws IOException {

        byte[] data = importacaoService.gerarTemplate(tipo, formato);
        String ext = "XLSX".equalsIgnoreCase(formato) ? "xlsx" : "csv";
        String mediaType = "XLSX".equalsIgnoreCase(formato)
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "text/csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=template_" + tipo.toLowerCase() + "." + ext)

                .contentType(MediaType.parseMediaType(mediaType))
                .body(data);
    }

    @PostMapping("/validar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'SECRETARIO_CONSELHO')")
    @Operation(summary = "Validate batch file", description = "Parses and validates uploaded CSV/Excel file line by " +
            "line")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resultado da validação do arquivo em lote",
                    content = @Content(schema = @Schema(implementation = ImportacaoValidacaoResultadoDTO.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo corrompido ou formato incompatível"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<ImportacaoValidacaoResultadoDTO> validar(
            @Parameter(description = "Tipo de entidade a importar")
            @RequestParam(defaultValue = "MEMBRO") String tipo,
            @Parameter(description = "Arquivo multipart contendo a planilha de importação", required = true)
            @RequestParam("file") MultipartFile file) throws IOException {

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo(tipo, file);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/confirmar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL', 'SECRETARIO_CONSELHO')")
    @Operation(summary = "Confirm batch import", description = "Persists validated records into the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Importação concluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para importação"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<Map<String, Object>> confirmar(
            @Parameter(description = "Tipo de entidade a persistir")
            @RequestParam(defaultValue = "MEMBRO") String tipo,
            @RequestBody List<Map<String, String>> validItems,
            @AuthenticationPrincipal UserDetails userDetails) {

        String actor = userDetails != null ? userDetails.getUsername() : "system_admin";
        int totalSalvos = importacaoService.confirmarImportacao(tipo, validItems, actor);
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "registrosImportados", totalSalvos));
    }
}
