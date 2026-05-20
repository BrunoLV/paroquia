package br.com.nsfatima.gestao.organizacao.application.usecase;

import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.MembershipResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListMembershipsUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;

    public ListMembershipsUseCase(MembroOrganizacaoJpaRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Transactional(readOnly = true)
    /**
     * Recupera todos os vínculos ativos de um usuário com organizações para determinar seu escopo de acesso e nível de atuação na paróquia.
     * 
     * Exemplo: useCase.execute(usuarioId)
     */
    public List<MembershipResponse> execute(UUID usuarioId) {
        return membershipRepository.findByUsuarioIdAndAtivoTrue(usuarioId).stream()
                .map(m -> new MembershipResponse(
                        m.getId(),
                        m.getOrganizacaoId(),
                        m.getTipoOrganizacao(),
                        m.getPapel(),
                        m.isAtivo()))
                .toList();
    }
}
