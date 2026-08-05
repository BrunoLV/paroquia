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
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Atualiza um vínculo usuário-organização existente (dados e situação ativo/inativo), preservando a invariante
 * de que um usuário não pode ter mais de um vínculo ativo com a mesma organização, e ressincronizando os campos
 * de liderança da organização quando o papel atribuído for de coordenação.
 */
@Service
public class UpdateMembroUseCase {

    private final MembroJpaRepository repository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final FielJpaRepository fielRepository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public UpdateMembroUseCase(
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
     * Atualiza os dados do vínculo, revalidando existência de usuário/organização e a unicidade de vínculo
     * ativo (o próprio registro sendo editado é excluído dessa checagem).
     *
     * @param id      identificador do vínculo a atualizar
     * @param request novos dados do vínculo
     * @param actor   identificador do agente executando a ação, usado para auditoria
     * @return o vínculo atualizado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se o vínculo, usuário ou organização não existirem, ou se o
     *                                   usuário/organização já tiver outro vínculo ativo
     */
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

        // Sincronizar liderança com Organizacao, se aplicável
        if ("COORDENADOR".equalsIgnoreCase(request.papel()) || "VICE_COORDENADOR".equalsIgnoreCase(request.papel())) {
            String nomeUsuario = fielRepository.findById(usuario.getFielId())
                    .map(FielEntity::getNome)
                    .orElse(null);
            if ("COORDENADOR".equalsIgnoreCase(request.papel())) {
                organizacao.setCoordenador(nomeUsuario);
            } else {
                organizacao.setViceCoordenador(nomeUsuario);
            }
            organizacaoRepository.save(organizacao);
        }

        auditPublisher.publish(actor, "atualizar", saved.getId().toString(), "sucesso");

        return mapper.toResponse(saved);
    }

    /**
     * Ativa ou desativa o vínculo, sem alterar os demais dados cadastrais (usuário, organização, papel etc.).
     *
     * @param id     identificador do vínculo
     * @param ativo  novo estado desejado ({@code true} para ativar, {@code false} para desativar)
     * @param actor  identificador do agente executando a ação, usado para auditoria
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @Transactional
    public void toggleAtivo(UUID id, boolean ativo, String actor) {
        MembroEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado: " + id));
        entity.setAtivo(ativo);
        repository.save(entity);
        auditPublisher.publish(actor, ativo ? "ativar" : "desativar", id.toString(), "sucesso");
    }
}
