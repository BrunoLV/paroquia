package br.com.nsfatima.gestao.calendario.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import br.com.nsfatima.gestao.sacramento.application.service.RequisitosAssentoRegistry;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AssentoSacramentalRepository assentoRepository;

    @Autowired
    private RequisitosAssentoRegistry requisitosAssento;

    @Autowired
    private AtaRepository ataRepository;

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
        assertEquals(39, flyway.info().applied().length,
                "O esquema deve conter as 39 migrações Flyway versionadas (V040-V042 adicionadas pela adequação LGPD)");

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
                "SELECT COUNT(*) FROM calendario.organizacao", Integer.class);
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

        Integer statusMandatosCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT status) FROM calendario.mandato_coordenacao", Integer.class);
        Integer tiposOrganizacaoCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT tipo) FROM calendario.organizacao", Integer.class);
        assertEquals(4, statusMandatosCount, "Seed deve cobrir todos os estados de mandato");
        assertEquals(4, tiposOrganizacaoCount, "Seed deve cobrir todos os tipos de organização");
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

        verificarCenariosDeSacramentos();
    }

    /**
     * Cenários do módulo de sacramentos: cobertura de tipos, estados, livro reservado, vínculos pendentes,
     * averbações, ressalvas, notificações canônicas e trilha; e prova de que todo assento homologado do seed
     * satisfaz os requisitos canônicos do domínio.
     */
    private void verificarCenariosDeSacramentos() {
        assertEquals(4, contar("SELECT COUNT(DISTINCT tipo) FROM calendario.assentos_sacramentais"),
                "Seed deve cobrir Batismo, Confirmação, Matrimônio e Ordem");
        assertEquals(3, contar("SELECT COUNT(DISTINCT status) FROM calendario.assentos_sacramentais"),
                "Seed deve cobrir DRAFT, SEALED e CANCELADO");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assentos_sacramentais "
                + "WHERE visibilidade = 'RESERVADO'") >= 1,
                "Seed deve incluir assento de livro reservado");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assentos_sacramentais WHERE dado_sensivel") >= 1,
                "Seed deve incluir assento com dado sensível (filiação adotiva)");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assento_participantes WHERE vinculo_pendente") >= 2,
                "Seed deve incluir vínculos pendentes de aprovação do Pároco");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assento_participantes "
                + "WHERE fiel_id IS NOT NULL AND NOT vinculo_pendente") >= 1
                && contar("SELECT COUNT(*) FROM calendario.assento_participantes "
                + "WHERE clerigo_id IS NOT NULL AND NOT vinculo_pendente") >= 1,
                "Seed deve incluir vínculos aprovados a fiel e a clérigo");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assento_participantes "
                + "WHERE fiel_id IS NULL AND clerigo_id IS NULL") >= 1,
                "Seed deve incluir participantes sem cadastro (late binding)");
        assertEquals(2, contar("SELECT COUNT(DISTINCT status) FROM calendario.averbacoes_batismo"),
                "Seed deve incluir averbações pendentes e homologadas");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.averbacoes_batismo WHERE reservada") >= 1,
                "Seed deve incluir averbação reservada");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assento_ressalvas") >= 1, "Seed deve incluir ressalva");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assento_notificacoes WHERE enviada_em IS NULL") >= 1
                && contar("SELECT COUNT(*) FROM calendario.assento_notificacoes WHERE enviada_em IS NOT NULL") >= 1,
                "Seed deve incluir notificações canônicas enviadas e ainda não enviadas");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.assento_historico") >= 1, "Seed deve incluir trilha");
        assertTrue(contar("SELECT COUNT(*) FROM calendario.notificacoes "
                + "WHERE tipo = 'SACRAMENTO_VINCULO_PENDENTE'") >= 1,
                "Seed deve notificar o Pároco dos vínculos pendentes");

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            List<UUID> homologados = jdbcTemplate.queryForList(
                    "SELECT id FROM calendario.assentos_sacramentais WHERE status = 'SEALED'", UUID.class);
            assertThat(homologados).isNotEmpty();
            for (UUID id : homologados) {
                AssentoSacramental assento = assentoRepository.findById(
                        new AssentoSacramentalId(id)).orElseThrow();
                assertThat(requisitosAssento.para(assento.getTipo()).pendencias(assento.getTeor()))
                        .as("Assento homologado do seed deve cumprir os requisitos canônicos: %s", id).isEmpty();
                assertThat(assento.getStatus()).isEqualTo(StatusAssento.SEALED);
            }
            assertThat(assentoRepository.listar(null, false))
                    .as("Livro reservado não aparece na listagem da Secretaria")
                    .noneMatch(a -> a.getVisibilidade() == Visibilidade.RESERVADO);
        });
    }

    @Test
    void seedDeAtasDeveCarregarPeloAgregadoCobrindoTodosOsTiposESituacoes() throws Exception {
        var dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(Files.readString(Path.of("../docker/seed-data.sql")));
        }

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            List<UUID> ids = jdbcTemplate.queryForList("SELECT id FROM calendario.ata_reuniao", UUID.class);
            List<AtaReuniao> atas = ids.stream().map(id -> ata(id.toString())).toList();

            assertThat(atas).hasSizeGreaterThanOrEqualTo(24);
            assertThat(atas).extracting(AtaReuniao::getTipo).containsAll(Arrays.asList(TipoReuniao.values()));
            assertThat(atas).extracting(AtaReuniao::getStatus).containsAll(Arrays.asList(StatusAta.values()));
            assertThat(atas).filteredOn(a -> a.getAtaRetificadaId() != null).isNotEmpty();
            assertThat(atas).filteredOn(a -> a.getStatus().selada()).allMatch(a -> a.identificacao().isPresent());
            assertThat(ata("91000000-0000-0000-0000-000000000001").identificacao()).contains("001/2026");
            assertThat(ata("91000000-0000-0000-0000-000000000003").getMotivoVeto()).isNotBlank();
            assertThat(ata("91000000-0000-0000-0000-000000000004").getConteudo().presenca().nominal())
                    .as("O Pároco preside o CPP").anyMatch(p -> p.clerigo() && "Presidente".equals(p.funcao()));
            assertThat(ata("91000000-0000-0000-0000-000000000017").quorum())
                    .as("A reunião do CAEP sem o Pároco não atinge o quórum")
                    .hasValueSatisfying(q -> assertThat(q.atingido()).isFalse());
        });
    }

    @Test
    void cenarioDeAtasDeveTerRedatorComLoginEVinculoDeRedacaoEPorocoParaHomologar() throws Exception {
        var dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(Files.readString(Path.of("../docker/seed-data.sql")));
        }

        assertThat(jdbcTemplate.queryForList("""
                SELECT a.id::text || ' ' || a.redator_id
                FROM calendario.ata_reuniao a
                WHERE NOT EXISTS (
                    SELECT 1 FROM calendario.usuarios u
                    JOIN calendario.membro_grupo m ON m.fiel_id = u.fiel_id
                    WHERE u.username = a.redator_id AND u.enabled AND m.ativo
                      AND m.organizacao_id = a.organizacao_convocante_id
                      AND m.papel IN ('secretario-conselho', 'secretario-grupo', 'coordenador'))
                """, String.class))
                .as("Todo redator da massa precisa ter login e papel de redação no órgão convocante")
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("""
                SELECT a.conteudo::jsonb ->> 'local'
                FROM calendario.ata_reuniao a
                WHERE NOT EXISTS (SELECT 1 FROM calendario.locais l WHERE l.nome = a.conteudo::jsonb ->> 'local')
                """, String.class))
                .as("O local de cada ata precisa ser um local cadastrado").isEmpty();
        assertThat(contar("SELECT COUNT(*) FROM calendario.usuarios WHERE username = 'padre.pedro' "
                + "AND roles = 'PAROCO'")).isEqualTo(1);
        assertThat(contar("SELECT COUNT(*) FROM calendario.ata_reuniao WHERE status = 'PENDENTE_HOMOLOGACAO'"))
                .as("O Pároco precisa ter atas para homologar").isPositive();
    }

    @Test
    void todoConselhoDeveTerCoordenadorComoMembro() throws Exception {
        var dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(Files.readString(Path.of("../docker/seed-data.sql")));
        }

        assertThat(jdbcTemplate.queryForList("""
                SELECT o.nome FROM calendario.organizacao o
                WHERE o.tipo = 'CONSELHO' AND NOT EXISTS (
                    SELECT 1 FROM calendario.membro_grupo m
                    WHERE m.organizacao_id = o.id AND m.ativo AND m.papel = 'coordenador')
                """, String.class)).as("Todo conselho tem um coordenador que é membro, com esse papel").isEmpty();
    }

    @Test
    void todoVinculoDaMassaDeveTerPapelValidoParaOTipoDaOrganizacao() throws Exception {
        var dataSource = Objects.requireNonNull(jdbcTemplate.getDataSource());
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(Files.readString(Path.of("../docker/seed-data.sql")));
        }

        List<String> invalidos = jdbcTemplate.query("""
                SELECT o.nome || ': ' || m.papel, o.tipo, m.papel
                FROM calendario.membro_grupo m JOIN calendario.organizacao o ON o.id = m.organizacao_id
                """, (rs, i) -> PapeisPorTipoOrganizacao.aceita(TipoOrganizacao.valueOf(rs.getString(2)),
                        PapelOrganizacional.fromStoredValue(rs.getString(3))) ? null : rs.getString(1))
                .stream().filter(Objects::nonNull).toList();

        assertThat(invalidos).as("Vínculos com papel que o tipo de organização não admite").isEmpty();
    }

    private AtaReuniao ata(String id) {
        return ataRepository.buscarPorId(AtaReuniaoId.fromString(id)).orElseThrow();
    }

    private int contar(String sql) {
        Integer total = jdbcTemplate.queryForObject(sql, Integer.class);
        return total == null ? 0 : total;
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
