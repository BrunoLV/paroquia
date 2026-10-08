package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.MandatoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.StatusMandato;
import br.com.nsfatima.gestao.organizacao.domain.repository.MandatoCoordenacaoRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MandatoCoordenacaoEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA de {@link MandatoCoordenacaoRepository}. Reaproveita a entidade existente ao salvar para
 * preservar versão e colunas de auditoria.
 */
@Repository
public class MandatoCoordenacaoRepositoryImpl implements MandatoCoordenacaoRepository {

    private final MandatoCoordenacaoJpaRepository jpaRepository;

    public MandatoCoordenacaoRepositoryImpl(MandatoCoordenacaoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(MandatoCoordenacao mandato) {
        MandatoCoordenacaoEntity entity = jpaRepository.findById(mandato.getId())
                .orElseGet(MandatoCoordenacaoEntity::new);
        entity.setId(mandato.getId());
        entity.setOrganizacaoId(mandato.getOrganizacaoId());
        entity.setFielId(mandato.getFielId());
        entity.setFuncao(mandato.getFuncao());
        entity.setInicio(mandato.getInicio());
        entity.setFim(mandato.getFim());
        entity.setStatus(mandato.getStatus());
        entity.setHomologadoPor(mandato.getHomologadoPor());
        entity.setMotivoVeto(mandato.getMotivoVeto());
        entity.setDecididoEmUtc(mandato.getDecididoEmUtc());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<MandatoCoordenacao> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MandatoCoordenacao> findByOrganizacaoId(UUID organizacaoId) {
        return jpaRepository.findByOrganizacaoIdOrderByInicioDesc(organizacaoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MandatoCoordenacao> findHomologadosVencidosAntesDe(LocalDate data) {
        return jpaRepository.findByStatusAndFimBefore(StatusMandato.HOMOLOGADO, data).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existeVigente(UUID organizacaoId, LocalDate data) {
        return jpaRepository.existsByOrganizacaoIdAndStatusAndInicioLessThanEqualAndFimGreaterThanEqual(
                organizacaoId, StatusMandato.HOMOLOGADO, data, data);
    }

    private MandatoCoordenacao toDomain(MandatoCoordenacaoEntity e) {
        return new MandatoCoordenacao(e.getId(), e.getOrganizacaoId(), e.getFielId(), e.getFuncao(),
                e.getInicio(), e.getFim(), e.getStatus(), e.getHomologadoPor(), e.getMotivoVeto(),
                e.getDecididoEmUtc());
    }
}
