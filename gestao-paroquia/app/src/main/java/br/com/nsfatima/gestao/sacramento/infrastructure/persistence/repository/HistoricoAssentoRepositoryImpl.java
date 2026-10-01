package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.sacramento.domain.model.HistoricoEntrada;
import br.com.nsfatima.gestao.sacramento.domain.repository.HistoricoAssentoRepository;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoHistoricoEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** Adaptador append-only da trilha de histórico: só inclui e consulta. */
@Repository
public class HistoricoAssentoRepositoryImpl implements HistoricoAssentoRepository {

    private final AssentoHistoricoJpaRepository jpa;

    public HistoricoAssentoRepositoryImpl(AssentoHistoricoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void registrar(HistoricoEntrada e) {
        jpa.save(new AssentoHistoricoEntity(e.id(), e.assentoId(), e.acao(), e.usuarioId(), e.ator(), e.perfil(),
                e.enderecoIp(), e.valoresAnteriores(), e.valoresPosteriores(), e.ocorridoEmUtc()));
    }

    @Override
    public List<HistoricoEntrada> listarPorAssento(UUID assentoId) {
        return jpa.findByAssentoIdOrderByOcorridoEmUtcAsc(assentoId).stream()
                .map(h -> new HistoricoEntrada(h.getId(), h.getAssentoId(), h.getAcao(), h.getUsuarioId(), h.getAtor(),
                        h.getPerfil(), h.getEnderecoIp(), h.getValoresAnteriores(), h.getValoresPosteriores(),
                        h.getOcorridoEmUtc()))
                .toList();
    }
}
