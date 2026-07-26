package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateMembroUseCase {

    private final MembroJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public UpdateMembroUseCase(
            MembroJpaRepository repository,
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            MembroMapper mapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public MembroResponse execute(UUID id, MembroRequest request, String actor) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado: " + id));

        var usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + request.usuarioId()));

        var organizacao = organizacaoRepository.findById(request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.organizacaoId()));

        repository.findByUsuarioIdAndOrganizacaoIdAndAtivoTrue(request.usuarioId(), request.organizacaoId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Usuário já possui outro vínculo ativo com esta organização.");
                    }
                });

        entity.setUsuarioId(request.usuarioId());
        entity.setOrganizacaoId(request.organizacaoId());
        entity.setPapel(request.papel());
        if (request.dataEntrada() != null) {
            entity.setDataEntrada(request.dataEntrada());
        }
        if (request.ativo() != null) {
            entity.setAtivo(request.ativo());
        }
        entity.setIgrejaId(request.igrejaId());

        MembroEntity saved = repository.save(entity);

        // Synchronize leadership with Organizacao if applicable
        if ("COORDENADOR".equalsIgnoreCase(request.papel())) {
            organizacao.setCoordenador(usuario.getNome());
            organizacaoRepository.save(organizacao);
        } else if ("VICE_COORDENADOR".equalsIgnoreCase(request.papel())) {
            organizacao.setViceCoordenador(usuario.getNome());
            organizacaoRepository.save(organizacao);
        }

        auditPublisher.publish(actor, "update", saved.getId().toString(), "success");

        return mapper.toResponse(saved);
    }

    @Transactional
    public void toggleAtivo(UUID id, boolean ativo, String actor) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado: " + id));
        entity.setAtivo(ativo);
        repository.save(entity);
        auditPublisher.publish(actor, ativo ? "activate" : "deactivate", id.toString(), "success");
    }
}
