package br.com.nsfatima.gestao.governanca.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Agregado Raiz do Ano Paroquial.
 *
 * <p>Modela a máquina de estados de governança do calendário anual
 * ({@code EM_PLANEJAMENTO -> FECHADO -> CONCLUIDO}) e concentra as invariantes que antes
 * viviam espalhadas no {@code AnoParoquialController} e no {@code AnoParoquialRolloverService}:</p>
 *
 * <ul>
 *   <li>Um ano {@code CONCLUIDO} é terminal e imutável.</li>
 *   <li>Só o ano civil corrente pode sair de {@code EM_PLANEJAMENTO} para {@code FECHADO}/{@code CONCLUIDO}.</li>
 *   <li>{@code concluir()} é idempotente.</li>
 * </ul>
 *
 * <p>A regra de sucessão (anos anteriores precisam estar concluídos) depende do repositório e
 * mora em {@code SucessaoAnoParoquialService}; deve ser verificada antes de invocar
 * {@link #fechar(int, Instant)} / {@link #concluir(int, Instant)}.</p>
 */
public class AnoParoquial {

    private final int ano;
    private AnoParoquialStatus status;
    private Instant dataFechamento;
    private Instant atualizadoEm;

    public AnoParoquial(int ano, AnoParoquialStatus status, Instant dataFechamento, Instant atualizadoEm) {
        if (ano < 1970) {
            throw new IllegalArgumentException("Ano paroquial inválido.");
        }
        this.ano = ano;
        this.status = Objects.requireNonNull(status, "Status do ano paroquial é obrigatório.");
        this.dataFechamento = dataFechamento;
        this.atualizadoEm = atualizadoEm;
    }

    /** Abre um novo ano em planejamento. */
    public static AnoParoquial abrirPlanejamento(int ano, Instant agora) {
        return new AnoParoquial(ano, AnoParoquialStatus.EM_PLANEJAMENTO, null, agora);
    }

    /** Trava o ano para novas edições. Só o ano corrente pode sair de EM_PLANEJAMENTO. */
    public void fechar(int anoCivilCorrente, Instant agora) {
        assegurarNaoConcluido();
        assegurarAnoCorrenteSeEmPlanejamento(anoCivilCorrente);
        this.status = AnoParoquialStatus.FECHADO;
        if (this.dataFechamento == null) {
            this.dataFechamento = agora;
        }
        this.atualizadoEm = agora;
    }

    /** Reabre o planejamento de um ano fechado (ou mantém um já em planejamento). */
    public void reabrirPlanejamento(Instant agora) {
        assegurarNaoConcluido();
        this.status = AnoParoquialStatus.EM_PLANEJAMENTO;
        this.dataFechamento = null;
        this.atualizadoEm = agora;
    }

    /** Encerra definitivamente o ano. Idempotente. */
    public void concluir(int anoCivilCorrente, Instant agora) {
        if (this.status == AnoParoquialStatus.CONCLUIDO) {
            return;
        }
        assegurarAnoCorrenteSeEmPlanejamento(anoCivilCorrente);
        this.status = AnoParoquialStatus.CONCLUIDO;
        this.atualizadoEm = agora;
    }

    private void assegurarNaoConcluido() {
        if (this.status == AnoParoquialStatus.CONCLUIDO) {
            throw new IllegalArgumentException("Um ano concluído não pode ser modificado.");
        }
    }

    private void assegurarAnoCorrenteSeEmPlanejamento(int anoCivilCorrente) {
        if (this.status == AnoParoquialStatus.EM_PLANEJAMENTO && this.ano != anoCivilCorrente) {
            throw new IllegalArgumentException("Apenas o ano corrente pode ser fechado ou concluído.");
        }
    }

    public int id() {
        return ano;
    }

    public int getAno() {
        return ano;
    }

    public AnoParoquialStatus getStatus() {
        return status;
    }

    public Instant getDataFechamento() {
        return dataFechamento;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }
}
