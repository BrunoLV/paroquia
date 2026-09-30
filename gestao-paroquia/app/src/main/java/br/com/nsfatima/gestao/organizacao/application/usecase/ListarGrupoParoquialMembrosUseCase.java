package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialMembroResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ListarGrupoParoquialMembrosUseCase {

    private final MembroGrupoJpaRepository vinculoRepository;
    private final FielJpaRepository fielRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;

    public ListarGrupoParoquialMembrosUseCase(
            MembroGrupoJpaRepository vinculoRepository,
            FielJpaRepository fielRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository) {
        this.vinculoRepository = vinculoRepository;
        this.fielRepository = fielRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
    }

    /**
     * Lista os membros efetivos da organização informada: vínculos formais diretos, vínculos de
     * pastorais/movimentos que usam esta organização como igreja de contexto, e, como fallback, o
     * coordenador/vice-coordenador registrados como texto livre quando ainda não houver vínculo formal com o
     * mesmo nome.
     *
     * @param grupoParoquialId identificador da organização
     * @return lista de membros efetivos da organização
     */
    @Transactional(readOnly = true)
    public List<GrupoParoquialMembroResponse> execute(UUID grupoParoquialId) {
        var vinculosBrutos =
                new ArrayList<>(vinculoRepository.findByGrupoParoquialIdAndAtivoTrue(grupoParoquialId));
        var membrosIgreja = vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(grupoParoquialId);
        for (var cm : membrosIgreja) {
            if (vinculosBrutos.stream().noneMatch(m -> m.getFielId().equals(cm.getFielId()))) {
                vinculosBrutos.add(cm);
            }
        }

        List<GrupoParoquialMembroResponse> membrosBanco = vinculosBrutos.stream()
                .map(m -> new GrupoParoquialMembroResponse(
                        m.getId(),
                        m.getFielId(),
                        resolveNome(m.getFielId()),
                        m.getPapel(),
                        m.isAtivo()))
                .toList();

        List<GrupoParoquialMembroResponse> membros = new ArrayList<>(membrosBanco);

        var orgOpt = grupoParoquialRepository.findById(grupoParoquialId);
        if (orgOpt.isPresent()) {
            var org = orgOpt.get();
            addDynamicMemberIfMissing(membros, org.getCoordenador(), "COORDENADOR");
            addDynamicMemberIfMissing(membros, org.getViceCoordenador(), "SECRETARIO_GRUPO");
        }

        return membros;
    }

    private String resolveNome(UUID fielId) {
        return fielRepository.findById(fielId)
                .map(FielEntity::getNome)
                .orElse("Fiel Desconhecido");
    }

    private void addDynamicMemberIfMissing(List<GrupoParoquialMembroResponse> list, String name, String papel) {
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
                .findFirst().ifPresent(fielId -> list.add(new GrupoParoquialMembroResponse(
                        UUID.randomUUID(), // dummy vinculo ID
                        fielId,
                        name,
                        papel,
                        true
                )));
    }
}
