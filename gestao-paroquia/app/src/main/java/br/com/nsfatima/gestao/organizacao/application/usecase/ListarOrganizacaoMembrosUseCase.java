package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.domain.model.IdoneidadeAgente;
import br.com.nsfatima.gestao.membro.domain.policy.PoliticaIdoneidadeFuncao;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lista os membros efetivos de uma organização, combinando os vínculos formais ({@code MembroGrupoEntity})
 * diretos da organização com os vínculos de pastorais/movimentos que a têm como igreja de contexto, e
 * completando a lista com o coordenador/vice-coordenador definidos como texto livre na organização quando
 * ainda não existir um vínculo formal correspondente — garantindo que o organograma sempre exiba a liderança
 * mesmo antes de ela ser formalmente vinculada.
 */
@Service
public class ListarOrganizacaoMembrosUseCase {

    private final MembroGrupoJpaRepository vinculoRepository;
    private final FielJpaRepository fielRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListarOrganizacaoMembrosUseCase(
            MembroGrupoJpaRepository vinculoRepository,
            FielJpaRepository fielRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.vinculoRepository = vinculoRepository;
        this.fielRepository = fielRepository;
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
                new ArrayList<>(vinculoRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId));
        var membrosIgreja = vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(organizacaoId);
        for (var cm : membrosIgreja) {
            if (vinculosBrutos.stream().noneMatch(m -> m.getFielId().equals(cm.getFielId()))) {
                vinculosBrutos.add(cm);
            }
        }

        List<OrganizacaoMembroResponse> membrosBanco = vinculosBrutos.stream()
                .map(m -> toResponse(m, setorDa(m.getOrganizacaoId())))
                .toList();

        List<OrganizacaoMembroResponse> membros = new ArrayList<>(membrosBanco);

        var orgOpt = organizacaoRepository.findById(organizacaoId);
        if (orgOpt.isPresent()) {
            var org = orgOpt.get();
            addDynamicMemberIfMissing(membros, org.getCoordenador(), "COORDENADOR");
            addDynamicMemberIfMissing(membros, org.getViceCoordenador(), "SECRETARIO_GRUPO");
        }

        return membros;
    }

    private OrganizacaoMembroResponse toResponse(MembroGrupoEntity m, SetorPastoral setor) {
        var papel = PapelOrganizacional.fromStoredValue(m.getPapel());
        var idoneidade = new IdoneidadeAgente(m.getFormacaoConcluidaEm(), m.getVivenciaSacramentalAtestadaEm(),
                m.getTermoProtecaoMenoresEm(), m.getTermoProtecaoMenoresValidoAte(), m.getEnviadoEm(),
                m.getEnviadoPor());
        var pendencias = PoliticaIdoneidadeFuncao.pendencias(
                papel != null && papel.isCoordenacao(), setor, idoneidade, LocalDate.now());
        return new OrganizacaoMembroResponse(m.getId(), m.getFielId(), resolveNome(m.getFielId()), m.getPapel(),
                m.isAtivo(), pendencias.isEmpty(), pendencias);
    }

    private SetorPastoral setorDa(UUID organizacaoId) {
        return organizacaoRepository.findById(organizacaoId)
                .map(OrganizacaoEntity::getSetorPastoral).orElse(null);
    }

    private String resolveNome(UUID fielId) {
        return fielRepository.findById(fielId)
                .map(FielEntity::getNome)
                .orElse("Fiel Desconhecido");
    }

    private void addDynamicMemberIfMissing(List<OrganizacaoMembroResponse> list, String name, String papel) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        boolean exists = list.stream().anyMatch(m -> name.equalsIgnoreCase(m.fielNome()));
        if (exists) {
            return;
        }

        fielRepository.findAll().stream()
                .filter(f -> name.equalsIgnoreCase(f.getNome()))
                .map(FielEntity::getId)
                .findFirst().ifPresent(fielId -> list.add(new OrganizacaoMembroResponse(
                        UUID.randomUUID(), // dummy vinculo ID
                        fielId,
                        name,
                        papel,
                        true,
                        null,
                        null
                )));
    }
}
