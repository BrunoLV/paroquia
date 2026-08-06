package br.com.nsfatima.gestao.calendario.performance;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de performance Tier-1 para consulta do calendário de eventos.
 *
 * <p>Valida que o endpoint {@code GET /api/v1/eventos} mantém latência P95 dentro
 * do limite operacional de CI de 2000ms sobre o banco H2 em memória com
 * {@link #SAMPLE_SIZE} amostras quentes (após warmup de 1 request).
 *
 * <p>Este teste NÃO é um microbenchmark de produção; é um guardrail de regressão
 * que detecta degradações grosseiras (ex.: N+1 queries, joins cartesianos).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class ConsultaCalendarioPerformanceTest {

    /** Limite P95 tolerado para consulta de calendário em ambiente CI (H2 em memória). */
    private static final long P95_LIMIT_MS = 2000L;

    /** Número de amostras para calcular P95; mínimo de 10 para ter percentil significativo. */
    private static final int SAMPLE_SIZE = 10;

    /** Número de eventos semeados para simular carga realista de um mês paroquial. */
    private static final int SEED_EVENT_COUNT = 30;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    private MockHttpSession autenticadoSession;

    private final UUID grupoParoquialId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

    @BeforeEach
    void setUp() throws Exception {
        eventoJpaRepository.deleteAll();
        seedEventos();
        autenticadoSession = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");
    }

    /**
     * Verifica que a consulta paginada do calendário mantém P95 dentro do limite
     * de CI com 30 eventos previamente semeados e filtro por período de um mês.
     */
    @Test
    void deveManterConsultaLatenciaDentroEsperadoJanela() throws Exception {
        // Warmup: 1 request para inicializar caches e planos de query
        realizarConsultaCalendario();

        List<Long> latencias = new ArrayList<>();
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            long inicio = System.nanoTime();
            realizarConsultaCalendario();
            latencias.add((System.nanoTime() - inicio) / 1_000_000L);
        }

        long p95Ms = calcularP95(latencias);
        assertThat(p95Ms)
                .as("P95 da consulta do calendário deve estar abaixo de %dms; obtido %dms", P95_LIMIT_MS, p95Ms)
                .isLessThanOrEqualTo(P95_LIMIT_MS);
    }

    private void realizarConsultaCalendario() throws Exception {
        mockMvc.perform(get("/api/v1/eventos")
                        .session(autenticadoSession)
                        .param("inicio", "2026-10-01T00:00:00Z")
                        .param("fim", "2026-10-31T23:59:59Z")
                        .header("X-Actor-Org-Id", grupoParoquialId.toString()))
                .andExpect(status().isOk());
    }

    private void seedEventos() {
        Instant base = Instant.parse("2026-10-01T10:00:00Z");
        for (int i = 0; i < SEED_EVENT_COUNT; i++) {
            EventoEntity entity = new EventoEntity();
            entity.setId(UUID.randomUUID());
            entity.setTitulo("Evento perf " + i);
            entity.setDescricao("Semente para teste de performance consulta calendário");
            entity.setGrupoParoquialResponsavelId(grupoParoquialId);
            entity.setInicioUtc(base.plusSeconds((long) i * 3600));
            entity.setFimUtc(base.plusSeconds((long) i * 3600 + 1800));
            entity.setStatus("CONFIRMADO");
            eventoJpaRepository.save(entity);
        }
    }

    private long calcularP95(List<Long> valores) {
        List<Long> ordenados = new ArrayList<>(valores);
        Collections.sort(ordenados);
        int indice = (int) Math.ceil(0.95 * ordenados.size()) - 1;
        return ordenados.get(Math.max(indice, 0));
    }
}
