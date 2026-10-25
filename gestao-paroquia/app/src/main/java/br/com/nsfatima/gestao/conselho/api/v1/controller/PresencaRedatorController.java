package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.api.v1.dto.PresencaRedatorResponse;
import br.com.nsfatima.gestao.conselho.application.usecase.ObterPresencaRedatorUseCase;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Presença do próprio redator numa ata nova: quem ele é na lista e com que função deve constar. */
@RestController
@RequestMapping("/api/v1/conselhos/redator-presenca")
@Tag(name = "Conselhos", description = "Atas de reunião, deliberações, homologação e exportação")
public class PresencaRedatorController {

    private final ObterPresencaRedatorUseCase useCase;

    public PresencaRedatorController(ObterPresencaRedatorUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Presença do redator na ata",
            description = "Fiel do usuário autenticado e a função (Secretário(a) ou Coordenador(a)) com que deve "
                    + "constar como presente na lista da ata do tipo, no órgão convocante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presença do redator"),
            @ApiResponse(responseCode = "204", description = "O usuário não redige este tipo neste órgão, "
                    + "ou não tem cadastro de fiel"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<PresencaRedatorResponse> obter(
            @Parameter(description = "Tipo de reunião") @RequestParam TipoReuniao tipo,
            @Parameter(description = "Órgão convocante") @RequestParam UUID organizacaoId) {
        return useCase.execute(tipo, organizacaoId)
                .map(p -> ResponseEntity.ok(new PresencaRedatorResponse(p.fielId(), p.funcao())))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
