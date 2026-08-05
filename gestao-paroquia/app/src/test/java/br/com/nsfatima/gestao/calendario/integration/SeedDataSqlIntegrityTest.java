package br.com.nsfatima.gestao.calendario.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SeedDataSqlIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void seedDataSqlShouldExecuteWithoutErrorsAndPopulateTables() throws Exception {
        Path seedFilePath = Path.of("../docker/seed-data.sql");
        assertTrue(Files.exists(seedFilePath), "seed-data.sql file must exist in docker directory");

        String sqlContent = Files.readString(seedFilePath);

        // Clean tables before running seed in H2
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.auditoria_operacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projeto_solicitacoes_aprovacao;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.observacoes_evento;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.aprovacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos_recorrencia;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projetos_eventos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.anos_paroquiais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.membro_grupo;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.usuarios;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.locais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.organizacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.tb_ciclo_liturgico;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.anos_liturgicos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.pastorais_movimentos_oficiais;");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");

        // Remove PL/pgSQL DO block for H2 execution in tests
        int doIndex = sqlContent.indexOf("DO $$");
        int endDoIndex = sqlContent.indexOf("END $$;");
        if (doIndex != -1 && endDoIndex != -1) {
            sqlContent = sqlContent.substring(0, doIndex) + sqlContent.substring(endDoIndex + "END $$;".length());
        }

        // Replace PostgreSQL ON CONFLICT for H2 compatibility in tests
        sqlContent = sqlContent.replaceAll("ON CONFLICT\\s*\\([^)]+\\)\\s*DO NOTHING", "")
                               .replaceAll(
                                       "ON CONFLICT\\s*\\(ano\\)\\s*DO UPDATE SET status = EXCLUDED.status,"
                                       + " data_fechamento_utc = EXCLUDED.data_fechamento_utc", "")
                               .replaceAll("ON CONFLICT\\s*DO NOTHING", "");

        String[] statements = sqlContent.split(";");

        assertDoesNotThrow(() -> {
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    jdbcTemplate.execute(trimmed);
                }
            }
        }, "Executing seed-data.sql statements should not throw any foreign key or SQL execution errors");

        Integer organizacoesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.organizacoes", Integer.class);
        Integer locaisCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.locais", Integer.class);
        Integer eventosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos", Integer.class);

        assertTrue(organizacoesCount != null && organizacoesCount > 0, "Organizacoes should be populated");
        assertTrue(locaisCount != null && locaisCount > 0, "Locais should be populated");
        assertTrue(eventosCount != null && eventosCount > 0, "Eventos should be populated");
    }
}
