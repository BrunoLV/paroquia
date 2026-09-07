package br.com.nsfatima.gestao.fiel.api.v1.controller;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.NotificacaoResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/notificacoes") public class NotificacaoController {
 private final GerenciarNotificacaoUseCase useCase; private final EventoActorContextResolver ator; public NotificacaoController(GerenciarNotificacaoUseCase u,EventoActorContextResolver a){useCase=u;ator=a;}
 @GetMapping @PreAuthorize("isAuthenticated()") public List<NotificacaoResponse> listar(){return useCase.listar(ator.resolveRequired().usuarioId());}
 @PostMapping("/{id}/marcar-lida") @ResponseStatus(HttpStatus.NO_CONTENT) @PreAuthorize("isAuthenticated()") public void marcarLida(@PathVariable UUID id){useCase.marcarLida(id,ator.resolveRequired().usuarioId());}
}
