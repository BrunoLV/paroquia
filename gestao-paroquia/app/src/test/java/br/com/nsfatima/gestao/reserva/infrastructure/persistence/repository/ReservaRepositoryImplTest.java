package br.com.nsfatima.gestao.reserva.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.mapper.ReservaMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaRepositoryImplTest {

    @Mock private ReservaJpaRepository jpaRepository;
    @Mock private ReservaMapper mapper;

    private ReservaRepositoryImpl repository() {
        return new ReservaRepositoryImpl(jpaRepository, mapper);
    }

    private Reserva reservaDominio() {
        Instant inicio = Instant.now().plus(1, ChronoUnit.HOURS);
        IntervaloReserva intervalo = new IntervaloReserva(inicio, inicio.plus(1, ChronoUnit.HOURS), 15);
        return Reserva.criar(ReservaId.generate(), TipoItemReserva.LOCAL, UUID.randomUUID(), intervalo, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, StatusReserva.PENDENTE, Instant.now());
    }

    private ReservaEntity entity(UUID id) {
        Instant now = Instant.now();
        return new ReservaEntity(id, "LOCAL", UUID.randomUUID(), now, now.plusSeconds(3600), 15, "desc",
                UUID.randomUUID(), UUID.randomUUID(), null, null, "PENDENTE", null, null, null, null, null,
                now, now, null, 0L);
    }

    @Test
    @DisplayName("save mapeia domínio para entidade, persiste e retorna domínio remapeado")
    void deveSalvarReserva() {
        Reserva domain = reservaDominio();
        ReservaEntity entityMapeada = entity(domain.getId().value());
        ReservaEntity entitySalva = entity(domain.getId().value());
        Reserva domainRetornado = reservaDominio();

        when(mapper.toEntity(domain)).thenReturn(entityMapeada);
        when(jpaRepository.save(entityMapeada)).thenReturn(entitySalva);
        when(mapper.toDomain(entitySalva)).thenReturn(domainRetornado);

        Reserva resultado = repository().save(domain);

        assertThat(resultado).isSameAs(domainRetornado);
    }

    @Test
    @DisplayName("saveAll mapeia toda a lista e retorna entidades salvas mapeadas de volta")
    void deveSalvarTodasAsReservas() {
        Reserva d1 = reservaDominio();
        Reserva d2 = reservaDominio();
        ReservaEntity e1 = entity(d1.getId().value());
        ReservaEntity e2 = entity(d2.getId().value());

        when(mapper.toEntity(d1)).thenReturn(e1);
        when(mapper.toEntity(d2)).thenReturn(e2);
        when(jpaRepository.saveAll(List.of(e1, e2))).thenReturn(List.of(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(d1);
        when(mapper.toDomain(e2)).thenReturn(d2);

        List<Reserva> resultado = repository().saveAll(List.of(d1, d2));

        assertThat(resultado).containsExactly(d1, d2);
    }

    @Test
    @DisplayName("findById retorna vazio quando entidade está com soft-delete marcado")
    void deveRetornarVazioQuandoEntidadeDeletada() {
        UUID id = UUID.randomUUID();
        ReservaEntity deletada = entity(id);
        deletada.setDeletedAt(Instant.now());
        when(jpaRepository.findById(id)).thenReturn(Optional.of(deletada));

        Optional<Reserva> resultado = repository().findById(new ReservaId(id));

        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("findById retorna reserva mapeada quando entidade não está deletada")
    void deveRetornarReservaQuandoEntidadeNaoDeletada() {
        UUID id = UUID.randomUUID();
        ReservaEntity ativa = entity(id);
        Reserva dominio = reservaDominio();
        when(jpaRepository.findById(id)).thenReturn(Optional.of(ativa));
        when(mapper.toDomain(ativa)).thenReturn(dominio);

        Optional<Reserva> resultado = repository().findById(new ReservaId(id));

        assertThat(resultado).contains(dominio);
    }

    @Test
    @DisplayName("findById retorna vazio quando entidade não existe")
    void deveRetornarVazioQuandoEntidadeNaoExiste() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        assertThat(repository().findById(new ReservaId(id))).isEmpty();
    }

    @Test
    @DisplayName("findConflitosPotenciaisComLock delega ao jpaRepository convertendo o tipo para String")
    void deveDelegarBuscaDeConflitos() {
        UUID itemId = UUID.randomUUID();
        Instant inicio = Instant.now();
        Instant fim = inicio.plusSeconds(3600);
        UUID ignorar = UUID.randomUUID();
        ReservaEntity e = entity(UUID.randomUUID());
        Reserva d = reservaDominio();

        when(jpaRepository.findConflitosPotenciaisComLock("LOCAL", itemId, inicio, fim, ignorar))
                .thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        List<Reserva> resultado = repository()
                .findConflitosPotenciaisComLock(TipoItemReserva.LOCAL, itemId, inicio, fim, ignorar);

        assertThat(resultado).containsExactly(d);
    }

    @Test
    @DisplayName("findPendentesExpiracao delega e mapeia lista de entidades")
    void deveBuscarPendentesExpiracao() {
        Instant limite = Instant.now();
        ReservaEntity e = entity(UUID.randomUUID());
        Reserva d = reservaDominio();
        when(jpaRepository.findPendentesExpiracao(limite)).thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        assertThat(repository().findPendentesExpiracao(limite)).containsExactly(d);
    }

    @Test
    @DisplayName("findByEventoId delega ao método específico do jpaRepository")
    void deveBuscarPorEventoId() {
        UUID eventoId = UUID.randomUUID();
        ReservaEntity e = entity(UUID.randomUUID());
        Reserva d = reservaDominio();
        when(jpaRepository.findByEventoIdAndDeletedAtIsNull(eventoId)).thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        assertThat(repository().findByEventoId(eventoId)).containsExactly(d);
    }

    @Test
    @DisplayName("findByGrupoRecorrenciaId delega ao método específico do jpaRepository")
    void deveBuscarPorGrupoRecorrenciaId() {
        UUID grupoId = UUID.randomUUID();
        ReservaEntity e = entity(UUID.randomUUID());
        Reserva d = reservaDominio();
        when(jpaRepository.findByGrupoRecorrenciaIdAndDeletedAtIsNull(grupoId)).thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        assertThat(repository().findByGrupoRecorrenciaId(grupoId)).containsExactly(d);
    }

    @Test
    @DisplayName("findAll com filtro nulo consulta apenas via Specification sem lançar exceção")
    void deveListarComFiltroNulo() {
        ReservaEntity e = entity(UUID.randomUUID());
        Reserva d = reservaDominio();
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        List<Reserva> resultado = repository().findAll(null);

        assertThat(resultado).containsExactly(d);
    }

    @Test
    @DisplayName("findAll com filtro completo aplica todos os predicados sem lançar exceção")
    void deveListarComFiltroCompleto() {
        ReservaFiltro filtro = new ReservaFiltro(
                TipoItemReserva.ATIVO,
                UUID.randomUUID(),
                StatusReserva.CONFIRMADA,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now().plusSeconds(3600),
                "missa");
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Reserva> resultado = repository().findAll(filtro);

        assertThat(resultado).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Specification<ReservaEntity> capturarSpecification() {
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(List.of());
        ArgumentCaptor<Specification<ReservaEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        repository().findAll(new ReservaFiltro(
                TipoItemReserva.ATIVO, UUID.randomUUID(), StatusReserva.CONFIRMADA, UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now().plusSeconds(3600), "missa"));
        verify(jpaRepository).findAll(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("findAll monta os predicados da Specification cobrindo todos os filtros informados")
    void deveMontarPredicadosDaSpecificationComFiltroCompleto() {
        Specification<ReservaEntity> spec = capturarSpecification();

        Root<ReservaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);
        Predicate[] combinedHolder = new Predicate[1];

        org.mockito.Mockito.lenient().when(root.get(org.mockito.ArgumentMatchers.anyString())).thenReturn(path);
        org.mockito.Mockito.lenient().when(cb.isNull(any())).thenReturn(predicate);
        org.mockito.Mockito.lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        org.mockito.Mockito.lenient().when(cb.greaterThanOrEqualTo(
                        (jakarta.persistence.criteria.Expression) any(), (Instant) any()))
                .thenReturn(predicate);
        org.mockito.Mockito.lenient().when(cb.lessThanOrEqualTo(
                        (jakarta.persistence.criteria.Expression) any(), (Instant) any()))
                .thenReturn(predicate);
        org.mockito.Mockito.lenient().when(cb.like(any(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(predicate);
        org.mockito.Mockito.lenient().when(cb.lower(any())).thenReturn(path);
        org.mockito.Mockito.lenient().when(cb.asc(any()))
                .thenReturn(mock(jakarta.persistence.criteria.Order.class));
        org.mockito.Mockito.lenient().when(cb.and(any(Predicate[].class))).thenAnswer(inv -> {
            combinedHolder[0] = predicate;
            return predicate;
        });

        Predicate resultado = spec.toPredicate(root, query, cb);

        assertThat(resultado).isNotNull();
        verify(cb).equal(path, "ATIVO");
        verify(cb).like(any(), org.mockito.ArgumentMatchers.contains("missa"));
        verify(query).orderBy(any(jakarta.persistence.criteria.Order.class));
    }

    @Test
    @DisplayName("findAll monta Specification apenas com o predicado de soft-delete quando filtro vazio")
    void deveMontarSpecificationApenasComSoftDeleteQuandoFiltroVazio() {
        when(jpaRepository.findAll(any(Specification.class))).thenReturn(List.of());
        ArgumentCaptor<Specification<ReservaEntity>> captor = ArgumentCaptor.forClass(Specification.class);
        repository().findAll(ReservaFiltro.vazio());
        verify(jpaRepository).findAll(captor.capture());

        Root<ReservaEntity> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(org.mockito.ArgumentMatchers.anyString())).thenReturn(path);
        when(cb.isNull(any())).thenReturn(predicate);
        when(cb.and(any(Predicate[].class))).thenReturn(predicate);

        Predicate resultado = captor.getValue().toPredicate(root, query, cb);

        assertThat(resultado).isNotNull();
        verify(cb).isNull(path);
    }

    @Test
    @DisplayName("countPendentesAprovacao delega ao jpaRepository")
    void deveContarPendentesAprovacao() {
        UUID pastoralId = UUID.randomUUID();
        when(jpaRepository.countPendentesAprovacao(pastoralId)).thenReturn(7L);

        assertThat(repository().countPendentesAprovacao(pastoralId)).isEqualTo(7L);
    }

    @Test
    @DisplayName("countDevolucoesPendentesHoje delega ao jpaRepository usando o limite superior")
    void deveContarDevolucoesPendentesHoje() {
        Instant inicioHoje = Instant.now();
        Instant fimHoje = inicioHoje.plusSeconds(86400);
        when(jpaRepository.countDevolucoesPendentesHoje(fimHoje)).thenReturn(3L);

        assertThat(repository().countDevolucoesPendentesHoje(inicioHoje, fimHoje)).isEqualTo(3L);
        verify(jpaRepository).countDevolucoesPendentesHoje(fimHoje);
    }
}
