package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.application.usecase.evento.ExportacaoCalendarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Expõe a exportação do calendário de eventos em formatos de arquivo (PDF, Excel, CSV, PowerPoint) para apoiar
 * a divulgação e o arquivamento externo da agenda paroquial, aplicando os mesmos filtros de período
 * e organização usados na listagem de eventos.
 */
@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Exportação de Calendário", description = "Endpoints para exportar eventos do calendário")
public class ExportacaoCalendarioController {

    private final ExportacaoCalendarioUseCase exportacaoCalendarioUseCase;

    public ExportacaoCalendarioController(ExportacaoCalendarioUseCase exportacaoCalendarioUseCase) {
        this.exportacaoCalendarioUseCase = exportacaoCalendarioUseCase;
    }

    /**
     * Gera e devolve um arquivo (PDF, Excel ou CSV) com os eventos que atendem aos filtros informados.
     * O arquivo é enviado diretamente no corpo da resposta com o header {@code Content-Disposition}
     * para download.
     *
     * @param formato formato de saída (PDF, EXCEL, CSV ou POWERPOINT); padrão PDF
     * @param opcao recorte temporal (MES, ANO ou CUSTOMIZADO); padrão MES
     * @param ano ano de referência para recortes MES e ANO
     * @param mes mês de referência (1-12) para recorte MES
     * @param dataInicio início do período para recorte CUSTOMIZADO (ISO-8601)
     * @param dataFim fim do período para recorte CUSTOMIZADO (ISO-8601)
     * @param categorias lista opcional de categorias para filtrar
     * @param grupoParoquialIds lista opcional de organizações para filtrar
     * @param visualizacao modo de apresentação (AGENDA ou GRADE); só afeta PDF/POWERPOINT
     * @return arquivo gerado para download
     */
    @GetMapping("/exportar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Exporta o calendário de eventos em arquivo (PDF, Excel, CSV ou PowerPoint)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo exportado com sucesso",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "400", description = "Parâmetros de exportação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<byte[]> exportar(
            @Parameter(description = "Formato do arquivo (PDF, EXCEL, CSV ou POWERPOINT)") @RequestParam(defaultValue = "PDF")
            String formato,
            @Parameter(description = "Tipo de período (MES, ANO ou CUSTOMIZADO)") @RequestParam(defaultValue = "MES")
            String opcao,
            @Parameter(description = "Ano para exportação (ex: 2026)") @RequestParam(required = false) Integer ano,
            @Parameter(description = "Mês para exportação (1-12)") @RequestParam(required = false) Integer mes,
            @Parameter(description = "Data inicial para período customizado (ISO-8601)") @RequestParam(required = false)
            Instant dataInicio,
            @Parameter(description = "Data final para período customizado (ISO-8601)") @RequestParam(required = false)
            Instant dataFim,
            @Parameter(description = "Filtrar por múltiplas categorias") @RequestParam(required = false)
            List<String> categorias,
            @Parameter(description = "Filtrar por organizações responsáveis") @RequestParam(required = false)
            List<UUID> grupoParoquialIds,
            @Parameter(description = "Visualização (AGENDA ou GRADE); só se aplica a PDF/POWERPOINT")
            @RequestParam(defaultValue = "AGENDA") String visualizacao) {

        byte[] conteudo = exportacaoCalendarioUseCase.execute(formato,
                opcao, ano, mes, dataInicio, dataFim, categorias, grupoParoquialIds, visualizacao);


        String nomeArquivo = "calendario_" + opcao.toLowerCase();
        if ("ANO".equalsIgnoreCase(opcao) && ano != null) {
            nomeArquivo += "_" + ano;
        } else if ("MES".equalsIgnoreCase(opcao) && ano != null && mes != null) {
            nomeArquivo += "_" + ano + "_" + String.format("%02d", mes);
        }

        String tipoMidia;
        String extensao;

        if ("CSV".equalsIgnoreCase(formato)) {
            tipoMidia = "text/csv; charset=UTF-8";
            extensao = ".csv";
        } else if ("EXCEL".equalsIgnoreCase(formato)) {
            tipoMidia = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            extensao = ".xlsx";
        } else if ("PDF".equalsIgnoreCase(formato)) {
            tipoMidia = "application/pdf";
            extensao = ".pdf";
        } else if ("POWERPOINT".equalsIgnoreCase(formato)) {
            tipoMidia = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            extensao = ".pptx";
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + extensao + "\"")
                .header(HttpHeaders.CONTENT_TYPE, tipoMidia)
                .body(conteudo);
    }
}
