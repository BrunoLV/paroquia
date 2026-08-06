package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Component;

/**
 * Converte {@link MembroGrupoEntity} em DTO de resposta, enriquecendo o vínculo com dados de exibição
 * (nome/e-mail de contato/telefone do {@code Fiel}, nome da organização) obtidos por consulta direta aos
 * contextos {@code fiel} e {@code organizacao} — {@code MembroGrupoEntity} guarda apenas os IDs.
 */
@Component
public class MembroMapper {

    private final OrganizacaoJpaRepository organizacaoRepository;
    private final FielJpaRepository fielRepository;

    public MembroMapper(OrganizacaoJpaRepository organizacaoRepository, FielJpaRepository fielRepository) {
        this.organizacaoRepository = organizacaoRepository;
        this.fielRepository = fielRepository;
    }

    /**
     * Converte o vínculo Fiel-organização em DTO de resposta, resolvendo os dados de exibição do Fiel e da
     * organização vinculados. IDs que não existem mais (registro órfão) resultam em campos de nome/e-mail/
     * telefone {@code null}, sem lançar exceção.
     *
     * @param entity vínculo persistido, ou {@code null}
     * @return DTO de resposta enriquecido, ou {@code null} se {@code entity} for {@code null}
     */
    public MembroResponse toResponse(MembroGrupoEntity entity) {
        if (entity == null) {
            return null;
        }

        String fielNome = null;
        String fielEmail = null;
        String fielTelefone = null;

        if (entity.getFielId() != null) {
            FielEntity fiel = fielRepository.findById(entity.getFielId()).orElse(null);
            if (fiel != null) {
                fielNome = fiel.getNome();
                fielEmail = fiel.getEmail();
                fielTelefone = fiel.getTelefone();
            }
        }

        String organizacaoNome = null;
        if (entity.getOrganizacaoId() != null) {
            organizacaoNome = organizacaoRepository.findById(entity.getOrganizacaoId())
                    .map(OrganizacaoEntity::getNome)
                    .orElse(null);
        }

        return new MembroResponse(
                entity.getId(),
                entity.getFielId(),
                fielNome,
                fielEmail,
                fielTelefone,
                entity.getOrganizacaoId(),
                organizacaoNome,
                entity.getPapel(),
                entity.getDataEntrada(),
                entity.isAtivo(),
                entity.getIgrejaVinculoId()
        );
    }
}
