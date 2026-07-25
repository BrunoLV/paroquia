package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class MembroMapper {

    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public MembroMapper(UsuarioJpaRepository usuarioRepository, OrganizacaoJpaRepository organizacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    public MembroResponse toResponse(MembroEntity entity) {
        if (entity == null) return null;

        String usuarioNome = null;
        String usuarioEmail = null;
        String usuarioTelefone = null;

        if (entity.getUsuarioId() != null) {
            var usuarioOpt = usuarioRepository.findById(entity.getUsuarioId());
            if (usuarioOpt.isPresent()) {
                UsuarioEntity u = usuarioOpt.get();
                usuarioNome = u.getNome();
                usuarioEmail = u.getUsername();
                usuarioTelefone = u.getTelefone();
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

    public br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse toParticipacaoResponse(br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.ParticipanteOrganizacaoEntity entity) {
        if (entity == null) return null;
        return new br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse(
                entity.getId(),
                entity.getMembroId(),
                entity.getOrganizacaoId(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.isAtivo()
        );
    }
}

