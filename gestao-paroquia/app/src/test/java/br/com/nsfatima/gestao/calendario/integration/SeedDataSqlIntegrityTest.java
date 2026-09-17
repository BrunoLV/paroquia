package br.com.nsfatima.gestao.calendario.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SeedDataSqlIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    @AfterEach
    void cleanTables() {
        jdbcTemplate.execute("SET session_replication_role = 'replica';");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.auditoria_operacoes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projeto_solicitacoes_aprovacao CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.notificacoes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.solicitacoes_ajuste_clerical CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.solicitacoes_ajuste_cadastral CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.reservas CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.relacionamentos_familiares CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.ativo_manutencoes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.observacoes_ativo_patrimonial CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.ativos_patrimoniais CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.patrimonio_numeracao_anual CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.encargos_clericais CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.migracoes_clero_pendentes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.observacoes_evento CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.aprovacoes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos_envolvidos CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos_recorrencia CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projetos_eventos CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.anos_paroquiais CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.membro_grupo CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.usuarios CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.clerigos CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.locais CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.grupo_paroquial CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.unidade_territorial CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.papel_global CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.tb_ciclo_liturgico CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.anos_liturgicos CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.pastorais_movimentos_oficiais CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.paroquia_configuracao CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.tb_solenidade_liturgica CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.cpc_decisoes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.fiel_dependentes CASCADE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.fieis CASCADE;");
        jdbcTemplate.execute("SET session_replication_role = 'origin';");
    }

    @Test
    void seedDataSqlShouldExecuteWithoutErrorsAndPopulateTables() throws Exception {
        Path seedFilePath = Path.of("../docker/seed-data.sql");
        assertTrue(Files.exists(seedFilePath), "seed-data.sql file must exist in docker directory");

        String sqlContent = Files.readString(seedFilePath);

        assertDoesNotThrow(() -> {
            var dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sqlContent);
            }
        }, "Executing seed-data.sql statements should not throw any foreign key or SQL execution errors");

        Integer organizacoesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.grupo_paroquial", Integer.class);
        Integer unidadesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.unidade_territorial", Integer.class);
        Integer locaisCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.locais", Integer.class);
        Integer eventosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos", Integer.class);
        Integer fieisAtivosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.fieis WHERE ativo = TRUE", Integer.class);
        Integer clerigosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.clerigos", Integer.class);
        Integer reservasCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.reservas", Integer.class);
        Integer notificacoesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.notificacoes", Integer.class);
        Integer ajustesCadastraisCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.solicitacoes_ajuste_cadastral", Integer.class);
        Integer ajustesClericaisCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.solicitacoes_ajuste_clerical", Integer.class);
        Integer relacionamentosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.relacionamentos_familiares", Integer.class);

        assertTrue(organizacoesCount != null && organizacoesCount > 0, "Organizacoes should be populated");
        assertTrue(unidadesCount != null && unidadesCount > 0, "UnidadeTerritorial should be populated");
        assertTrue(locaisCount != null && locaisCount > 0, "Locais should be populated");
        assertTrue(eventosCount != null && eventosCount > 0, "Eventos should be populated");
        assertTrue(fieisAtivosCount != null && fieisAtivosCount > 0,
                "Seed deve incluir fiéis ativos");
        assertTrue(clerigosCount != null && clerigosCount >= 4,
                "Seed deve incluir clérigos (pároco, vigário, diácono, emérito)");
        assertTrue(reservasCount != null && reservasCount >= 6,
                "Seed deve incluir reservas cobrindo LOCAL e ATIVO em múltiplos estados");
        assertTrue(notificacoesCount != null && notificacoesCount >= 3,
                "Seed deve incluir notificações lidas e não-lidas");
        assertTrue(ajustesCadastraisCount != null && ajustesCadastraisCount >= 3,
                "Seed deve incluir solicitações de ajuste cadastral em múltiplos estados");
        assertTrue(ajustesClericaisCount != null && ajustesClericaisCount >= 2,
                "Seed deve incluir solicitações de ajuste clerical");
        assertTrue(relacionamentosCount != null && relacionamentosCount >= 6,
                "Seed deve incluir relacionamentos familiares polimórficos");
    }
}
