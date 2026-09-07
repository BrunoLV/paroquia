package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.NotificacaoResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controller para consulta e marcação de leitura de notificações. */
@RestController
@RequestMapping("/api/v1/notificacoes")
public class NotificacaoController {

    private final GerenciarNotificacaoUseCase useCase;
    private final EventoActorContextResolver ator;

    public NotificacaoController(GerenciarNotificacaoUseCase useCase, EventoActorContextResolver ator) {
        this.useCase = useCase;
        this.ator = ator;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<NotificacaoResponse> listar() {
        return useCase.listar(ator.resolveRequired().usuarioId());
    }

    @PostMapping("/{id}/marcar-lida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    public void marcarLida(@PathVariable UUID id) {
        useCase.marcarLida(id, ator.resolveRequired().usuarioId());
    }
}
