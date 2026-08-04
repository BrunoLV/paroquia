package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaRequestDTO;
import br.com.nsfatima.gestao.calendario.api.v1.dto.SolenidadeLiturgicaResponseDTO;
import br.com.nsfatima.gestao.calendario.domain.service.SolenidadeLiturgicaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Gerencia o cadastro das solenidades litúrgicas (datas fixas ou móveis do calendário eclesiástico,
 * como Natal, Páscoa e festas de guarda) que servem de base para o provisionamento anual de eventos
 * da Matriz e das Comunidades. A criação, atualização e remoção são restritas por papel do usuário,
 * verificado a partir do cabeçalho {@code X-User-Role}.
 */
@RestController
@RequestMapping("/api/v1/solenidades-liturgicas")
public class SolenidadeLiturgicaController {

    private final SolenidadeLiturgicaService service;

    public SolenidadeLiturgicaController(SolenidadeLiturgicaService service) {
        this.service = service;
    }

    /**
     * Lista todas as solenidades litúrgicas cadastradas, incluindo as inativas, para telas de
     * administração e manutenção do calendário eclesiástico.
     *
     * @return todas as solenidades litúrgicas cadastradas
     */
    @GetMapping
    public ResponseEntity<List<SolenidadeLiturgicaResponseDTO>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Lista apenas as solenidades litúrgicas atualmente ativas, usadas pelo provisionamento anual
     * de eventos e por telas de consulta do calendário corrente.
     *
     * @return as solenidades litúrgicas com status ativo
     */
    @GetMapping("/ativas")
    public ResponseEntity<List<SolenidadeLiturgicaResponseDTO>> listAtivas() {
        return ResponseEntity.ok(service.findAtivas());
    }

    /**
     * Cadastra uma nova solenidade litúrgica, sujeita à autorização por papel do usuário informado
     * no cabeçalho {@code X-User-Role}.
     *
     * @param request dados da solenidade a ser criada
     * @param userRole papel do usuário requisitante, usado para autorizar a operação
     * @return a solenidade litúrgica criada, com status HTTP 201
     */
    @PostMapping
    public ResponseEntity<SolenidadeLiturgicaResponseDTO> create(@RequestBody SolenidadeLiturgicaRequestDTO request,
                                                                 @RequestHeader(value = "X-User-Role", required = false)
            String userRole) {
        SolenidadeLiturgicaResponseDTO response = service.create(request, userRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Atualiza os dados de uma solenidade litúrgica existente, sujeita à autorização por papel do
     * usuário informado no cabeçalho {@code X-User-Role}.
     *
     * @param id identificador da solenidade a ser atualizada
     * @param request novos dados da solenidade
     * @param userRole papel do usuário requisitante, usado para autorizar a operação
     * @return a solenidade litúrgica atualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<SolenidadeLiturgicaResponseDTO> update(@PathVariable UUID id,
                                                                 @RequestBody SolenidadeLiturgicaRequestDTO request,
                                                                 @RequestHeader(value = "X-User-Role", required = false)
            String userRole) {
        SolenidadeLiturgicaResponseDTO response = service.update(id, request, userRole);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove uma solenidade litúrgica do cadastro, sujeita à autorização por papel do usuário
     * informado no cabeçalho {@code X-User-Role}. Solenidades removidas deixam de ser consideradas
     * no provisionamento anual de eventos.
     *
     * @param id identificador da solenidade a ser removida
     * @param userRole papel do usuário requisitante, usado para autorizar a operação
     * @return {@code 204 No Content} após a remoção bem-sucedida
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestHeader(value = "X-User-Role", required = false)
            String userRole) {
        service.delete(id, userRole);
        return ResponseEntity.noContent().build();
    }
}
