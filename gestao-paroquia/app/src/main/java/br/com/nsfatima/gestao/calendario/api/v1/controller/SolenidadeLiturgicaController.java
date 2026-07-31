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

@RestController
@RequestMapping("/api/v1/solenidades-liturgicas")
public class SolenidadeLiturgicaController {

    private final SolenidadeLiturgicaService service;

    public SolenidadeLiturgicaController(SolenidadeLiturgicaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<SolenidadeLiturgicaResponseDTO>> listAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<SolenidadeLiturgicaResponseDTO>> listAtivas() {
        return ResponseEntity.ok(service.findAtivas());
    }

    @PostMapping
    public ResponseEntity<SolenidadeLiturgicaResponseDTO> create(@RequestBody SolenidadeLiturgicaRequestDTO request,
                                                                 @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        SolenidadeLiturgicaResponseDTO response = service.create(request, userRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SolenidadeLiturgicaResponseDTO> update(@PathVariable UUID id,
                                                                 @RequestBody SolenidadeLiturgicaRequestDTO request,
                                                                 @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        SolenidadeLiturgicaResponseDTO response = service.update(id, request, userRole);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        service.delete(id, userRole);
        return ResponseEntity.noContent().build();
    }
}
