package br.com.nsfatima.gestao.governanca.domain.model;

import br.com.nsfatima.gestao.support.domain.EnumRequestNormalizer;

/**
 * Estados do ciclo de vida de governança de um Ano Paroquial, que gatilha o quanto o calendário anual pode ainda
 * ser editado.
 * <p>
 * Transições válidas (aplicadas em {@code AnoParoquialController#update} e {@code AnoParoquialRolloverService}):
 * <ul>
 *     <li>{@code EM_PLANEJAMENTO -> FECHADO}: apenas dentro do próprio ano civil, e apenas se todos os anos
 *     anteriores já estiverem {@code CONCLUIDO}.</li>
 *     <li>{@code EM_PLANEJAMENTO -> CONCLUIDO} ou {@code FECHADO -> CONCLUIDO}: mesma regra de sucessão (anos
 *     anteriores concluídos); dispara o rollover automático que provisiona o próximo ano.</li>
 *     <li>{@code CONCLUIDO} é terminal e imutável — nenhuma transição de saída é permitida.</li>
 * </ul>
 */
public enum AnoParoquialStatus {
    /** Ano aberto para planejamento: eventos/projetos ainda podem ser criados e alterados livremente. */
    EM_PLANEJAMENTO,
    /** Ano travado para novas edições, mas ainda não definitivamente encerrado. */
    FECHADO,
    /** Ano definitivamente encerrado; estado terminal e imutável que libera o rollover para o próximo ano. */
    CONCLUIDO;

    /**
     * Normaliza um valor textual (ex.: vindo de payload JSON) para o enum correspondente, tolerando variações de
     * caixa/formatação conforme {@code EnumRequestNormalizer}.
     *
     * @param rawValue valor bruto recebido na requisição
     * @return o {@link AnoParoquialStatus} correspondente
     * @throws IllegalArgumentException se o valor não corresponder a nenhum status conhecido
     */
    public static AnoParoquialStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, AnoParoquialStatus.class);
    }
}
