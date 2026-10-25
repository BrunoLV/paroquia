package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.RegistroAuditoriaAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaHistoricoRepository;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.AtaHistoricoEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Adaptador append-only da trilha de auditoria das atas: só inclui e consulta. */
@Repository
public class AtaHistoricoRepositoryImpl implements AtaHistoricoRepository {

    private final AtaHistoricoJpaRepository jpa;

    public AtaHistoricoRepositoryImpl(AtaHistoricoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(RegistroAuditoriaAta r) {
        AtaHistoricoEntity e = new AtaHistoricoEntity();
        e.setId(r.id());
        e.setAtaId(r.ataId().value());
        e.setAcao(r.acao());
        e.setUsuarioId(r.usuarioId());
        e.setAtor(r.ator());
        e.setPerfil(r.perfil());
        e.setEnderecoIp(r.enderecoIp());
        e.setValoresAnteriores(r.valoresAnteriores());
        e.setValoresPosteriores(r.valoresPosteriores());
        e.setOcorridoEmUtc(r.ocorridoEmUtc());
        jpa.save(e);
    }

    @Override
    public List<RegistroAuditoriaAta> listarPorAta(AtaReuniaoId ataId) {
        return jpa.findByAtaIdOrderByOcorridoEmUtcAsc(ataId.value()).stream()
                .map(h -> new RegistroAuditoriaAta(h.getId(), new AtaReuniaoId(h.getAtaId()), h.getAcao(),
                        h.getUsuarioId(), h.getAtor(), h.getPerfil(), h.getEnderecoIp(), h.getValoresAnteriores(),
                        h.getValoresPosteriores(), h.getOcorridoEmUtc()))
                .toList();
    }
}
