package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.domain.model.MembroGrupo;
import br.com.nsfatima.gestao.membro.domain.policy.PoliticaPapelMembroComum;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroGrupoMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Atualiza um vínculo Fiel-organização existente (dados e situação ativo/inativo), preservando a invariante
 * de que um Fiel não pode ter mais de um vínculo ativo com a mesma organização, e ressincronizando o campo
 * de liderança da organização quando o papel atribuído for de coordenação.
 */
@Service
public class AtualizarMembroUseCase {

    private final MembroGrupoJpaRepository repository;
    private final FielJpaRepository fielRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final MembroMapper mapper;
    private final MembroGrupoMapper membroGrupoMapper;
    private final MembroAuditPublisher auditPublisher;

    public AtualizarMembroUseCase(
            MembroGrupoJpaRepository repository,
            FielJpaRepository fielRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            MembroMapper mapper,
            MembroGrupoMapper membroGrupoMapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.fielRepository = fielRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.mapper = mapper;
        this.membroGrupoMapper = membroGrupoMapper;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Atualiza os dados do vínculo, revalidando existência de Fiel/organização e a unicidade de vínculo
     * ativo (o próprio registro sendo editado é excluído dessa checagem). Papéis de liderança ou de conselho
     * só se mantêm quando o papel e a organização não mudam; conferi-los exige a promoção pelo Pároco.
     *
     * @param id      identificador do vínculo a atualizar
     * @param request novos dados do vínculo
     * @param actor   identificador do agente executando a ação, usado para auditoria
     * @return o vínculo atualizado, com dados de exibição resolvidos
     * @throws IllegalArgumentException se o vínculo, Fiel ou organização não existirem, o papel for
     *                                   inválido, ou se o Fiel/organização já tiver outro vínculo ativo
     */
    @Transactional
    public MembroResponse execute(UUID id, MembroRequest request, String actor) {
        MembroGrupoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado: " + id));

        fielRepository.findById(request.fielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + request.fielId()));

        OrganizacaoEntity organizacao = organizacaoRepository.findById(request.organizacaoId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.organizacaoId()));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(request.papel());
        if (papel == null) {
            throw new IllegalArgumentException("Papel inválido: " + request.papel());
        }
        PapeisPorTipoOrganizacao.exigir(organizacao.getTipo(), papel);
        boolean mantemPapelEOrganizacao = papel == PapelOrganizacional.fromStoredValue(entity.getPapel())
                && request.organizacaoId().equals(entity.getOrganizacaoId());
        boolean reativando = Boolean.TRUE.equals(request.ativo()) && !entity.isAtivo();
        if (!mantemPapelEOrganizacao || reativando) {
            PoliticaPapelMembroComum.exigirComum(papel);
        }

        repository.findByFielIdAndOrganizacaoIdAndAtivoTrue(request.fielId(), request.organizacaoId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Fiel já possui outro vínculo ativo com esta organização.");
                    }
                });

        MembroGrupo membro = membroGrupoMapper.toDomain(entity);
        membro.alterarVinculo(request.fielId(), request.organizacaoId(), organizacao.getTipo(),
                papel, request.igrejaId(), request.dataEntrada());
        if (request.ativo() != null) {
            if (request.ativo()) {
                membro.ativar();
            } else {
                membro.desativar();
            }
        }
        membroGrupoMapper.aplicar(membro, entity);

        MembroGrupoEntity saved = repository.save(entity);


        auditPublisher.publish(actor, "atualizar", saved.getId().toString(), "sucesso");

        return mapper.toResponse(saved);
    }

    /**
     * Ativa ou desativa o vínculo, sem alterar os demais dados cadastrais (fiel, organização, papel etc.).
     * Reativar um vínculo de liderança ou de conselho equivale a conferir o papel de novo e é recusado.
     *
     * @param id     identificador do vínculo
     * @param ativo  novo estado desejado ({@code true} para ativar, {@code false} para desativar)
     * @param actor  identificador do agente executando a ação, usado para auditoria
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @Transactional
    public void alternarAtivo(UUID id, boolean ativo, String actor) {
        MembroGrupoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado: " + id));
        MembroGrupo membro = membroGrupoMapper.toDomain(entity);
        if (ativo && !entity.isAtivo()) {
            PoliticaPapelMembroComum.exigirComum(PapelOrganizacional.fromStoredValue(entity.getPapel()));
        }
        if (ativo) {
            membro.ativar();
        } else {
            membro.desativar();
        }
        membroGrupoMapper.aplicar(membro, entity);
        repository.save(entity);
        auditPublisher.publish(actor, ativo ? "ativar" : "desativar", id.toString(), "sucesso");
    }
}
