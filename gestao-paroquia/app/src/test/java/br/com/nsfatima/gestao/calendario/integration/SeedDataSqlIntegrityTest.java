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
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.auditoria_operacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projeto_solicitacoes_aprovacao;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.notificacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.solicitacoes_ajuste_clerical;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.solicitacoes_ajuste_cadastral;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.reservas;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.relacionamentos_familiares;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.ativo_manutencoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.observacoes_ativo_patrimonial;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.ativos_patrimoniais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.patrimonio_numeracao_anual;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.encargos_clericais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.migracoes_clero_pendentes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.observacoes_evento;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.aprovacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projeto_solicitacoes_aprovacao;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.auditoria_operacoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos_envolvidos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.eventos_recorrencia;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.projetos_eventos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.anos_paroquiais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.membro_grupo;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.usuarios;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.clerigos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.locais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.grupo_paroquial;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.unidade_territorial;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.papel_global;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.tb_ciclo_liturgico;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.anos_liturgicos;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.pastorais_movimentos_oficiais;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.paroquia_configuracao;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.tb_solenidade_liturgica;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.cpc_decisoes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.fiel_dependentes;");
        jdbcTemplate.execute("TRUNCATE TABLE calendario.fieis;");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE;");
    }

    @Test
    void seedDataSqlShouldExecuteWithoutErrorsAndPopulateTables() throws Exception {
        Path seedFilePath = Path.of("../docker/seed-data.sql");
        assertTrue(Files.exists(seedFilePath), "seed-data.sql file must exist in docker directory");

        String sqlContent = Files.readString(seedFilePath);

        // Remove PL/pgSQL DO block for H2 execution in tests
        int doIndex = sqlContent.indexOf("DO $$");
        int endDoIndex = sqlContent.indexOf("END $$;");
        if (doIndex != -1 && endDoIndex != -1) {
            sqlContent = sqlContent.substring(0, doIndex) + sqlContent.substring(endDoIndex + "END $$;".length());
        }

        // Replace PostgreSQL ON CONFLICT (including multiline DO UPDATE SET) for H2 compatibility in tests
        sqlContent = sqlContent.replaceAll("(?s)ON CONFLICT\\b[^;]*", "");

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
