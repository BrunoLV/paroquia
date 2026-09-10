package br.com.nsfatima.gestao.patrimonio.domain.model;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz de uma manutenção de ativo patrimonial: um período de indisponibilidade técnica
 * (conserto, revisão, reparo). Associa-se ao ativo apenas por {@link AtivoPatrimonialId}. Enquanto
 * {@code dataFimReal} é nula a manutenção está em aberto e bloqueia o calendário de reservas.
 */
public class AtivoManutencao implements Serializable {

    private final AtivoManutencaoId id;
    private final AtivoPatrimonialId ativoId;
    private final String descricaoReparo;
    private final PeriodoManutencao periodo;
    private Instant dataFimReal;
    private final UUID registradoPorId;
    private final Instant createdAt;
    private Instant updatedAt;
    private long version;

    public AtivoManutencao(
            AtivoManutencaoId id,
            AtivoPatrimonialId ativoId,
            String descricaoReparo,
            PeriodoManutencao periodo,
            Instant dataFimReal,
            UUID registradoPorId,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id, "O identificador da manutenção é obrigatório.");
        this.ativoId = Objects.requireNonNull(ativoId, "O ativo da manutenção é obrigatório.");
        this.descricaoReparo = exigirDescricao(descricaoReparo);
        this.periodo = Objects.requireNonNull(periodo, "O período da manutenção é obrigatório.");
        this.dataFimReal = dataFimReal;
        this.registradoPorId = Objects.requireNonNull(registradoPorId, "O autor do registro é obrigatório.");
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.version = version;
    }

    public static AtivoManutencao registrar(
            AtivoManutencaoId id,
            AtivoPatrimonialId ativoId,
            String descricaoReparo,
            PeriodoManutencao periodo,
            UUID registradoPorId,
            Instant agora
    ) {
        return new AtivoManutencao(id, ativoId, descricaoReparo, periodo, null, registradoPorId, agora, agora, 0L);
    }

    /** Registro efetivo da conclusão do reparo. */
    public void concluir(Instant dataFimReal) {
        Objects.requireNonNull(dataFimReal, "A data de conclusão real é obrigatória.");
        if (!emAberto()) {
            throw new AtivoManutencaoInvalidaException("Esta manutenção já foi concluída em " + this.dataFimReal);
        }
        if (dataFimReal.isBefore(periodo.inicio())) {
            throw new AtivoManutencaoInvalidaException("A conclusão não pode ser anterior ao início da manutenção.");
        }
        this.dataFimReal = dataFimReal;
        this.updatedAt = dataFimReal;
    }

    public boolean emAberto() {
        return dataFimReal == null;
    }

    private static String exigirDescricao(String valor) {
        Objects.requireNonNull(valor, "A descrição do reparo é obrigatória.");
        String limpo = valor.trim();
        if (limpo.isBlank()) {
            throw new IllegalArgumentException("A descrição do reparo não pode ser vazia.");
        }
        return limpo;
    }

    public AtivoManutencaoId getId() {
        return id;
    }
    public AtivoPatrimonialId getAtivoId() {
        return ativoId;
    }
    public String getDescricaoReparo() {
        return descricaoReparo;
    }
    public PeriodoManutencao getPeriodo() {
        return periodo;
    }
    public Instant getDataFimReal() {
        return dataFimReal;
    }
    public UUID getRegistradoPorId() {
        return registradoPorId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public long getVersion() {
        return version;
    }
}
