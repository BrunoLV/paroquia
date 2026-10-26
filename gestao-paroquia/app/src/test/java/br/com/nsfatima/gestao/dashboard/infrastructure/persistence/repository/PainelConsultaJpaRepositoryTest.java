package br.com.nsfatima.gestao.dashboard.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ContagemPorProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.EstatisticaEventosProjeto;
import br.com.nsfatima.gestao.dashboard.infrastructure.persistence.projection.ReservaLocalIntervalo;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.repository.ReservaJpaRepository;
import br.com.nsfatima.gestao.support.infrastructure.configuration.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Consultas agregadas do painel contra PostgreSQL real (Testcontainers). */
@DataJpaTest
@Import(JpaAuditingConfig.class)
@ActiveProfiles("test")
class PainelConsultaJpaRepositoryTest {

    private static final Instant T0 = Instant.parse("2026-06-10T12:00:00Z");
    private static final Set<TipoOrganizacao> ENGAJAMENTO =
            EnumSet.of(TipoOrganizacao.PASTORAL, TipoOrganizacao.MOVIMENTO);

    @Autowired
    private PainelConsultaJpaRepository painel;
    @Autowired
    private EventoJpaRepository eventoRepository;
    @Autowired
    private ProjetoEventoJpaRepository projetoRepository;
    @Autowired
    private AprovacaoJpaRepository aprovacaoRepository;
    @Autowired
    private ReservaJpaRepository reservaRepository;
    @Autowired
    private FielJpaRepository fielRepository;
    @Autowired
    private MembroGrupoJpaRepository membroRepository;
    @Autowired
    private JdbcTemplate jdbc;

