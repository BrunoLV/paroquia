package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.DecidirAjusteCadastralRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitacaoAjusteCadastralResponse;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitarAjusteCadastralRequest;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarAjusteCadastralUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.GetFielUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Fila de moderação para alterações solicitadas pelo próprio fiel. */
@RestController
@RequestMapping("/api/v1")
public class AjusteCadastralController {

    private final GerenciarAjusteCadastralUseCase useCase;
    private final EventoActorContextResolver ator;
    private final UsuarioJpaRepository usuarios;
    private final GetFielUseCase getFiel;

    public AjusteCadastralController(
            GerenciarAjusteCadastralUseCase useCase,
            EventoActorContextResolver ator,
            UsuarioJpaRepository usuarios,
            GetFielUseCase getFiel) {
        this.useCase = useCase;
        this.ator = ator;
        this.usuarios = usuarios;
        this.getFiel = getFiel;
    }

    @GetMapping("/meu-perfil")
    @PreAuthorize("isAuthenticated()")
    public FielResponse meuPerfil() {
        UUID usuarioId = ator.resolveRequired().usuarioId();
        UUID fielId = usuarios.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."))
                .getFielId();
        return getFiel.execute(fielId);
    }

    @PostMapping("/meu-perfil/solicitar-ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public UUID solicitar(@RequestBody @Valid SolicitarAjusteCadastralRequest request) {
        return useCase.solicitar(ator.resolveRequired().usuarioId(), request);
    }

    @GetMapping("/ajustes-cadastrais")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')")
    public List<SolicitacaoAjusteCadastralResponse> pendentes() {
        return useCase.pendentes();
    }

    @GetMapping("/fieis/{fielId}/ajustes-cadastrais")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')")
    public List<SolicitacaoAjusteCadastralResponse> pendentesDoFiel(@PathVariable UUID fielId) {
        return useCase.pendentesDoFiel(fielId);
    }

    @PostMapping("/ajustes-cadastrais/{id}/decidir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')")
    public void decidir(@PathVariable UUID id, @RequestBody @Valid DecidirAjusteCadastralRequest request) {
        useCase.decidir(id, ator.resolveRequired().usuarioId(), request.aprovar(), request.justificativa());
    }
}
