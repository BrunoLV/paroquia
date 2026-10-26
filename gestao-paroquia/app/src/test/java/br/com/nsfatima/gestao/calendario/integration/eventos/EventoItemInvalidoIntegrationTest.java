package br.com.nsfatima.gestao.calendario.integration.eventos;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criar evento com local inativo ou ativo não reservável falha com erro claro (400), sem criar o evento nem
 * reservas: as reservas do evento passam pelas mesmas validações de item de uma reserva avulsa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:sql/security-fixtures.sql", "classpath:sql/locais-fixtures.sql"})
class EventoItemInvalidoIntegrationTest {

    private static final String ORG_ID = "00000000-0000-0000-0000-000000000001";

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private DataSource dataSource;

    private final UUID localInativoId = UUID.randomUUID();
    private final UUID ativoNaoReservavelId = UUID.randomUUID();

    @BeforeEach
    void criarItens() throws SQLException {
        jdbc.update("INSERT INTO calendario.locais (id, nome, tipo, ativo) "
                + "VALUES (?, 'Salão Inativo IT', 'SALAO_PAROQUIAL', false)", localInativoId);
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("SET session_replication_role = replica");
            s.execute("INSERT INTO calendario.ativos_patrimoniais (id, nome, codigo_tombamento, categoria, "
                    + "is_reservavel, organizacao_id, data_aquisicao, valor_aquisicao, criado_em_utc, "
                    + "atualizado_em_utc) VALUES ('" + ativoNaoReservavelId + "', 'Ativo IT fixo', 'PAT-2026-9201', "
                    + "'MOBILIARIO', false, '" + ORG_ID + "', CURRENT_DATE, 10, now(), now())");
            s.execute("SET session_replication_role = DEFAULT");
        }
    }

    @AfterEach
    void limpar() {
        jdbc.update("DELETE FROM calendario.locais WHERE id = ?", localInativoId);
        jdbc.update("DELETE FROM calendario.ativos_patrimoniais WHERE id = ?", ativoNaoReservavelId);
    }

    private void criarEvento(String itensJson, String titulo) throws Exception {
        String payload = """
                {
                  "titulo": "%s",
                  "organizacaoResponsavelId": "%s",
                  %s
                  "inicio": "2026-12-05T10:00:00Z",
                  "fim": "2026-12-05T12:00:00Z"
                }
                """.formatted(titulo, ORG_ID, itensJson);
        mockMvc.perform(post("/api/v1/eventos")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO")
                        .header("X-Actor-Org-Id", ORG_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("Evento com local inativo é recusado com 400 e não é criado")
    void eventoComLocalInativoERecusado() throws Exception {
        criarEvento("\"localId\": \"" + localInativoId + "\",", "Evento Local Inativo IT");

        Integer criados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE titulo = 'Evento Local Inativo IT'", Integer.class);
        assertThat(criados).isZero();
    }

    @Test
    @DisplayName("Evento com ativo não reservável é recusado com 400 e não é criado")
    void eventoComAtivoNaoReservavelERecusado() throws Exception {
        criarEvento("\"ativoIds\": [\"" + ativoNaoReservavelId + "\"],", "Evento Ativo Fixo IT");

        Integer criados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE titulo = 'Evento Ativo Fixo IT'", Integer.class);
        assertThat(criados).isZero();
    }
}
