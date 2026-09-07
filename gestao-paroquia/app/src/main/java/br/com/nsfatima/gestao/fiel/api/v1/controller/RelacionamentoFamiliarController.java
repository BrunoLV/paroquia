package br.com.nsfatima.gestao.fiel.api.v1.controller;
import br.com.nsfatima.gestao.fiel.api.v1.dto.*;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarRelacionamentoFamiliarUseCase;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/** Gestão administrativa dos vínculos familiares ativos. */
@RestController @RequestMapping("/api/v1/fieis/{fielId}/familiares") public class RelacionamentoFamiliarController {
 private final GerenciarRelacionamentoFamiliarUseCase useCase; private final EventoActorContextResolver ator; public RelacionamentoFamiliarController(GerenciarRelacionamentoFamiliarUseCase u, EventoActorContextResolver a){useCase=u;ator=a;}
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')") public List<RelacionamentoFamiliarResponse> listar(@PathVariable UUID fielId){return useCase.listar(fielId);}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')") public UUID criar(@PathVariable UUID fielId,@RequestBody @Valid CriarRelacionamentoFamiliarRequest request){return useCase.criar(fielId,request.fielDestinoId(),request.tipo(),ator.resolveRequired().usuarioId());}
 @DeleteMapping("/{relacaoId}") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')") public void remover(@PathVariable UUID relacaoId){useCase.remover(relacaoId,ator.resolveRequired().usuarioId());}
}
