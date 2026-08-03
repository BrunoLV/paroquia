package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CreateMembroUseCase {

    private final MembroJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public CreateMembroUseCase(
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
    public MembroResponse execute(MembroRequest request, String actor) {
        var usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + request.usuarioId()));

        var organizacao = organizacaoRepository.findById(request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.organizacaoId()));

        repository.findByUsuarioIdAndOrganizacaoIdAndAtivoTrue(request.usuarioId(), request.organizacaoId())
                .ifPresent(m -> {
                    throw new IllegalArgumentException("Usuário já possui vínculo ativo com esta organização.");
                });

        MembroEntity entity = new MembroEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(request.usuarioId());
        entity.setOrganizacaoId(request.organizacaoId());
        entity.setPapel(request.papel());
        entity.setDataEntrada(request.dataEntrada() != null ? request.dataEntrada() : LocalDate.now());
        entity.setAtivo(request.ativo() == null || request.ativo());
        entity.setIgrejaId(request.igrejaId());

        MembroEntity saved = repository.save(entity);

        // Synchronize leadership with Organizacao if applicable
        syncLeadership(request.papel(), usuario.getNome(), organizacao);

        auditPublisher.publish(actor, "criar", saved.getId().toString(), "sucesso");

        return mapper.toResponse(saved);
    }

    private void syncLeadership(String papel,
                                String nomeUsuario,
                                OrganizacaoEntity org) {

        if ("COORDENADOR".equalsIgnoreCase(papel)) {
            org.setCoordenador(nomeUsuario);
            organizacaoRepository.save(org);
        } else if ("VICE_COORDENADOR".equalsIgnoreCase(papel)) {
            org.setViceCoordenador(nomeUsuario);
            organizacaoRepository.save(org);
        }
    }
}
