package br.com.nsfatima.gestao.governanca.integration;

import br.com.nsfatima.gestao.governanca.api.v1.dto.CriarSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ManifestarConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitacaoDpoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.application.service.ServicoEmailNotificacaoConsentimento;
import br.com.nsfatima.gestao.governanca.application.usecase.GerenciarConsentimentoPublicoUseCase;
import br.com.nsfatima.gestao.governanca.application.usecase.GerenciarSolicitacoesDpoUseCase;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.DesafioConsentimentoEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.DesafioConsentimentoJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import br.com.nsfatima.gestao.support.infrastructure.crypto.CpfBlindIndex;
import br.com.nsfatima.gestao.support.infrastructure.crypto.CryptoUtils;
import br.com.nsfatima.gestao.support.infrastructure.crypto.MigracaoCifraPiiLegadaLgpd;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cobre, contra PostgreSQL real, o que mocks não provam: a cifra efetiva das colunas do DPO, a trilha
 * append-only por gatilho, a migração de dados V041, a persistência do contador de tentativas apesar da
 * exceção, o limite de pedidos de código e o token de uso único.
 */
@SpringBootTest
@DisplayName("LGPD: consentimento, DPO e trilha, contra PostgreSQL")
class LgpdConsentimentoIntegrationTest {

    private static final String CPF_DPO = "52998224725";
    private static final String CODIGO = "123456";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private GerenciarSolicitacoesDpoUseCase dpoUseCase;
    @Autowired private GerenciarConsentimentoPublicoUseCase consentimentoUseCase;
    @Autowired private DesafioConsentimentoJpaRepository desafioRepository;
    @Autowired private HistoricoConsentimentoJpaRepository historicoRepository;
    @Autowired private ServicoEmailNotificacaoConsentimento emailService;
    @Autowired private MigracaoCifraPiiLegadaLgpd migracao;

    private static String novoCpf() {
        return String.format("%011d", Math.abs(UUID.randomUUID().getMostSignificantBits()) % 100_000_000_000L);
    }

