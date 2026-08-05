package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
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

/**
 * Cria o vínculo de um usuário do IAM a uma organização, com o papel que ele passa a exercer ali. Garante que
 * não exista já um vínculo ativo entre o mesmo usuário e organização, e sincroniza automaticamente os campos de
 * liderança (coordenador/vice-coordenador) da organização quando o papel atribuído for de coordenação.
 */
@Service
public class CreateMembroUseCase {

    private final MembroJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final FielJpaRepository fielRepository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public CreateMembroUseCase(
            MembroJpaRepository repository,
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            FielJpaRepository fielRepository,
            MembroMapper mapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.fielRepository = fielRepository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Cria o vínculo usuário-organização, validando a existência de ambas as partes e a ausência de vínculo
     * ativo prévio entre elas antes de persistir.
     *
     * @param request dados do vínculo a criar
     * @param actor   identificador do agente executando a ação, usado para auditoria
     * @return o vínculo criado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se o usuário ou a organização não existirem, ou se já houver vínculo
     *                                   ativo entre eles
     */
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
        String nomeUsuario = fielRepository.findById(usuario.getFielId())
                .map(FielEntity::getNome)
                .orElse(null);
        syncLeadership(request.papel(), nomeUsuario, organizacao);

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
