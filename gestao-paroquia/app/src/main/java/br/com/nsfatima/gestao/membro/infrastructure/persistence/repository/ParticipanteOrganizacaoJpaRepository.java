package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.ParticipanteOrganizacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Acesso a persistência das participações adicionais de um membro em organizações
 * ({@code ParticipanteOrganizacaoEntity}), usado para listar o histórico/engajamento ativo de uma pessoa e para
 * checar duplicidade antes de uma nova inscrição.
 */
@Repository
public interface ParticipanteOrganizacaoJpaRepository extends JpaRepository<ParticipanteOrganizacaoEntity, UUID> {
    /** Participações ativas (sem data de fim) de um membro, usado para montar seu engajamento atual. */
    List<ParticipanteOrganizacaoEntity> findByMembroIdAndAtivoTrue(UUID membroId);

    List<ParticipanteOrganizacaoEntity> findByOrganizacaoIdAndAtivoTrue(UUID organizacaoId);
}