    private UUID organizacao(String nome) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.organizacao (id, nome) VALUES (?, ?)", id, nome + " " + id);
        return id;
    }

    private UUID pastoral(String nome) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.organizacao (id, nome, tipo) VALUES (?, ?, 'PASTORAL')", id,
                nome + " " + id);
        return id;
    }

    private ProjetoEventoEntity projeto(UUID org, ProjetoStatus status) {
        ProjetoEventoEntity p = new ProjetoEventoEntity();
        p.setId(UUID.randomUUID());
        p.setNome("Projeto " + p.getId());
        p.setStatus(status);
        p.setOrganizacaoResponsavelId(org);
        return projetoRepository.save(p);
    }

    private EventoEntity evento(UUID org, UUID projetoId, String status, Instant inicio) {
        EventoEntity e = new EventoEntity();
        e.setId(UUID.randomUUID());
        e.setTitulo("Evento");
        e.setOrganizacaoResponsavelId(org);
        e.setProjetoId(projetoId);
        e.setStatus(status);
        e.setInicioUtc(inicio);
        e.setFimUtc(inicio.plusSeconds(3600));
        return eventoRepository.save(e);
    }

    private AprovacaoEntity aprovacao(UUID eventoId, AprovacaoStatus status) {
        AprovacaoEntity a = new AprovacaoEntity();
        a.setId(UUID.randomUUID());
        a.setEventoId(eventoId);
        a.setTipoSolicitacao("CRIACAO_EVENTO");
        a.setAprovadorPapel("PAROCO");
        a.setStatus(status);
        return aprovacaoRepository.save(a);
    }

    @Test
    @DisplayName("findAllInPeriod nao devolve evento cancelado")
    void findAllInPeriodIgnoraCancelados() {
        UUID org = organizacao("Org");
        evento(org, null, "CONFIRMADO", T0);
        evento(org, null, "RASCUNHO", T0.plusSeconds(60));
        evento(org, null, "CANCELADO", T0.plusSeconds(120));

        List<EventoEntity> eventos = eventoRepository.findAllInPeriod(T0.minusSeconds(10), T0.plusSeconds(600));

        assertThat(eventos).extracting(EventoEntity::getStatus).containsExactly("CONFIRMADO", "RASCUNHO");
    }

    @Test
    @DisplayName("Estatisticas, aprovacoes pendentes e organizacoes por projeto saem de consultas agrupadas")
    void agregadosPorProjeto() {
        UUID org = organizacao("Responsavel");
        ProjetoEventoEntity p1 = projeto(org, ProjetoStatus.EM_ANDAMENTO);
        ProjetoEventoEntity p2 = projeto(org, ProjetoStatus.ABERTO);
        evento(org, p1.getId(), "CONFIRMADO", T0.minusSeconds(7200));
        EventoEntity futuro = evento(org, p1.getId(), "RASCUNHO", T0.plusSeconds(7200));
        evento(org, p1.getId(), "CONFIRMADO", T0.plusSeconds(86400));
        aprovacao(futuro.getId(), AprovacaoStatus.PENDENTE);
        aprovacao(futuro.getId(), AprovacaoStatus.APROVADA);
        Set<UUID> ids = Set.of(p1.getId(), p2.getId());

        List<EstatisticaEventosProjeto> estatisticas = painel.estatisticasEventosPorProjeto(ids, T0);
        List<ContagemPorProjeto> pendentes = painel.contarAprovacoesPorProjeto(ids, AprovacaoStatus.PENDENTE);
        List<EventoEntity> marcos = painel.eventosFuturosDosProjetos(ids, T0);
        List<Object[]> nomes = painel.nomesOrganizacoesEnvolvidasPorProjeto(ids);

        assertThat(estatisticas).singleElement().satisfies(e -> {
            assertThat(e.projetoId()).isEqualTo(p1.getId());
            assertThat(e.total()).isEqualTo(3L);
            assertThat(e.realizados()).isEqualTo(1L);
            assertThat(e.pendentes()).isEqualTo(2L);
        });
        assertThat(pendentes).singleElement().satisfies(c -> {
            assertThat(c.projetoId()).isEqualTo(p1.getId());
            assertThat(c.quantidade()).isEqualTo(1L);
        });
        assertThat(marcos).hasSize(2);
        assertThat(nomes).extracting(l -> (UUID) l[0]).contains(p1.getId(), p2.getId());
    }

    @Test
    @DisplayName("Aprovacoes pendentes sao contadas por organizacao do evento e globalmente")
    void aprovacoesPendentesComEscopo() {
        UUID orgA = organizacao("A");
        UUID orgB = organizacao("B");
        aprovacao(evento(orgA, null, "RASCUNHO", T0).getId(), AprovacaoStatus.PENDENTE);
        aprovacao(evento(orgB, null, "RASCUNHO", T0).getId(), AprovacaoStatus.PENDENTE);
        aprovacao(evento(orgB, null, "CONFIRMADO", T0).getId(), AprovacaoStatus.APROVADA);

        long globalAntes = painel.contarAprovacoesDeEvento(AprovacaoStatus.PENDENTE, null);

        assertThat(painel.contarAprovacoesDeEvento(AprovacaoStatus.PENDENTE, orgA)).isEqualTo(1L);
        assertThat(painel.contarAprovacoesDeEvento(AprovacaoStatus.PENDENTE, orgB)).isEqualTo(1L);
        assertThat(globalAntes).isGreaterThanOrEqualTo(2L);
        assertThat(painel.contarAprovacoesDeEventoPorTipo(AprovacaoStatus.PENDENTE, orgA))
                .singleElement().satisfies(l -> {
                    assertThat(l[0]).isEqualTo("CRIACAO_EVENTO");
                    assertThat(((Number) l[1]).longValue()).isEqualTo(1L);
                });
    }

    @Test
    @DisplayName("Reservas de LOCAL na janela respeitam status, janela e organizacao solicitante")
    void reservasDeLocal() {
        UUID local = UUID.randomUUID();
        UUID orgA = organizacao("A");
        UUID orgB = organizacao("B");
        reserva("LOCAL", local, orgA, "CONFIRMADA", T0, T0.plusSeconds(3600));
        reserva("LOCAL", local, orgB, "PENDENTE", T0.plusSeconds(7200), T0.plusSeconds(10800));
        reserva("LOCAL", local, orgA, "CANCELADA", T0, T0.plusSeconds(3600));
        reserva("ATIVO", UUID.randomUUID(), orgA, "CONFIRMADA", T0, T0.plusSeconds(3600));
        reserva("LOCAL", local, orgA, "CONFIRMADA", T0.plusSeconds(864000), T0.plusSeconds(867600));

        List<ReservaLocalIntervalo> todas = painel.reservasDeLocalNaJanela(
                T0.minusSeconds(60), T0.plusSeconds(86400), null);
        List<ReservaLocalIntervalo> daOrgA = painel.reservasDeLocalNaJanela(
                T0.minusSeconds(60), T0.plusSeconds(86400), orgA);

        assertThat(todas).hasSize(2);
        assertThat(daOrgA).singleElement().satisfies(r -> assertThat(r.localId()).isEqualTo(local));
    }

    @Test
    @DisplayName("Engajamento exige fiel ativo; escopo conta so fieis ativos vinculados a organizacao")
    void fieisComEscopoEEngajamentoExigemFielAtivo() {
        UUID pastoral = pastoral("Pastoral");
        UUID outra = pastoral("Outra");
        long engajadosAntes = membroRepository.contarFieisDistintosEngajados(ENGAJAMENTO);
        UUID ativo = fiel(true);
        UUID inativo = fiel(false);
        vinculo(ativo, pastoral, TipoOrganizacao.PASTORAL);
        vinculo(inativo, pastoral, TipoOrganizacao.PASTORAL);
        vinculo(ativo, outra, TipoOrganizacao.PASTORAL);

        assertThat(painel.contarFieisAtivosDaOrganizacao(pastoral)).isEqualTo(1L);
        assertThat(painel.contarFieisEngajadosDaOrganizacao(pastoral, ENGAJAMENTO)).isEqualTo(1L);
        assertThat(painel.contarFieisNovosDaOrganizacao(pastoral, Instant.parse("2000-01-01T00:00:00Z")))
                .isEqualTo(1L);
        assertThat(painel.contarFieisNovosDaOrganizacao(pastoral, Instant.parse("2999-01-01T00:00:00Z")))
                .isZero();
        assertThat(membroRepository.contarFieisDistintosEngajados(ENGAJAMENTO)).isEqualTo(engajadosAntes + 1);
    }

    private void reserva(String tipo, UUID item, UUID org, String status, Instant inicio, Instant fim) {
        ReservaEntity r = new ReservaEntity();
        r.setId(UUID.randomUUID());
        r.setTipoItem(tipo);
        r.setItemId(item);
        r.setDataHoraInicio(inicio);
        r.setDataHoraFim(fim);
        r.setDescricao("reserva");
        r.setSolicitanteId(usuario());
        r.setPastoralMovimentoId(org);
        r.setStatus(status);
        reservaRepository.save(r);
    }

    private UUID usuario() {
        UUID id = UUID.randomUUID();
        UUID fielId = fiel(true);
        fielRepository.flush();
        jdbc.update("INSERT INTO calendario.usuarios (id, username, password_hash, enabled, fiel_id,"
                + " criado_em_utc, atualizado_em_utc) VALUES (?, ?, '{noop}x', TRUE, ?, CURRENT_TIMESTAMP,"
                + " CURRENT_TIMESTAMP)", id, "u-" + id, fielId);
        return id;
    }

    private UUID fiel(boolean ativo) {
        FielEntity f = new FielEntity();
        f.setId(UUID.randomUUID());
        f.setNome("Fiel " + f.getId());
        f.setDataNascimento(LocalDate.of(1990, 1, 1));
        f.setAtivo(ativo);
        return fielRepository.save(f).getId();
    }

    private void vinculo(UUID fielId, UUID org, TipoOrganizacao tipo) {
        MembroGrupoEntity m = new MembroGrupoEntity();
        m.setId(UUID.randomUUID());
        m.setFielId(fielId);
        m.setOrganizacaoId(org);
        m.setTipoOrganizacao(tipo);
        m.setPapel("membro-ativo");
        m.setAtivo(true);
        membroRepository.save(m);
    }
}
