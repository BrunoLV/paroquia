package br.com.nsfatima.gestao.governanca.domain.policy;

/** Define quem pode alterar a governança anual da paróquia sem conhecer a segurança da infraestrutura. */
@FunctionalInterface
public interface AutorizacaoAnoParoquialPolicy {

    void assertPodeGerir(String rawRole, String rawTipoGrupo);
}
