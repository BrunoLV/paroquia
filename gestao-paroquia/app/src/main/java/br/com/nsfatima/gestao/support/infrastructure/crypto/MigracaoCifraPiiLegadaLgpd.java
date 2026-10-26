package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Migração de dados V041 (fase de dados do <em>expand &amp; contract</em> da adequação LGPD). Roda entre
 * V040 (alarga colunas e cria {@code clerigos.cpf_hash}) e V042 (trilha append-only), e precisa ser Java
 * porque exige a chave de cifragem e o pepper da aplicação, que o SQL puro não possui.
 *
 * <p>Faz, de forma idempotente: (1) cifra em AES-GCM os dados pessoais que antes ficavam em claro nas
 * solicitações ao DPO e na trilha de consentimento — valores que já têm forma de ciphertext são ignorados;
 * (2) recalcula o blind index de CPF de fiéis, trocando o SHA-256 puro por HMAC-SHA256 com pepper; e
 * (3) preenche o blind index de clérigos (paridade clérigo/fiel). Depende de {@link CryptoInitializer}
 * apenas para garantir que chave e pepper estejam configurados antes de o Flyway executar o script.
 * Nenhum valor pessoal é logado nem incluído em mensagens de erro.
 */
@Component
public class MigracaoCifraPiiLegadaLgpd implements JavaMigration {

    private static final String VERSAO = "041";
    private static final int CHECKSUM = 41;
    private static final int LOTE = 500;
    private static final String TAB_DPO = "solicitacoes_dpo";
    private static final String TAB_HISTORICO = "historico_consentimentos";

    private static final List<String[]> COLUNAS_A_CIFRAR = List.of(
            new String[] {TAB_DPO, "titular_nome"},
            new String[] {TAB_DPO, "titular_email"},
            new String[] {TAB_DPO, "titular_cpf"},
            new String[] {TAB_HISTORICO, "ip_origem"},
            new String[] {TAB_HISTORICO, "responsavel_menor_nome"},
            new String[] {TAB_HISTORICO, "responsavel_menor_cpf"});

    /**
     * Construtor com dependência explícita do inicializador criptográfico, que garante a ordem de criação
     * dos beans (chave e pepper prontos antes da execução do Flyway).
     *
     * @param inicializador inicializador de chave e pepper
     */
    public MigracaoCifraPiiLegadaLgpd(CryptoInitializer inicializador) {
        if (inicializador == null) {
            throw new IllegalArgumentException("Inicializador criptográfico é obrigatório.");
        }
    }

    @Override
    public MigrationVersion getVersion() {
        return MigrationVersion.fromVersion(VERSAO);
    }

    @Override
    public String getDescription() {
        return "lgpd cifrar pii legada e rehash cpf";
    }

    @Override
    public Integer getChecksum() {
        return CHECKSUM;
    }

    @Override
    public boolean canExecuteInTransaction() {
        return true;
    }

    @Override
    public void migrate(Context context) throws SQLException {
        executar(context.getConnection());
    }

    /**
     * Executa a migração sobre a conexão informada (exposto para teste de integração com Testcontainers).
     *
     * @param conexao conexão JDBC com o schema {@code calendario} migrado até V040
     * @throws SQLException se o acesso ao banco falhar
     */
    public void executar(Connection conexao) throws SQLException {
        for (String[] alvo : COLUNAS_A_CIFRAR) {
            cifrarColuna(conexao, alvo[0], alvo[1]);
        }
        recalcularIndiceCpf(conexao, "fieis");
        recalcularIndiceCpf(conexao, "clerigos");
    }

    private void cifrarColuna(Connection conexao, String tabela, String coluna) throws SQLException {
        List<Object[]> pendentes = new ArrayList<>();
        String consulta = "SELECT id, " + coluna + " FROM calendario." + tabela + " WHERE " + coluna + " IS NOT NULL";
        try (PreparedStatement ps = conexao.prepareStatement(consulta); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String valor = rs.getString(2);
                if (!valor.isBlank() && !CryptoUtils.pareceCifrado(valor)) {
                    pendentes.add(new Object[] {rs.getObject(1, UUID.class), CryptoUtils.encrypt(valor)});
                }
            }
        }
        gravar(conexao, "UPDATE calendario." + tabela + " SET " + coluna + " = ? WHERE id = ?", pendentes);
    }

    private void recalcularIndiceCpf(Connection conexao, String tabela) throws SQLException {
        List<Object[]> pendentes = new ArrayList<>();
        String consulta = "SELECT id, cpf, cpf_hash FROM calendario." + tabela + " WHERE cpf IS NOT NULL";
        try (PreparedStatement ps = conexao.prepareStatement(consulta); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String cpf = rs.getString(2);
                String claro = CryptoUtils.pareceCifrado(cpf) ? CryptoUtils.decrypt(cpf) : cpf;
                String indice = CpfBlindIndex.hashDeCpf(claro);
                if (!indice.equals(rs.getString(3))) {
                    pendentes.add(new Object[] {rs.getObject(1, UUID.class), indice});
                }
            }
        }
        gravar(conexao, "UPDATE calendario." + tabela + " SET cpf_hash = ? WHERE id = ?", pendentes);
    }

    private void gravar(Connection conexao, String comando, List<Object[]> linhas) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(comando)) {
            int emLote = 0;
            for (Object[] linha : linhas) {
                ps.setString(1, (String) linha[1]);
                ps.setObject(2, linha[0]);
                ps.addBatch();
                if (++emLote % LOTE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
    }
}
