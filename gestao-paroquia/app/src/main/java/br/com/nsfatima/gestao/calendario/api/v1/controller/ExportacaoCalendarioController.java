package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.application.usecase.evento.ExportacaoCalendarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Expõe a exportação do calendário de eventos em formatos de arquivo (PDF, Excel, CSV) para apoiar
 * a divulgação e o arquivamento externo da agenda paroquial, aplicando os mesmos filtros de período
 * e organização usados na listagem de eventos.
 */
@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Exportação de Calendário", description = "Endpoints para exportar eventos do calendário")
public class ExportacaoCalendarioController {

    private final ExportacaoCalendarioUseCase exportUseCase;

    public ExportacaoCalendarioController(ExportacaoCalendarioUseCase exportUseCase) {
        this.exportUseCase = exportUseCase;
    }

    /**
     * Gera e retorna o arquivo de exportação do calendário no formato solicitado, aplicando o filtro
     * de período (ano, mês ou intervalo customizado) e, opcionalmente, restringindo por categorias e
     * organizações responsáveis. O nome do arquivo é derivado da opção de filtragem para facilitar a
     * identificação do conteúdo exportado.
     *
     * @param formato formato de saída: PDF, EXCEL ou CSV (case-insensitive)
     * @param opcao modo de filtragem por período: ANO, MES ou PERIODO
     * @param ano ano de referência, obrigatório quando {@code opcao} é ANO ou MES
     * @param mes mês de referência (1-12), obrigatório quando {@code opcao} é MES
     * @param dataInicio início do intervalo customizado, usado quando {@code opcao} é PERIODO
     * @param dataFim fim do intervalo customizado, usado quando {@code opcao} é PERIODO
     * @param categorias categorias de evento para restringir a exportação; se omitido, inclui todas
     * @param organizacaoIds organizações responsáveis para restringir a exportação; se omitido, inclui todas
     * @return o arquivo exportado com os cabeçalhos de content-type e content-disposition apropriados,
     *     ou {@code 400 Bad Request} caso o formato informado não seja reconhecido
     */
    @GetMapping("/exportar")
    @Operation(summary = "Exportar calendário", description = "Exporta os eventos do calendário nos formatos PDF, " +
            "Excel ou CSV com base em filtros de período.")
    public ResponseEntity<byte[]> exportar(
            @Parameter(description = "Formato de exportação: PDF, EXCEL, CSV") @RequestParam String formato,
            @Parameter(description = "Opção de filtragem: ANO, MES, PERIODO") @RequestParam String opcao,
            @Parameter(description = "Ano para exportação (ex: 2026)") @RequestParam(required = false) Integer ano,
            @Parameter(description = "Mês para exportação (1-12)") @RequestParam(required = false) Integer mes,
            @Parameter(description = "Data inicial para período customizado (ISO-8601)") @RequestParam(required = false)
            Instant dataInicio,
            @Parameter(description = "Data final para período customizado (ISO-8601)") @RequestParam(required = false)
            Instant dataFim,
            @Parameter(description = "Filtrar por múltiplas categorias") @RequestParam(required = false)
            java.util.List<String> categorias,
            @Parameter(description = "Filtrar por organizações responsáveis") @RequestParam(required = false)
            java.util.List<UUID> organizacaoIds) {

        byte[] content = exportUseCase.execute(formato,
                opcao, ano, mes, dataInicio, dataFim, categorias, organizacaoIds);


        String filename = "calendario_" + opcao.toLowerCase();
        if ("ANO".equalsIgnoreCase(opcao) && ano != null) {
            filename += "_" + ano;
        } else if ("MES".equalsIgnoreCase(opcao) && ano != null && mes != null) {
            filename += "_" + ano + "_" + String.format("%02d", mes);
        }

        String mediaType;
        String extension;

        if ("CSV".equalsIgnoreCase(formato)) {
            mediaType = "text/csv; charset=UTF-8";
            extension = ".csv";
        } else if ("EXCEL".equalsIgnoreCase(formato)) {
            mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            extension = ".xlsx";
        } else if ("PDF".equalsIgnoreCase(formato)) {
            mediaType = "application/pdf";
            extension = ".pdf";
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + extension + "\"")
                .header(HttpHeaders.CONTENT_TYPE, mediaType)
                .body(content);
    }
}
