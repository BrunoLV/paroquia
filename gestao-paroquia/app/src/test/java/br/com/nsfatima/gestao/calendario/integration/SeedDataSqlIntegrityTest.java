package br.com.nsfatima.gestao.calendario.integration;

import org.flywaydb.core.Flyway;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class SeedDataSqlIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    @AfterEach
    void cleanTables() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE tabela RECORD;
                BEGIN
                    FOR tabela IN
                        SELECT tablename
                        FROM pg_tables
                        WHERE schemaname = 'calendario'
                    AND tablename NOT IN (
                        'flyway_schema_history',
                        'anos_liturgicos',
                        'tb_ciclo_liturgico',
                        'tb_solenidade_liturgica'
                    )
                    LOOP
                        EXECUTE 'TRUNCATE TABLE calendario.' || quote_ident(tabela.tablename)
                            || ' RESTART IDENTITY CASCADE';
                    END LOOP;
                END $$;
                """);
    }

    @Test
    void seedDataSqlShouldExecuteWithoutErrorsAndPopulateTables() throws Exception {
        assertEquals(24, flyway.info().applied().length,
                "O baseline deve manter uma migração Flyway por contexto com persistência");

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
        Integer diocesesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.dioceses", Integer.class);
        Integer governoEclesiasticoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.governo_eclesiastico", Integer.class);
        Integer unidadesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.unidade_territorial", Integer.class);
        Integer unidadesComPadroeiroCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.unidade_territorial WHERE padroeiro IS NOT NULL", Integer.class);
        Integer unidadesComAvcbCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.unidade_territorial WHERE avcb_numero IS NOT NULL", Integer.class);
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
        Integer idempotencyCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.evento_idempotency", Integer.class);
        Integer revisoesNotasCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.observacoes_nota_revisoes", Integer.class);
        Integer statusReservasCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT status) FROM calendario.reservas", Integer.class);
        Integer statusAprovacoesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT status) FROM calendario.aprovacoes", Integer.class);
        Integer aprovacoesFilhasCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.aprovacoes WHERE aprovacao_pai_id IS NOT NULL", Integer.class);
        Integer aprovacoesConflitosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.aprovacoes WHERE conflitos_json IS NOT NULL", Integer.class);
        Integer aprovacoesJustificativasCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.aprovacoes WHERE justificativa IS NOT NULL", Integer.class);
        Integer secretarioUsuariosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.usuarios WHERE roles LIKE '%SECRETARIO%'", Integer.class);
        Integer eventosFalhaExecucaoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE status = 'FALHA_EXECUCAO'", Integer.class);
        Integer eventosConflitoPendenteCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE conflict_state = 'PENDENTE'", Integer.class);
        Integer eventosDiaInteiroCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE tipo_horario = 'DIA_INTEIRO'", Integer.class);
        Integer eventosMadrugadaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE tipo_horario = 'MADRUGADA'", Integer.class);
        Integer eventosManhaCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE tipo_horario = 'MANHA'", Integer.class);
        Integer eventosTardeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE tipo_horario = 'TARDE'", Integer.class);
        Integer eventosNoiteCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE tipo_horario = 'NOITE'", Integer.class);
        Integer eventosOnlineCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE formato = 'ONLINE'", Integer.class);
        Integer eventosHibridoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM calendario.eventos WHERE formato = 'HIBRIDO'", Integer.class);

        assertTrue(organizacoesCount != null && organizacoesCount > 0, "Organizacoes should be populated");
        assertTrue(diocesesCount != null && diocesesCount > 0, "Seed deve incluir dioceses cadastradas");
        assertTrue(governoEclesiasticoCount != null && governoEclesiasticoCount > 0,
                "Seed deve incluir governo eclesiástico configurado");
        assertTrue(unidadesCount != null && unidadesCount > 0, "UnidadeTerritorial should be populated");
        assertTrue(unidadesComPadroeiroCount != null && unidadesComPadroeiroCount >= 4,
                "Unidades territoriais devem possuir padroeiros configurados");
        assertTrue(unidadesComAvcbCount != null && unidadesComAvcbCount >= 3,
                "Unidades territoriais devem possuir dados de bombeiros/AVCB");
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
        assertTrue(idempotencyCount != null && idempotencyCount > 0,
                "Seed deve incluir comando idempotente de criação de evento");
        assertTrue(revisoesNotasCount != null && revisoesNotasCount > 0,
                "Seed deve incluir histórico de revisão de nota de evento");
        assertTrue(statusReservasCount != null && statusReservasCount >= 5,
                "Seed deve incluir reservas em múltiplos estados operacionais");
        assertTrue(statusAprovacoesCount != null && statusAprovacoesCount >= 4,
                "Seed deve incluir decisões pendentes, aprovadas, reprovadas e com falha");
        assertTrue(aprovacoesFilhasCount != null && aprovacoesFilhasCount >= 4,
                "Seed deve incluir ocorrências filhas de aprovação de série recorrente");
        assertTrue(aprovacoesConflitosCount != null && aprovacoesConflitosCount >= 2,
                "Seed deve incluir aprovações com detalhamento analítico de conflitos");
        assertTrue(aprovacoesJustificativasCount != null && aprovacoesJustificativasCount >= 3,
                "Seed deve incluir aprovações com justificativa mandatória");
        assertTrue(secretarioUsuariosCount != null && secretarioUsuariosCount >= 1,
                "Seed deve incluir usuário com papel de Secretário");
        assertTrue(eventosFalhaExecucaoCount != null && eventosFalhaExecucaoCount >= 1,
                "Seed deve incluir eventos com status FALHA_EXECUCAO para validação visual");
        assertTrue(eventosConflitoPendenteCount != null && eventosConflitoPendenteCount >= 1,
                "Seed deve incluir eventos com estado de conflito PENDENTE");
        assertTrue(eventosDiaInteiroCount != null && eventosDiaInteiroCount >= 1,
                "Seed deve incluir eventos no formato DIA_INTEIRO");
        assertTrue(eventosMadrugadaCount != null && eventosMadrugadaCount >= 1,
                "Seed deve incluir eventos no período MADRUGADA");
        assertTrue(eventosManhaCount != null && eventosManhaCount >= 1,
                "Seed deve incluir eventos no período MANHA");
        assertTrue(eventosTardeCount != null && eventosTardeCount >= 1,
                "Seed deve incluir eventos no período TARDE");
        assertTrue(eventosNoiteCount != null && eventosNoiteCount >= 1,
                "Seed deve incluir eventos no período NOITE");
        assertTrue(eventosOnlineCount != null && eventosOnlineCount >= 1,
                "Seed deve incluir eventos no formato ONLINE");
        assertTrue(eventosHibridoCount != null && eventosHibridoCount >= 1,
                "Seed deve incluir eventos no formato HIBRIDO");
    }

    /**
     * CRITÉRIO DE ACEITAÇÃO: o baseline precisa manter o catálogo PostgreSQL
     * autodescritivo para que operações e auditorias entendam cada estrutura sem
     * depender da leitura dos fontes da aplicação.
     */
    @Test
    void deveDocumentarTodasAsTabelasEColunasDoSchemaCalendario() {
        Integer tabelasSemDescricao = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_class tabela
                INNER JOIN pg_namespace esquema ON esquema.oid = tabela.relnamespace
                LEFT JOIN pg_description descricao ON descricao.objoid = tabela.oid
                    AND descricao.objsubid = 0
                WHERE esquema.nspname = 'calendario'
                  AND tabela.relkind = 'r'
                  AND tabela.relname <> 'flyway_schema_history'
                  AND descricao.description IS NULL
                """, Integer.class);
        Integer colunasSemDescricao = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_class tabela
                INNER JOIN pg_namespace esquema ON esquema.oid = tabela.relnamespace
                INNER JOIN pg_attribute coluna ON coluna.attrelid = tabela.oid
                LEFT JOIN pg_description descricao ON descricao.objoid = tabela.oid
                    AND descricao.objsubid = coluna.attnum
                WHERE esquema.nspname = 'calendario'
                  AND tabela.relkind = 'r'
                  AND tabela.relname <> 'flyway_schema_history'
                  AND coluna.attnum > 0
                  AND NOT coluna.attisdropped
                  AND descricao.description IS NULL
                """, Integer.class);

        assertThat(tabelasSemDescricao).isZero();
        assertThat(colunasSemDescricao).isZero();
    }
}
