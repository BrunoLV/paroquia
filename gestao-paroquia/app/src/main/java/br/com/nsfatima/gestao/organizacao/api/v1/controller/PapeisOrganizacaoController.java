package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.PapelOrganizacionalResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Papéis de vínculo admitidos em cada tipo de organização, para a interface oferecer só os válidos. */
@RestController
@RequestMapping("/api/v1/organizacoes/papeis")
@Tag(name = "Organizações", description = "Hierarquia de pastorais, movimentos, conselhos e serviços")
public class PapeisOrganizacaoController {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Papéis de vínculo por tipo de organização",
            description = "Fonte única usada para criar vínculos, promover coordenadores e autorizar no calendário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Papéis por tipo"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public Map<TipoOrganizacao, List<PapelOrganizacionalResponse>> listar() {
        Map<TipoOrganizacao, List<PapelOrganizacionalResponse>> porTipo = new EnumMap<>(TipoOrganizacao.class);
        for (TipoOrganizacao tipo : TipoOrganizacao.values()) {
            porTipo.put(tipo, Arrays.stream(PapelOrganizacional.values())
                    .filter(papel -> PapeisPorTipoOrganizacao.aceita(tipo, papel))
                    .map(papel -> new PapelOrganizacionalResponse(papel.name(), papel.getDescricao())).toList());
        }
        return porTipo;
    }
}
