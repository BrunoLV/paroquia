package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarDpoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.AtualizarIdentidadeVisualRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.ParoquiaConfiguracaoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.AtualizarDpoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.AtualizarIdentidadeVisualUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.AtualizarLogoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.ObterConfiguracaoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.configuracao.ObterLogoParoquiaUseCase;
import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

/**
 * Controlador REST para gestão da identidade visual e consulta dos dados institucionais consolidados da paróquia.
 */
@RestController
@RequestMapping("/api/v1/configuracao-paroquia")
@Tag(name = "Configuração e Identidade da Paróquia", description = "Endpoints para consulta institucional e gestão de cores e logotipos binarizados")
public class ParoquiaConfiguracaoController {

    private final ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase;
    private final AtualizarIdentidadeVisualUseCase atualizarIdentidadeVisualUseCase;
    private final AtualizarLogoParoquiaUseCase atualizarLogoParoquiaUseCase;
    private final ObterLogoParoquiaUseCase obterLogoParoquiaUseCase;
    private final AtualizarDpoUseCase atualizarDpoUseCase;

    @Autowired
    public ParoquiaConfiguracaoController(
            ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase,
            AtualizarIdentidadeVisualUseCase atualizarIdentidadeVisualUseCase,
            AtualizarLogoParoquiaUseCase atualizarLogoParoquiaUseCase,
            ObterLogoParoquiaUseCase obterLogoParoquiaUseCase,
            AtualizarDpoUseCase atualizarDpoUseCase) {
        this.obterConfiguracaoParoquiaUseCase = obterConfiguracaoParoquiaUseCase;
        this.atualizarIdentidadeVisualUseCase = atualizarIdentidadeVisualUseCase;
        this.atualizarLogoParoquiaUseCase = atualizarLogoParoquiaUseCase;
        this.obterLogoParoquiaUseCase = obterLogoParoquiaUseCase;
        this.atualizarDpoUseCase = atualizarDpoUseCase;
    }

    public ParoquiaConfiguracaoController(
            ObterConfiguracaoParoquiaUseCase obterConfiguracaoParoquiaUseCase,
            AtualizarIdentidadeVisualUseCase atualizarIdentidadeVisualUseCase,
            AtualizarLogoParoquiaUseCase atualizarLogoParoquiaUseCase,
            ObterLogoParoquiaUseCase obterLogoParoquiaUseCase) {
        this(obterConfiguracaoParoquiaUseCase, atualizarIdentidadeVisualUseCase,
                atualizarLogoParoquiaUseCase, obterLogoParoquiaUseCase, null);
    }

    @GetMapping
    @Operation(summary = "Obtém a configuração e identidade visual consolidada da paróquia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuração retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = ParoquiaConfiguracaoResponse.class)))
    })
    public ResponseEntity<ParoquiaConfiguracaoResponse> obterConfiguracao() {
        ParoquiaConfiguracaoResponse response = obterConfiguracaoParoquiaUseCase.execute();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Atualiza as cores institucionais da paróquia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cores atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = ParoquiaConfiguracaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o usuário autenticado")
    })
    public ResponseEntity<ParoquiaConfiguracaoResponse> atualizarIdentidadeVisual(
            @Valid @RequestBody AtualizarIdentidadeVisualRequest request) {
        ParoquiaConfiguracaoResponse response = atualizarIdentidadeVisualUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/dpo")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Atualiza os dados do Encarregado de Proteção de Dados (DPO) (Art. 41 LGPD)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do DPO atualizados com sucesso",
                    content = @Content(schema = @Schema(implementation = ParoquiaConfiguracaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o usuário autenticado")
    })
    public ResponseEntity<ParoquiaConfiguracaoResponse> atualizarDpo(
            @Valid @RequestBody AtualizarDpoRequest request) {
        if (atualizarDpoUseCase == null) {
            return ResponseEntity.ok(obterConfiguracaoParoquiaUseCase.execute());
        }
        ParoquiaConfiguracaoResponse response = atualizarDpoUseCase.execute(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logo/{variante}")
    @Operation(summary = "Obtém o logotipo oficial da paróquia binarizado de acordo com a variante de contraste (claro ou escuro)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logotipo binarizado recuperado com sucesso",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Nenhum logotipo cadastrado para a variante informada")
    })
    public ResponseEntity<byte[]> obterLogo(
            @Parameter(description = "Variante de fundo: 'claro' ou 'escuro'", example = "claro")
            @PathVariable String variante) {
        VarianteFundoLogo enumVariante = VarianteFundoLogo.fromString(variante);
        return obterLogoParoquiaUseCase.execute(enumVariante)
                .map(logo -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(logo.contentType()))
                        .header(HttpHeaders.CACHE_CONTROL,
                                CacheControl.maxAge(Duration.ofHours(1)).cachePublic().getHeaderValue())
                        .body(logo.bytes()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping(value = "/logo/{variante}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Realiza o upload e atualização do logotipo binarizado da paróquia para a variante especificada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logotipo atualizado e binarizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ParoquiaConfiguracaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido ou excede o tamanho máximo de 2MB"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para o usuário autenticado")
    })
    public ResponseEntity<ParoquiaConfiguracaoResponse> atualizarLogo(
            @Parameter(description = "Variante de fundo: 'claro' ou 'escuro'", example = "claro")
            @PathVariable String variante,
            @Parameter(description = "Arquivo de imagem a ser binarizado (PNG, JPEG, WebP, SVG; máximo 2MB)")
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        VarianteFundoLogo enumVariante = VarianteFundoLogo.fromString(variante);
        byte[] bytes = arquivo.getBytes();
        String contentType = arquivo.getContentType();
        ParoquiaConfiguracaoResponse response = atualizarLogoParoquiaUseCase.execute(enumVariante, bytes, contentType);
        return ResponseEntity.ok(response);
    }
}
