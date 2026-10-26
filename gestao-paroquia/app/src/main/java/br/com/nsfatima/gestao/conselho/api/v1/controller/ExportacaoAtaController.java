package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.application.dto.ArquivoExportado;
import br.com.nsfatima.gestao.conselho.application.dto.ConteudoExportacao;
import br.com.nsfatima.gestao.conselho.application.dto.FormatoExportacaoAta;
import br.com.nsfatima.gestao.conselho.application.dto.LayoutExportacao;
import br.com.nsfatima.gestao.conselho.application.usecase.ExportarAtaUseCase;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Exportação das atas em Word e PDF. Uma ata sai em qualquer situação que o usuário possa ler: a selada como
 * documento oficial; as demais como minuta, com a marca "MINUTA — SEM EFEITO". A compilação inclui apenas atas
 * seladas. Toda exportação é registrada na trilha de auditoria da ata. A resposta não é guardada em cache.
 */
@RestController
@RequestMapping("/api/v1/conselhos/atas")
@Tag(name = "Conselhos", description = "Atas de reunião, deliberações, homologação e exportação")
public class ExportacaoAtaController {

    private static final String NAO_AUTENTICADO = "Não autenticado";
    private static final String NAO_ENCONTRADA = "Ata não encontrada ou fora do escopo do usuário";
    private static final String PARAM_FORMATO = "Formato do arquivo: PDF ou DOCX";
    private static final String PARAM_LAYOUT = "Layout: ESTRUTURADA (seções) ou NARRATIVA (texto de Livro de Atas)";
    private static final String PARAM_CONTEUDO = "Conteúdo: ATA_COMPLETA ou SOMENTE_DECISOES";

    private final ExportarAtaUseCase exportar;

    public ExportacaoAtaController(ExportarAtaUseCase exportar) {
        this.exportar = exportar;
    }

    @GetMapping("/{id}/exportacao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Exporta uma ata em PDF ou Word",
            description = "Ata selada sai como documento oficial; as demais, como minuta sem efeito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado"),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA)
    })
    public ResponseEntity<byte[]> exportarAta(
            @Parameter(description = "Identificador da ata", required = true) @PathVariable UUID id,
            @Parameter(description = PARAM_FORMATO) @RequestParam FormatoExportacaoAta formato,
            @Parameter(description = PARAM_LAYOUT) @RequestParam(defaultValue = "ESTRUTURADA") LayoutExportacao layout,
            @Parameter(description = PARAM_CONTEUDO) @RequestParam(defaultValue = "ATA_COMPLETA")
            ConteudoExportacao conteudo) {
        return resposta(exportar.exportarAta(id, formato, layout, conteudo));
    }

    @GetMapping("/exportacao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Exporta a compilação das atas seladas de um órgão e período",
            description = "Livro de atas e decisões. Inclui apenas atas homologadas ou registradas, no escopo de "
                    + "leitura do usuário, em ordem cronológica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo gerado"),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "404", description = "Nenhuma ata selada para os filtros"),
            @ApiResponse(responseCode = "422", description = "Filtro alcança atas demais; reduza o período")
    })
    public ResponseEntity<byte[]> exportarCompilacao(
            @Parameter(description = "Órgão convocante") @RequestParam(required = false) UUID organizacaoId,
            @Parameter(description = "Tipo de reunião") @RequestParam(required = false) TipoReuniao tipo,
            @Parameter(description = "Início do período da reunião")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @Parameter(description = "Fim do período da reunião")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @Parameter(description = PARAM_FORMATO) @RequestParam FormatoExportacaoAta formato,
            @Parameter(description = PARAM_LAYOUT) @RequestParam(defaultValue = "ESTRUTURADA") LayoutExportacao layout,
            @Parameter(description = PARAM_CONTEUDO) @RequestParam(defaultValue = "ATA_COMPLETA")
            ConteudoExportacao conteudo) {
        return resposta(exportar.exportarCompilacao(organizacaoId, tipo, de, ate, formato, layout, conteudo));
    }

    private static ResponseEntity<byte[]> resposta(ArquivoExportado arquivo) {
        ContentDisposition anexo = ContentDisposition.attachment()
                .filename(arquivo.nomeArquivo(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, anexo.toString())
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(arquivo.tipoMidia()))
                .body(arquivo.conteudo());
    }
}
