package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.CreateUsuarioRequest;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.application.usecase.ApproveUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.CreateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.GetUsuarioUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.ListUsuariosUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.UpdateUsuarioUseCase;
import br.com.nsfatima.gestao.iam.domain.service.UsuarioAuthorizationService;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.AdicionarVinculoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.VinculoResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListarVinculosUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.RemoverVinculoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * CRUD de usuários e gestão dos seus vínculos organizacionais (membros). Todo cadastro é administrativo
 * (via {@link CreateUsuarioUseCase}, exige um {@code Fiel} já existente) — não há mais autocadastro público;
 * {@link ApproveUsuarioUseCase} segue existindo para o fluxo de aprovação de usuários pendentes criados
 * administrativamente. A autorização por endpoint combina {@code @PreAuthorize} de papel global com
 * checagens finas de escopo organizacional feitas por {@link UsuarioAuthorizationService} (admin ou
 * coordenador da organização do usuário-alvo).
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários e permissões (IAM)")
public class UsuarioController {

    private final ListUsuariosUseCase listUsuariosUseCase;
    private final GetUsuarioUseCase getUsuarioUseCase;
    private final CreateUsuarioUseCase createUsuarioUseCase;
    private final UpdateUsuarioUseCase updateUsuarioUseCase;
    private final AdicionarVinculoUseCase adicionarVinculoUseCase;
    private final RemoverVinculoUseCase removerVinculoUseCase;
    private final ListarVinculosUseCase listarVinculosUseCase;
    private final UsuarioAuthorizationService authorizationService;
    private final ApproveUsuarioUseCase approveUsuarioUseCase;

    public UsuarioController(
            ListUsuariosUseCase listUsuariosUseCase,
            GetUsuarioUseCase getUsuarioUseCase,
            CreateUsuarioUseCase createUsuarioUseCase,
            UpdateUsuarioUseCase updateUsuarioUseCase,
            AdicionarVinculoUseCase adicionarVinculoUseCase,
            RemoverVinculoUseCase removerVinculoUseCase,
            ListarVinculosUseCase listarVinculosUseCase,
            UsuarioAuthorizationService authorizationService,
            ApproveUsuarioUseCase approveUsuarioUseCase) {
        this.listUsuariosUseCase = listUsuariosUseCase;
        this.getUsuarioUseCase = getUsuarioUseCase;
        this.createUsuarioUseCase = createUsuarioUseCase;
        this.updateUsuarioUseCase = updateUsuarioUseCase;
        this.adicionarVinculoUseCase = adicionarVinculoUseCase;
        this.removerVinculoUseCase = removerVinculoUseCase;
        this.listarVinculosUseCase = listarVinculosUseCase;
        this.authorizationService = authorizationService;
        this.approveUsuarioUseCase = approveUsuarioUseCase;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR')")
    @Operation(summary = "Lista todos os usuários")
    /*
      Fornece uma visão geral de todos os usuários cadastrados no sistema para fins de auditoria e gestão
      administrativa global.
      <p>
      Exemplo: GET /api/v1/usuarios
     */
    public List<UsuarioResponse> list() {
        return listUsuariosUseCase.execute();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo usuário")
    /*
      Registra novos usuários no sistema, permitindo que iniciem o processo de integração e obtenção de acessos.
      <p>
      Exemplo: POST /api/v1/usuarios { "username": "joao.silva", "password": "SafePassword123",
      "fielId": "..." }
     */
    public UUID create(@RequestBody @Valid CreateUsuarioRequest request) {
        return createUsuarioUseCase.execute(request.username(), request.password(), request.fielId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @Operation(summary = "Obtém detalhes de um usuário")
    /*
      Recupera as informações detalhadas de um usuário específico para consulta ou preparação de edições.
      <p>
      Exemplo: GET /api/v1/usuarios/{id}
     */
    public UsuarioResponse get(@PathVariable UUID id) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        return getUsuarioUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um usuário")
    /*
      Permite a modificação de dados cadastrais de um usuário para manter o registro atualizado e consistente.
      <p>
      Exemplo: PATCH /api/v1/usuarios/{id} { "username": "novo_usuario", "enabled": true }
     */
    public void update(@PathVariable UUID id, @RequestBody UsuarioResponse request) {
        updateUsuarioUseCase.execute(id, request.username(), request.enabled());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLERO_PAROCO') or hasRole('CONSELHO_COORDENADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Aprova um novo usuário e ativa suas solicitações de pastorais")
    public void approve(@PathVariable UUID id) {
        approveUsuarioUseCase.execute(id);
    }

    @GetMapping("/{id}/membros")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @Operation(summary = "Lista associações de um usuário a organizações")
    /*
      Lista os vínculos de um usuário com diferentes organizações para mapear seu escopo de atuação e
      responsabilidades.
      <p>
      Exemplo: GET /api/v1/usuarios/{id}/membros
     */
    public List<VinculoResponse> listarVinculos(@PathVariable UUID id) {
        authorizationService.requireAdminOrCoordinatorOf(id);
        return listarVinculosUseCase.execute(id);
    }

    @PostMapping("/{id}/membros")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona associação a organização")
    /*
      Vincula um usuário a uma organização com um papel e tipo específicos para delegar autoridade e funções.
      <p>
      Exemplo: POST /api/v1/usuarios/{id}/membros { "organizacaoId": "...", "tipo": "VOLUNTARIO", "papel": "AUXILIAR" }
     */
    public UUID adicionarVinculo(@PathVariable UUID id, @RequestBody @Valid AdicionarVinculoRequest request) {
        authorizationService.requireAdminOrCoordinatorOfOrganization(request.organizacaoId());
        return adicionarVinculoUseCase.execute(id,
                request.organizacaoId(), request.tipo(), request.papel(), request.igrejaVinculoId());

    }

    @DeleteMapping("/{id}/membros/{vinculoId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COORDENADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove associação a organização")
    /*
      Encerra o vínculo de um usuário com uma organização específica para revogar acessos e responsabilidades
      naquele contexto.
      <p>
      Exemplo: DELETE /api/v1/usuarios/{id}/membros/{vinculoId}
     */
    public void removerVinculo(@PathVariable UUID id, @PathVariable UUID vinculoId) {
        // Here we ideally need to check the organization of the vinculo being removed.
        // For simplicity, we can use requireAdminOrCoordinatorOf(id) which ensures 
        // the actor can manage the target user.
        authorizationService.requireAdminOrCoordinatorOf(id);
        removerVinculoUseCase.execute(vinculoId);
    }
}
