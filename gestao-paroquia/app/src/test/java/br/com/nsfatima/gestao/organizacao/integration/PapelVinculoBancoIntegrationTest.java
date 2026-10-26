package br.com.nsfatima.gestao.organizacao.integration;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.policy.PapeisPorTipoOrganizacao;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O gatilho do banco precisa concordar com a tabela do domínio ({@link PapeisPorTipoOrganizacao}): para todo tipo
 * de organização e todo papel, o vínculo só é gravado onde a tabela diz que o papel existe. A normalização dos
 * vínculos antigos também é exercitada. Cada tentativa roda numa transação descartada.
 */
@SpringBootTest
@DisplayName("Papel do vínculo por tipo de organização no banco")
class PapelVinculoBancoIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private FielJpaRepository fielRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    static Stream<Object[]> todasAsCombinacoes() {
        return Stream.of(TipoOrganizacao.values()).flatMap(tipo -> Stream.of(PapelOrganizacional.values())
                .map(papel -> new Object[] {tipo, papel}));
    }

    private <T> T descartando(Supplier<T> bloco) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            status.setRollbackOnly();
            return bloco.get();
        });
    }

    private UUID novaOrganizacao(TipoOrganizacao tipo, SubtipoConselho subtipo) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.organizacao (id, nome, tipo, subtipo_conselho) VALUES (?, ?, ?, ?)",
                id, "Organização " + id, tipo.name(), subtipo == null ? null : subtipo.name());
        return id;
    }

    private UUID novoVinculo(UUID organizacao, TipoOrganizacao tipo, String papel) {
        UUID id = UUID.randomUUID();
        UUID fiel = FielTestFixtures.criarFiel(fielRepository);
        fielRepository.flush();
        jdbc.update("INSERT INTO calendario.membro_grupo (id, fiel_id, organizacao_id, tipo_organizacao, papel, "
                + "ativo, criado_em_utc, atualizado_em_utc) VALUES (?, ?, ?, ?, ?, TRUE, now(), now())",
                id, fiel, organizacao, tipo.name(), papel);
        return id;
    }

    @ParameterizedTest(name = "{1} em {0}")
    @MethodSource("todasAsCombinacoes")
    @DisplayName("o banco aceita só os papéis que a tabela do domínio admite, para todo tipo e todo papel")
    void bancoSegueATabela(TipoOrganizacao tipo, PapelOrganizacional papel) {
        boolean aceita = descartando(() -> {
            UUID organizacao = novaOrganizacao(tipo, tipo == TipoOrganizacao.CONSELHO ? SubtipoConselho.CPP : null);
            try {
                novoVinculo(organizacao, tipo, papel.storedValue());
                return true;
            } catch (DataAccessException recusado) {
                return false;
            }
        });

        assertThat(aceita).isEqualTo(PapeisPorTipoOrganizacao.aceita(tipo, papel));
    }

    @Test
    @DisplayName("o papel de membro comum do banco é o do domínio e é válido no tipo")
    void papelDeMembroDoBancoSegueODominio() {
        for (TipoOrganizacao tipo : TipoOrganizacao.values()) {
            for (SubtipoConselho subtipo : tipo == TipoOrganizacao.CONSELHO
                    ? SubtipoConselho.values() : new SubtipoConselho[] {null}) {
                String doBanco = jdbc.queryForObject("SELECT calendario.papel_vinculo_de_membro(?, ?)",
                        String.class, tipo.name(), subtipo == null ? null : subtipo.name());

                assertThat(doBanco).isEqualTo(PapeisPorTipoOrganizacao.papelDeMembro(tipo, subtipo).storedValue());
                assertThat(jdbc.queryForObject("SELECT calendario.papel_vinculo_valido(?, ?)", Boolean.class,
                        tipo.name(), doBanco)).isTrue();
            }
        }
    }

    @Test
    @DisplayName("a normalização troca o papel inválido pelo de membro comum e deixa o válido como está")
    void normalizacaoCorrigeSoOQueEstaInvalido() {
        descartando(() -> {
            jdbc.execute("ALTER TABLE calendario.membro_grupo DISABLE TRIGGER trg_membro_grupo_papel_valido");
            UUID conselho = novaOrganizacao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP);
            UUID pastoral = novaOrganizacao(TipoOrganizacao.PASTORAL, null);
            UUID voluntarioNoConselho = novoVinculo(conselho, TipoOrganizacao.CONSELHO, "voluntario");
            UUID coordenadorNoConselho = novoVinculo(conselho, TipoOrganizacao.CONSELHO, "coordenador");
            UUID secretarioNaPastoral = novoVinculo(pastoral, TipoOrganizacao.PASTORAL, "secretario-conselho");
            UUID membroNaPastoral = novoVinculo(pastoral, TipoOrganizacao.PASTORAL, "membro-ativo");

            jdbc.update("UPDATE calendario.membro_grupo m "
                    + "SET papel = calendario.papel_vinculo_de_membro(o.tipo, o.subtipo_conselho) "
                    + "FROM calendario.organizacao o "
                    + "WHERE o.id = m.organizacao_id AND NOT calendario.papel_vinculo_valido(o.tipo, m.papel) "
                    + "AND m.id IN (?, ?, ?, ?)",
                    voluntarioNoConselho, coordenadorNoConselho, secretarioNaPastoral, membroNaPastoral);

            assertThat(papelDe(voluntarioNoConselho)).isEqualTo("membro-conselho-economico");
            assertThat(papelDe(coordenadorNoConselho)).isEqualTo("coordenador");
            assertThat(papelDe(secretarioNaPastoral)).isEqualTo("membro-ativo");
            assertThat(papelDe(membroNaPastoral)).isEqualTo("membro-ativo");
            return null;
        });
    }

    private String papelDe(UUID vinculo) {
        return jdbc.queryForObject("SELECT papel FROM calendario.membro_grupo WHERE id = ?", String.class, vinculo);
    }
}
