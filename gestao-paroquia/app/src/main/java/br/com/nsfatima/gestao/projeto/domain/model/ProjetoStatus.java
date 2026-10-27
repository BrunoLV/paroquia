package br.com.nsfatima.gestao.projeto.domain.model;

import br.com.nsfatima.gestao.support.domain.EnumRequestNormalizer;

/**
 * Ciclo de vida de um {@code Projeto} paroquial.
 *
 * <p>A transição de estados segue estritamente
 * {@code ABERTO -> EM_ANDAMENTO -> CONCLUIDO}, com {@code CANCELADO} podendo ocorrer a partir de
 * {@code ABERTO} ou {@code EM_ANDAMENTO}. A transição {@code ABERTO -> EM_ANDAMENTO} também pode
 * ocorrer automaticamente via {@code ProjetoAutoActivationScheduler} quando a data de início é
 * atingida. Projetos {@code CANCELADO} ou {@code CONCLUIDO} são estados terminais: não podem
 * receber novos eventos vinculados (ver {@code ProjetoVincularPolicy}) nem sofrer nova transição.
 */
public enum ProjetoStatus {
    ABERTO,
    EM_ANDAMENTO,
    CANCELADO,
    CONCLUIDO;

    /**
     * Normaliza um valor bruto (possivelmente com variações de caixa/acentuação) recebido via
     * JSON para o valor correspondente do enum, usado como ponto de entrada de desserialização
     * do Jackson.
     *
     * @param rawValue valor textual recebido na requisição.
     * @return o {@link ProjetoStatus} normalizado correspondente.
     */
    public static ProjetoStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, ProjetoStatus.class);
    }
}
