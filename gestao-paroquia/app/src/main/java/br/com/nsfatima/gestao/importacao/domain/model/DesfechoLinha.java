package br.com.nsfatima.gestao.importacao.domain.model;

/**
 * Destino de uma linha de importação que passou por todas as regras do contexto de destino. Distingue o
 * registro gravado do que entrou na esteira de aprovação do calendário (por conflito de agenda ou papel do
 * solicitante), para que o relatório não apresente como "importado" algo que ainda depende de decisão.
 */
public enum DesfechoLinha {
    /** O registro foi gravado no contexto de destino. */
    IMPORTADA,
    /** O registro virou solicitação de aprovação e só será gravado se for aprovado. */
    AGUARDANDO_APROVACAO
}
