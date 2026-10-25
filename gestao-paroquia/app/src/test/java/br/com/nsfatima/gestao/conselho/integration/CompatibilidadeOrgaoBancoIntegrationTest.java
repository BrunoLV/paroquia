package br.com.nsfatima.gestao.conselho.integration;

import br.com.nsfatima.gestao.conselho.MatrizCompatibilidade;
import br.com.nsfatima.gestao.conselho.MatrizCompatibilidade.Orgao;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O gatilho do banco precisa concordar com a política do domínio: para todo tipo de reunião e todo órgão, o
 * INSERT só passa onde a matriz esperada diz que passa. Cada tentativa roda numa transação descartada.
 */
@SpringBootTest
@DisplayName("Gatilho de compatibilidade entre tipo de reunião e órgão")
class CompatibilidadeOrgaoBancoIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PlatformTransactionManager transactionManager;

    static Stream<Object[]> todasAsCombinacoes() {
        return Stream.of(TipoReuniao.values()).flatMap(tipo -> MatrizCompatibilidade.TODOS.stream()
                .map(orgao -> new Object[] {tipo, orgao}));
    }

    private UUID novoOrgao(Orgao orgao, UUID unidade) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.organizacao (id, nome, tipo, subtipo_conselho, parent_id) "
                + "VALUES (?, ?, ?, ?, ?)", id, "Órgão " + id, orgao.tipo().name(),
                orgao.subtipo() == null ? null : orgao.subtipo().name(), unidade);
        return id;
    }

    private void inserirAta(TipoReuniao tipo, UUID convocante, UUID associada) {
        jdbc.update("INSERT INTO calendario.ata_reuniao (id, tipo, organizacao_convocante_id, "
                + "organizacao_associada_id, titulo, data_reuniao, carater, modalidade, status, conteudo, "
                + "redator_id) VALUES (?, ?, ?, ?, 'Ata', '2026-06-01', 'ORDINARIA', 'PRESENCIAL', 'RASCUNHO', "
                + "'{}', 'teste')", UUID.randomUUID(), tipo.name(), convocante, associada);
    }

    /** Executa e sempre descarta; devolve se o banco aceitou. */
    private boolean aceita(Runnable gravacao) {
        return Boolean.TRUE.equals(new TransactionTemplate(transactionManager).execute(status -> {
            status.setRollbackOnly();
            try {
                gravacao.run();
                return true;
            } catch (DataAccessException recusado) {
                return false;
            }
        }));
    }

    @ParameterizedTest(name = "{0} convocada por {1}")
    @MethodSource("todasAsCombinacoes")
    @DisplayName("o banco aceita só os órgãos da matriz, para todo tipo e todo órgão")
    void bancoSegueAMatriz(TipoReuniao tipo, Orgao orgao) {
        boolean aceita = aceita(() -> {
            UUID convocante = novoOrgao(orgao, null);
            UUID associada = tipo == TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP
                    ? novoOrgao(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP), null) : null;
            inserirAta(tipo, convocante, associada);
        });

        assertThat(aceita).isEqualTo(MatrizCompatibilidade.pode(tipo, orgao));
    }

    @Test
    @DisplayName("a reunião conjunta só passa com o CAEP da mesma unidade do CPP")
    void conjuntaPorUnidade() {
        UUID matriz = UUID.randomUUID();
        UUID comunidade = UUID.randomUUID();
        for (UUID unidade : new UUID[] {matriz, comunidade}) {
            jdbc.update("INSERT INTO calendario.unidade_territorial (id, nome, tipo) VALUES (?, ?, ?)",
                    unidade, "Unidade " + unidade, unidade.equals(matriz) ? "MATRIZ" : "COMUNIDADE");
        }
        var conselho = TipoOrganizacao.CONSELHO;
        var cpp = SubtipoConselho.CPP;
        var caep = SubtipoConselho.CAEP;

        boolean mesmaUnidade = aceita(() -> inserirAta(TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP,
                novoOrgao(new Orgao(conselho, cpp), matriz), novoOrgao(new Orgao(conselho, caep), matriz)));
        boolean outraUnidade = aceita(() -> inserirAta(TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP,
                novoOrgao(new Orgao(conselho, cpp), matriz), novoOrgao(new Orgao(conselho, caep), comunidade)));

        assertThat(mesmaUnidade).isTrue();
        assertThat(outraUnidade).isFalse();
    }
}
