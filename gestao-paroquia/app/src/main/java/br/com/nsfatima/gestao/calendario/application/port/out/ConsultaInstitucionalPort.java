package br.com.nsfatima.gestao.calendario.application.port.out;

/**
 * Porta de saída para consulta dos dados institucionais canônicos e civis da paróquia (Igreja Matriz e Diocese).
 * Isola o contexto de calendário e exportação da persistência interna dos bounded contexts de organização e clero.
 */
public interface ConsultaInstitucionalPort {

    /**
     * Snapshot imutável contendo os dados canônicos e civis consolidados da paróquia.
     *
     * @param nome nome oficial da paróquia (obtido da Unidade Territorial MATRIZ)
     * @param diocese nome da diocese canônica vinculada (obtido do Governo Eclesiástico)
     * @param endereco endereço formatado completo da Matriz
     * @param cnpj CNPJ da paróquia
     * @param telefone telefone de contato
     * @param email e-mail institucional
     */
    record DadosInstitucionais(
            String nome,
            String diocese,
            String endereco,
            String cnpj,
            String telefone,
            String email
    ) {
        public static DadosInstitucionais padrao() {
            return new DadosInstitucionais(
                    "Paróquia Nossa Senhora de Fátima",
                    "Diocese de São Miguel Paulista",
                    "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP",
                    null,
                    null,
                    null
            );
        }
    }

    /**
     * Recupera os dados institucionais consolidados da paróquia.
     *
     * @return snapshot com dados da Matriz e Diocese
     */
    DadosInstitucionais obterDadosInstitucionais();
}
