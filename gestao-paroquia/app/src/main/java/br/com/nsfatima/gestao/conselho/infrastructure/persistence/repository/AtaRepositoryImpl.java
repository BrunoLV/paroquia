package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import br.com.nsfatima.gestao.conselho.domain.repository.FiltroAtas;
import br.com.nsfatima.gestao.conselho.domain.repository.PaginaAtas;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.AtaReuniaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper.AtaReuniaoMapper;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Adaptador da porta {@link AtaRepository} sobre Spring Data JPA. */
@Repository
public class AtaRepositoryImpl implements AtaRepository {

    /** Valor neutro para o {@code IN} quando o filtro não restringe a coleção (a cláusula é desligada por flag). */
    private static final Set<UUID> SEM_ORGANIZACAO = Set.of(new UUID(0, 0));
    private static final Set<StatusAta> SEM_STATUS = Set.of(StatusAta.RASCUNHO);
    private static final String TRAVA_SERIE =
            "SELECT CAST(pg_advisory_xact_lock(hashtextextended(:chave, 0)) AS TEXT)";

    private final AtaReuniaoJpaRepository jpa;
    private final AtaReuniaoMapper mapper;
    private final EntityManager entityManager;

    public AtaRepositoryImpl(AtaReuniaoJpaRepository jpa, AtaReuniaoMapper mapper, EntityManager entityManager) {
        this.jpa = jpa;
        this.mapper = mapper;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<AtaReuniao> buscarPorId(AtaReuniaoId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public AtaReuniao salvar(AtaReuniao ata) {
        AtaReuniaoEntity entity = jpa.findById(ata.getId().value()).orElseGet(AtaReuniaoEntity::new);
        mapper.aplicar(ata, entity);
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public PaginaAtas listar(FiltroAtas filtro, int pagina, int tamanho) {
        if (filtro.semAcesso()) {
            return PaginaAtas.vazia();
        }
        Sort ordem = Sort.by(Sort.Direction.DESC, "dataReuniao").and(Sort.by(Sort.Direction.DESC, "criadoEmUtc"));
        Page<AtaReuniaoEntity> resultado = jpa.listar(filtro.irrestrito(), organizacoes(filtro),
                filtro.organizacaoId(), filtro.tipo(), filtro.statuses().isEmpty(), statuses(filtro),
                filtro.de(), filtro.ate(), PageRequest.of(pagina, tamanho, ordem));
        return new PaginaAtas(resultado.getContent().stream().map(mapper::toDomain).toList(),
                resultado.getTotalElements());
    }

    /**
     * Serializa a numeração por organização e ano com trava consultiva da transação: quem chega depois espera
     * o commit de quem numerou antes e enxerga o último número já gravado.
     */
    @Override
    public int proximo(UUID organizacaoConvocanteId, int ano) {
        entityManager.createNativeQuery(TRAVA_SERIE)
                .setParameter("chave", "ata:" + organizacaoConvocanteId + ':' + ano)
                .getSingleResult();
        return jpa.maiorNumero(organizacaoConvocanteId, ano) + 1;
    }

    private static Collection<UUID> organizacoes(FiltroAtas filtro) {
        return filtro.organizacoesPermitidas().isEmpty() ? SEM_ORGANIZACAO : filtro.organizacoesPermitidas();
    }

    private static Collection<StatusAta> statuses(FiltroAtas filtro) {
        return filtro.statuses().isEmpty() ? SEM_STATUS : filtro.statuses();
    }
}
