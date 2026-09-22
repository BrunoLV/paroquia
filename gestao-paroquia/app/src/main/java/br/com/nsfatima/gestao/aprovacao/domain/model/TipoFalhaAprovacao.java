package br.com.nsfatima.gestao.aprovacao.domain.model;

/**
 * Classificação da natureza da falha de execução de uma solicitação de aprovação (quando o status
 * transiciona para {@link AprovacaoStatus#FALHA_EXECUCAO}).
 *
 * <ul>
 *   <li>{@link #NEGOCIO_CADASTRAL}: Falhas decorrentes de regras e invariantes de negócio ou inconsistências
 *   cadastrais irremediáveis na mesma solicitação (por exemplo: conflito de espaço físico ou ativo patrimonial
 *   já alocado para outro evento aprovado, data de início já expirada no momento da deliberação). Solicitações com
 *   este tipo de falha são estritamente imutáveis e não admitem reexecução direta; o usuário deve realizar um novo
 *   cadastro com parâmetros corrigidos.</li>
 *
 *   <li>{@link #SISTEMICA_APLICACAO}: Falhas técnicas inesperadas de infraestrutura ou aplicação (ex.: indisponibilidade
 *   momentânea de rede/banco, timeout de serviço, falha transitória de integração). Solicitações com este tipo de falha
 *   admitem reexecução formal para restaurar o estado desejado sem necessidade de refazer o cadastro pelo solicitante.</li>
 * </ul>
 */
public enum TipoFalhaAprovacao {
    NEGOCIO_CADASTRAL,
    SISTEMICA_APLICACAO
}
