package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fase de contração do <em>expand &amp; contract</em> da cifra: regrava em AES-GCM ({@code v1:}) os atributos
 * pessoais de fiéis, dependentes, clérigos e solicitações ao DPO que ainda estejam no formato CBC legado.
 * O hibernate só regrava um atributo quando ele muda, então sem este passo o legado permaneceria
 * indefinidamente. É idempotente (valores já em GCM, nulos ou em claro são ignorados) e roda na subida da
 * aplicação, quando a chave já foi configurada. A trilha de consentimento fica de fora: é append-only e foi
 * cifrada diretamente em GCM pela V041. Nenhum valor pessoal é logado.
 */
@Component
public class RecifragemCbcParaGcm implements ApplicationRunner {

    private static final Logger LOG = LogManager.getLogger(RecifragemCbcParaGcm.class);
    private static final String TAB_FIEIS = "fieis";
    private static final String TAB_CLERIGOS = "clerigos";
    private static final String COL_NASCIMENTO = "data_nascimento";

    private static final Map<String, List<String>> COLUNAS = Map.of(
            TAB_FIEIS, List.of("nome", "cpf", "rg", "documento_estrangeiro_numero", COL_NASCIMENTO, "email",
                    "celular", "telefone_fixo", "endereco_logradouro", "endereco_numero", "endereco_complemento",
                    "endereco_bairro", "endereco_cep", "endereco_cidade", "responsavel_menor_nome",
                    "responsavel_menor_cpf"),
            "fiel_dependentes", List.of("nome", COL_NASCIMENTO),
            TAB_CLERIGOS, List.of("nome_civil", COL_NASCIMENTO, "cpf", "rg", "documento_estrangeiro", "email",
                    "celular", "telefone_fixo", "endereco", "endereco_cep", "endereco_logradouro",
                    "endereco_numero", "endereco_complemento", "endereco_bairro", "endereco_cidade"),
            "solicitacoes_dpo", List.of("titular_nome", "titular_email", "titular_cpf"));

    private final JdbcTemplate jdbc;

    public RecifragemCbcParaGcm(JdbcTemplate jdbc, CryptoInitializer inicializador) {
        this.jdbc = jdbc;
        if (inicializador == null) {
            throw new IllegalArgumentException("Inicializador criptográfico é obrigatório.");
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        int regravados = recifrar();
        if (regravados > 0) {
            LOG.info("Recifragem CBC para GCM concluida: {} valores regravados.", regravados);
        }
    }

    /**
     * Regrava em GCM todo valor ainda no formato CBC legado.
     *
     * @return quantidade de valores regravados (zero quando não há legado)
     */
    public int recifrar() {
        int total = 0;
        for (Map.Entry<String, List<String>> tabela : COLUNAS.entrySet()) {
            for (String coluna : tabela.getValue()) {
                total += recifrarColuna(tabela.getKey(), coluna);
            }
        }
        return total;
    }

    private int recifrarColuna(String tabela, String coluna) {
        List<Map<String, Object>> linhas = jdbc.queryForList(
                "SELECT id, " + coluna + " AS valor FROM calendario." + tabela + " WHERE " + coluna
                        + " IS NOT NULL AND " + coluna + " NOT LIKE 'v1:%'");
        int regravados = 0;
        for (Map<String, Object> linha : linhas) {
            String valor = (String) linha.get("valor");
            if (CryptoUtils.pareceCifrado(valor)) {
                jdbc.update("UPDATE calendario." + tabela + " SET " + coluna + " = ? WHERE id = ?",
                        CryptoUtils.encrypt(CryptoUtils.decrypt(valor)), (UUID) linha.get("id"));
                regravados++;
            }
        }
        return regravados;
    }
}
