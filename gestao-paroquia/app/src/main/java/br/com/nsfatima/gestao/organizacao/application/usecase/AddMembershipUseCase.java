package br.com.nsfatima.gestao.organizacao.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddMembershipUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public AddMembershipUseCase(
            MembroOrganizacaoJpaRepository membershipRepository,
            UsuarioJpaRepository usuarioRepository) {
        this.membershipRepository = membershipRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    /**
     * Vincula um usuário a uma organização com um papel e tipo específicos, validando a existência do usuário para garantir a consistência do vínculo.
     * 
     * Exemplo: useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORDENADOR)
     */
    public UUID execute(UUID usuarioId, UUID organizacaoId, TipoOrganizacao tipo, PapelOrganizacional papel) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException("Usuario nao encontrado: " + usuarioId);
        }

        MembroOrganizacaoEntity entity = new MembroOrganizacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsuarioId(usuarioId);
        entity.setOrganizacaoId(organizacaoId);
        entity.setTipoOrganizacao(tipo.name());
        entity.setPapel(papel.name());
        entity.setAtivo(true);

        return membershipRepository.save(entity).getId();
    }
}