    private UUID novoFiel(String cpf) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.fieis (id, nome, cpf, cpf_hash, data_nascimento, email, ativo, "
                + "criado_em_utc, atualizado_em_utc, criado_por, atualizado_por) VALUES (?, 'Maria Silva', ?, ?, "
                + "'1990-01-01', 'maria@exemplo.com', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 't', 't')",
                id, CryptoUtils.encrypt(cpf), CpfBlindIndex.hash(cpf));
        return id;
    }

    private DesafioConsentimentoEntity novoDesafio(UUID fielId) {
        DesafioConsentimentoEntity desafio = new DesafioConsentimentoEntity();
        desafio.setId(UUID.randomUUID());
        desafio.setTitularTipo(TipoTitularConsentimento.FIEL);
        desafio.setTitularId(fielId);
        desafio.setCodigoHash(emailService.hashCodigo(CODIGO));
        desafio.setExpiraEmUtc(Instant.now().plusSeconds(300));
        return desafioRepository.save(desafio);
    }

    private UUID novoHistorico(UUID titularId) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.historico_consentimentos (id, titular_tipo, titular_id, tipo_evento, "
                + "termo_versao, termo_hash, termo_aceito, ip_origem, responsavel_menor_cpf) "
                + "VALUES (?, 'FIEL', ?, 'CONCESSAO', 'v1.0', 'h', TRUE, ?, ?)",
                id, titularId, CryptoUtils.encrypt("10.0.0.1"), CryptoUtils.encrypt("52998224725"));
        return id;
    }

    @Test
    @DisplayName("PII da solicitacao ao DPO fica cifrada no banco e a consulta publica a devolve mascarada")
    void solicitacaoDpoCifradaEMascarada() {
        SolicitacaoDpoResponse aberta = dpoUseCase.criarSolicitacao(new CriarSolicitacaoDpoRequest(
                "Maria Silva", "maria@exemplo.com", CPF_DPO, "ACESSO", "Quero acessar meus dados."));

        String cpf = jdbc.queryForObject(
                "SELECT titular_cpf FROM calendario.solicitacoes_dpo WHERE protocolo = ?", String.class,
                aberta.protocolo());
        String email = jdbc.queryForObject(
                "SELECT titular_email FROM calendario.solicitacoes_dpo WHERE protocolo = ?", String.class,
                aberta.protocolo());
        String nome = jdbc.queryForObject(
                "SELECT titular_nome FROM calendario.solicitacoes_dpo WHERE protocolo = ?", String.class,
                aberta.protocolo());

        assertThat(cpf).startsWith("v1:").doesNotContain(CPF_DPO);
        assertThat(email).startsWith("v1:");
        assertThat(nome).startsWith("v1:");
        SolicitacaoDpoResponse publica = dpoUseCase.obterPorProtocolo(aberta.protocolo());
        assertThat(publica.titularCpf()).isEqualTo("***.***.***-25");
        assertThat(publica.titularEmail()).isEqualTo("m***a@exemplo.com");
        assertThat(dpoUseCase.listarTodas()).anySatisfy(s -> {
            assertThat(s.protocolo()).isEqualTo(aberta.protocolo());
            assertThat(s.titularCpf()).isEqualTo(CPF_DPO);
        });
    }

    @Test
    @DisplayName("Trilha de consentimento e append-only: so a anonimizacao de PII e aceita")
    void trilhaAppendOnly() {
        UUID titular = UUID.randomUUID();
        UUID evento = novoHistorico(titular);

        assertThatThrownBy(() -> jdbc.update(
                "UPDATE calendario.historico_consentimentos SET termo_aceito = FALSE WHERE id = ?", evento))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update(
                "UPDATE calendario.historico_consentimentos SET ip_origem = 'outro' WHERE id = ?", evento))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update(
                "DELETE FROM calendario.historico_consentimentos WHERE id = ?", evento))
                .isInstanceOf(DataAccessException.class);

        int anonimizados = new TransactionTemplate(transactionManager).execute(status ->
                historicoRepository.anonimizarDadosPessoais("FIEL", titular));

        assertThat(anonimizados).isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT ip_origem IS NULL AND responsavel_menor_cpf IS NULL AND termo_aceito "
                        + "FROM calendario.historico_consentimentos WHERE id = ?", Boolean.class, evento)).isTrue();
    }

    @Test
    @DisplayName("Migracao V041 cifra PII legada em claro e troca o SHA-256 puro pelo HMAC do CPF")
    void migracaoCifraEReindexa() throws Exception {
        UUID fiel = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.fieis (id, nome, cpf, cpf_hash, ativo, criado_em_utc, "
                + "atualizado_em_utc, criado_por, atualizado_por) VALUES (?, 'Legado', ?, 'sha256-legado', TRUE, "
                + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 't', 't')", fiel, CryptoUtils.encrypt("11144477735"));
        UUID clerigo = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.clerigos (id, nome_civil, nome_eclesiastico, data_nascimento, "
                + "pais_origem, cpf, email, celular, grau_ordem, diocese_incardinacao, criado_em_utc, "
                + "atualizado_em_utc, criado_por, atualizado_por) VALUES (?, 'x', 'Pe. X', '1970-01-01', 'BRA', ?, "
                + "'x@x.com', '11999990000', 'PRESBITERO', 'Dioc', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 't', 't')",
                clerigo, CryptoUtils.encrypt("52998224725"));
        String protocolo = "LEGADO-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update("INSERT INTO calendario.solicitacoes_dpo (id, protocolo, titular_nome, titular_email, "
                + "titular_cpf, tipo_solicitacao, mensagem, prazo_resposta_utc) VALUES (?, ?, 'Joao Claro', "
                + "'joao@claro.com', '12345678909', 'ACESSO', 'm', CURRENT_TIMESTAMP)", UUID.randomUUID(), protocolo);
        UUID titular = UUID.randomUUID();
        UUID evento = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.historico_consentimentos (id, titular_tipo, titular_id, tipo_evento, "
                + "termo_versao, termo_hash, termo_aceito, ip_origem) VALUES (?, 'FIEL', ?, 'CONCESSAO', 'v1.0', "
                + "'h', TRUE, '200.1.2.3')", evento, titular);

        executarMigracaoSemGatilho();

        assertThat(jdbc.queryForObject("SELECT cpf_hash FROM calendario.fieis WHERE id = ?", String.class, fiel))
                .isEqualTo(CpfBlindIndex.hash("11144477735"));
        assertThat(jdbc.queryForObject("SELECT cpf_hash FROM calendario.clerigos WHERE id = ?", String.class,
                clerigo)).isEqualTo(CpfBlindIndex.hash("52998224725"));
        assertThat(jdbc.queryForObject("SELECT titular_cpf FROM calendario.solicitacoes_dpo WHERE protocolo = ?",
                String.class, protocolo)).startsWith("v1:");
        assertThat(CryptoUtils.decrypt(jdbc.queryForObject(
                "SELECT titular_nome FROM calendario.solicitacoes_dpo WHERE protocolo = ?", String.class,
                protocolo))).isEqualTo("Joao Claro");
        assertThat(jdbc.queryForObject("SELECT ip_origem FROM calendario.historico_consentimentos WHERE id = ?",
                String.class, evento)).startsWith("v1:");
    }

    private void executarMigracaoSemGatilho() throws Exception {
        jdbc.execute("ALTER TABLE calendario.historico_consentimentos "
                + "DISABLE TRIGGER trg_historico_consentimento_append_only");
        try {
            jdbc.execute((Connection conexao) -> {
                migracao.executar(conexao);
                return null;
            });
        } finally {
            jdbc.execute("ALTER TABLE calendario.historico_consentimentos "
                    + "ENABLE TRIGGER trg_historico_consentimento_append_only");
        }
    }

    @Test
    @DisplayName("Tentativas incorretas persistem mesmo com a excecao e o codigo correto passa a ser recusado")
    void tentativasPersistemApesarDaExcecao() {
        String cpf = novoCpf();
        UUID fiel = novoFiel(cpf);
        DesafioConsentimentoEntity desafio = novoDesafio(fiel);
        ValidarCodigoConsentimentoRequest errado = new ValidarCodigoConsentimentoRequest(cpf, "000000");

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> consentimentoUseCase.validarCodigo(errado))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        assertThat(desafioRepository.findById(desafio.getId()).orElseThrow().getTentativas()).isEqualTo(3);
        assertThatThrownBy(() -> consentimentoUseCase.validarCodigo(
                new ValidarCodigoConsentimentoRequest(cpf, CODIGO))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("No maximo 3 codigos por titular na janela; excedentes nao geram desafio")
    void limiteDePedidosDeCodigo() {
        String cpf = novoCpf();
        UUID fiel = novoFiel(cpf);

        for (int i = 0; i < 5; i++) {
            consentimentoUseCase.solicitarCodigo(new SolicitarCodigoConsentimentoRequest(cpf));
        }

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM calendario.desafios_consentimento WHERE titular_id = ?",
                Integer.class, fiel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Token de consentimento e de uso unico: a segunda manifestacao e recusada")
    void tokenDeUsoUnico() {
        String cpf = novoCpf();
        UUID fiel = novoFiel(cpf);
        novoDesafio(fiel);
        ValidarCodigoConsentimentoResponse validado = consentimentoUseCase.validarCodigo(
                new ValidarCodigoConsentimentoRequest(cpf, CODIGO));
        ManifestarConsentimentoRequest manifestacao = new ManifestarConsentimentoRequest(true, true, true, null, null);

        consentimentoUseCase.manifestar(validado.token(), manifestacao, "10.0.0.9", "UA");

        assertThatThrownBy(() -> consentimentoUseCase.manifestar(validado.token(), manifestacao, "10.0.0.9", "UA"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já utilizado");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM calendario.historico_consentimentos "
                + "WHERE titular_id = ?", Integer.class, fiel)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT ip_origem FROM calendario.historico_consentimentos "
                + "WHERE titular_id = ?", String.class, fiel)).startsWith("v1:");
    }
}
