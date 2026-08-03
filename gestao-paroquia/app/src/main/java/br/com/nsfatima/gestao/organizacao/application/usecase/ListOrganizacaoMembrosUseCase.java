package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista os membros efetivos de uma organização, combinando os vínculos formais ({@code MembroOrganizacaoEntity})
 * diretos da organização com os vínculos de pastorais/movimentos que a têm como igreja de contexto, e
 * completando a lista com o coordenador/vice-coordenador definidos como texto livre na organização quando
 * ainda não existir um vínculo formal correspondente — garantindo que o organograma sempre exiba a liderança
 * mesmo antes de ela ser formalmente vinculada.
 */
@Service
public class ListOrganizacaoMembrosUseCase {

    private final MembroOrganizacaoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListOrganizacaoMembrosUseCase(
            MembroOrganizacaoJpaRepository vinculoRepository,
            UsuarioJpaRepository usuarioRepository,
            MembroJpaRepository membroRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    /**
     * Lista os membros efetivos da organização informada: vínculos formais diretos, vínculos de
     * pastorais/movimentos que usam esta organização como igreja de contexto, e, como fallback, o
     * coordenador/vice-coordenador registrados como texto livre quando ainda não houver vínculo formal com o
     * mesmo nome.
     *
     * @param organizacaoId identificador da organização
     * @return lista de membros efetivos da organização
     */
    @Transactional(readOnly = true)
    public List<OrganizacaoMembroResponse> execute(UUID organizacaoId) {
        var vinculosBrutos =
                new java.util.ArrayList<>(vinculoRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId));
        var membrosIgreja = vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(organizacaoId);
        for (var cm : membrosIgreja) {
            if (vinculosBrutos.stream().noneMatch(m -> m.getUsuarioId().equals(cm.getUsuarioId()))) {
                vinculosBrutos.add(cm);
            }
        }

        List<OrganizacaoMembroResponse> membrosBanco = vinculosBrutos.stream()
                .map(m -> {
                    String nomeUsuario = usuarioRepository.findById(m.getUsuarioId())
                            .map(UsuarioEntity::getNome)
                            .orElse("Usuário Desconhecido");
                    return new OrganizacaoMembroResponse(
                            m.getId(),
                            m.getUsuarioId(),
                            nomeUsuario,
                            m.getPapel(),
                            m.isAtivo()
                    );
                })
                .toList();

        List<OrganizacaoMembroResponse> membros = new java.util.ArrayList<>(membrosBanco);

        var orgOpt = organizacaoRepository.findById(organizacaoId);
        if (orgOpt.isPresent()) {
            var org = orgOpt.get();
            addDynamicMemberIfMissing(membros, org.getCoordenador(), "COORDENADOR");
            addDynamicMemberIfMissing(membros, org.getViceCoordenador(), "VICE_COORDENADOR");
        }

        return membros;
    }

    private void addDynamicMemberIfMissing(List<OrganizacaoMembroResponse> list, String name, String papel) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        boolean exists = list.stream().anyMatch(m -> name.equalsIgnoreCase(m.usuarioNome()));
        if (exists) {
            return;
        }

        usuarioRepository.findAll().stream()
                .filter(u -> name.equalsIgnoreCase(u.getNome()))
                .map(UsuarioEntity::getId)
                .findFirst().ifPresent(userId -> list.add(new OrganizacaoMembroResponse(
                        UUID.randomUUID(), // dummy vinculo ID
                        userId,
                        name,
                        papel,
                        true
                )));

    }

}
