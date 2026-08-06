package br.com.nsfatima.gestao.membro.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroRequest;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.observability.MembroAuditPublisher;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper.MembroMapper;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Atualiza um vínculo Fiel-organização existente (dados e situação ativo/inativo), preservando a invariante
 * de que um Fiel não pode ter mais de um vínculo ativo com a mesma organização, e ressincronizando o campo
 * de liderança da organização quando o papel atribuído for de coordenação.
 */
@Service
public class UpdateMembroUseCase {

    private final MembroGrupoJpaRepository repository;
    private final FielJpaRepository fielRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final MembroMapper mapper;
    private final MembroAuditPublisher auditPublisher;

    public UpdateMembroUseCase(
            MembroGrupoJpaRepository repository,
            FielJpaRepository fielRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository,
            MembroMapper mapper,
            MembroAuditPublisher auditPublisher) {
        this.repository = repository;
        this.fielRepository = fielRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Atualiza os dados do vínculo, revalidando existência de Fiel/organização e a unicidade de vínculo
     * ativo (o próprio registro sendo editado é excluído dessa checagem).
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

        FielEntity fiel = fielRepository.findById(request.fielId())
                .orElseThrow(() -> new IllegalArgumentException("Fiel não encontrado: " + request.fielId()));

        GrupoParoquialEntity grupoParoquial = grupoParoquialRepository.findById(request.grupoParoquialId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Organização não encontrada: " + request.grupoParoquialId()));

        PapelOrganizacional papel = PapelOrganizacional.fromStoredValue(request.papel());
        if (papel == null) {
            throw new IllegalArgumentException("Papel inválido: " + request.papel());
        }
        PapelGlobalValidation.rejectIfGlobal(papel);

        repository.findByFielIdAndGrupoParoquialIdAndAtivoTrue(request.fielId(), request.grupoParoquialId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Fiel já possui outro vínculo ativo com esta organização.");
                    }
                });

        entity.setFielId(request.fielId());
        entity.setGrupoParoquialId(request.grupoParoquialId());
        entity.setTipoGrupo(grupoParoquial.getTipo());
        entity.setPapel(papel.storedValue());
        if (request.dataEntrada() != null) {
            entity.setDataEntrada(request.dataEntrada());
        }
        if (request.ativo() != null) {
            entity.setAtivo(request.ativo());
        }
        entity.setIgrejaVinculoId(request.igrejaId());

        MembroGrupoEntity saved = repository.save(entity);

        LiderancaSyncSupport.sync(grupoParoquialRepository, papel, fiel.getNome(), grupoParoquial);

        auditPublisher.publish(actor, "atualizar", saved.getId().toString(), "sucesso");

        return mapper.toResponse(saved);
    }

    /**
     * Ativa ou desativa o vínculo, sem alterar os demais dados cadastrais (fiel, organização, papel etc.).
     *
     * @param id     identificador do vínculo
     * @param ativo  novo estado desejado ({@code true} para ativar, {@code false} para desativar)
     * @param actor  identificador do agente executando a ação, usado para auditoria
     * @throws IllegalArgumentException se não houver vínculo com o {@code id} informado
     */
    @Transactional
    public void toggleAtivo(UUID id, boolean ativo, String actor) {
        MembroGrupoEntity entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Membro não encontrado: " + id));
        entity.setAtivo(ativo);
        repository.save(entity);
        auditPublisher.publish(actor, ativo ? "ativar" : "desativar", id.toString(), "sucesso");
    }
}
