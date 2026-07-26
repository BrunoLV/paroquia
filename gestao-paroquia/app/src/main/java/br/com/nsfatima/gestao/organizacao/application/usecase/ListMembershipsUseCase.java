package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.MembershipResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListMembershipsUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListMembershipsUseCase(MembroOrganizacaoJpaRepository membershipRepository,
                                  OrganizacaoJpaRepository organizacaoRepository) {

        this.membershipRepository = membershipRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional(readOnly = true)
    /*
      Recupera todos os vínculos ativos de um usuário com organizações para determinar seu escopo de acesso e nível
      de atuação na paróquia.
      <p>
      Exemplo: useCase.execute(usuarioId)
     */
    public List<MembershipResponse> execute(UUID usuarioId) {
        return membershipRepository.findByUsuarioIdAndAtivoTrue(usuarioId).stream()
                .map(m -> {
                    String orgNome = organizacaoRepository.findById(m.getOrganizacaoId())
                            .map(OrganizacaoEntity::getNome)
                            .orElse(null);
                    String igrejaNome = m.getIgrejaVinculoId() != null
                            ? organizacaoRepository.findById(m.getIgrejaVinculoId())
                            .map(OrganizacaoEntity::getNome).orElse(null)
                            : null;
                    return new MembershipResponse(
                            m.getId(),
                            m.getOrganizacaoId(),
                            orgNome,
                            m.getTipoOrganizacao(),
                            m.getPapel(),
                            m.isAtivo(),
                            m.getIgrejaVinculoId(),
                            igrejaNome);
                })
                .toList();
    }
}
