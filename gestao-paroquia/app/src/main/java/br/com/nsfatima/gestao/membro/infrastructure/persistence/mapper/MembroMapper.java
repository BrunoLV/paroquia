package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.ParticipanteOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Component;

/**
 * Converte entidades do contexto {@code membro} em DTOs de resposta, enriquecendo o vínculo usuário-organização
 * com dados de exibição (nome/e-mail/telefone do usuário, nome da organização) obtidos por consulta direta aos
 * contextos {@code iam}, {@code fiel} e {@code organizacao} — necessário porque {@code MembroEntity} guarda
 * apenas os IDs, sem duplicar dados de outros agregados. Nome/telefone vêm do {@code Fiel} vinculado ao
 * {@code Usuario} (o {@code Usuario} não carrega mais esses dados diretamente).
 */
@Component
public class MembroMapper {

    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final FielJpaRepository fielRepository;

    public MembroMapper(
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            FielJpaRepository fielRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.fielRepository = fielRepository;
    }

    /**
     * Converte o vínculo usuário-organização em DTO de resposta, resolvendo os dados de exibição do usuário e da
     * organização vinculados. IDs de usuário/organização que não existem mais (registro órfão) resultam em campos
     * de nome/e-mail/telefone {@code null}, sem lançar exceção.
     *
     * @param entity vínculo persistido, ou {@code null}
     * @return DTO de resposta enriquecido, ou {@code null} se {@code entity} for {@code null}
     */
    public MembroResponse toResponse(MembroEntity entity) {
        if (entity == null) {
            return null;
        }

        String usuarioNome = null;
        String usuarioEmail = null;
        String usuarioTelefone = null;

        if (entity.getUsuarioId() != null) {
            var usuarioOpt = usuarioRepository.findById(entity.getUsuarioId());
            if (usuarioOpt.isPresent()) {
                UsuarioEntity u = usuarioOpt.get();
                usuarioEmail = u.getUsername();
                FielEntity fiel = fielRepository.findById(u.getFielId()).orElse(null);
                if (fiel != null) {
                    usuarioNome = fiel.getNome();
                    usuarioTelefone = fiel.getTelefone();
                }
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
                entity.getUsuarioId(),
                usuarioNome,
                usuarioEmail,
                usuarioTelefone,
                entity.getOrganizacaoId(),
                organizacaoNome,
                entity.getPapel(),
                entity.getDataEntrada(),
                entity.isAtivo(),
                entity.getIgrejaId()
        );
    }

    /**
     * Converte um registro de participação em DTO de resposta.
     *
     * @param entity participação persistida, ou {@code null}
     * @return DTO de resposta, ou {@code null} se {@code entity} for {@code null}
     */
    public ParticipacaoResponse toParticipacaoResponse(ParticipanteOrganizacaoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ParticipacaoResponse(
                entity.getId(),
                entity.getMembroId(),
                entity.getOrganizacaoId(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.isAtivo()
        );
    }
}

