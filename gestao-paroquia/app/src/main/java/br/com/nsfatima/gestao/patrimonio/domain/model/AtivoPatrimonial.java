package br.com.nsfatima.gestao.patrimonio.domain.model;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz do bem patrimonial móvel da paróquia.
 *
 * <p>Modela a imutabilidade estrutural exigida pela especificação: após o cadastro, apenas a
 * {@code descricao} pode ser alterada; nome, código de tombamento, datas, valor, grupo proprietário
 * e a elegibilidade a reserva são fixos. A posse do bem também é fixa — não há transferência entre
 * comunidades.</p>
 *
 * <p>O {@link AtivoStatus} tem uma máquina de estados simples: {@code EM_MANUTENCAO} tem precedência
 * sobre {@code RESERVADO}; a sincronização de {@code RESERVADO}/{@code DISPONIVEL} a partir das
 * reservas é feita fora do agregado (evento/job) via {@link #marcarComoReservado(Instant)} /
 * {@link #marcarComoDisponivel(Instant)}, que são no-ops quando o bem está em manutenção ou
 * excluído.</p>
 */
public class AtivoPatrimonial implements Serializable {

    private final AtivoPatrimonialId id;
    private final String nome;
    private final CodigoTombamento codigoTombamento;
    private String descricao;
    private final AtivoCategoria categoria;
    private final LocalDate dataAquisicao;
    private final BigDecimal valorAquisicao;
    private final UUID grupoParoquialId;
    private final boolean reservavel;
    private AtivoStatus status;
    private final UUID cadastradoPorId;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private long version;

    public AtivoPatrimonial(
            AtivoPatrimonialId id,
            String nome,
            CodigoTombamento codigoTombamento,
            String descricao,
            AtivoCategoria categoria,
            LocalDate dataAquisicao,
            BigDecimal valorAquisicao,
            UUID grupoParoquialId,
            boolean reservavel,
            AtivoStatus status,
            UUID cadastradoPorId,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id, "O identificador do ativo é obrigatório.");
        this.nome = exigirTextoNaoVazio(nome, "O nome do ativo é obrigatório.");
        this.codigoTombamento = Objects.requireNonNull(codigoTombamento, "O código de tombamento é obrigatório.");
        this.descricao = descricao != null ? descricao.trim() : null;
        this.categoria = Objects.requireNonNull(categoria, "A categoria (tipo) do ativo é obrigatória.");
        this.dataAquisicao = Objects.requireNonNull(dataAquisicao, "A data de aquisição é obrigatória.");
        this.valorAquisicao = exigirValorNaoNegativo(valorAquisicao);
        this.grupoParoquialId = Objects.requireNonNull(grupoParoquialId,
                "O grupo paroquial proprietário é obrigatório.");
        this.reservavel = reservavel;
        this.status = Objects.requireNonNull(status, "O status do ativo é obrigatório.");
        this.cadastradoPorId = cadastradoPorId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    /**
     * Fábrica de cadastro de um novo bem. O status inicial é sempre {@link AtivoStatus#DISPONIVEL} e
     * o código de tombamento já vem gerado pelo sistema.
     */
    public static AtivoPatrimonial cadastrar(
            AtivoPatrimonialId id,
            String nome,
            CodigoTombamento codigoTombamento,
            String descricao,
            AtivoCategoria categoria,
            LocalDate dataAquisicao,
            BigDecimal valorAquisicao,
            UUID grupoParoquialId,
            boolean reservavel,
            UUID cadastradoPorId,
            Instant agora
    ) {
        Objects.requireNonNull(cadastradoPorId, "O autor do cadastro é obrigatório.");
        return new AtivoPatrimonial(
                id, nome, codigoTombamento, descricao, categoria, dataAquisicao, valorAquisicao,
                grupoParoquialId, reservavel, AtivoStatus.DISPONIVEL, cadastradoPorId,
                agora, agora, null, 0L);
    }

    /** Único mutador de conteúdo permitido após o cadastro (imutabilidade estrutural). */
    public void atualizarDescricao(String novaDescricao, Instant agora) {
        garantirNaoExcluido();
        this.descricao = novaDescricao != null ? novaDescricao.trim() : null;
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    /**
     * Reflete no bem a existência de uma reserva ativa. Ignorado quando o bem está em manutenção
     * (precedência) ou excluído.
     */
    public void marcarComoReservado(Instant agora) {
        if (deletedAt != null || status == AtivoStatus.EM_MANUTENCAO) {
            return;
        }
        if (status != AtivoStatus.RESERVADO) {
            this.status = AtivoStatus.RESERVADO;
            this.updatedAt = agora != null ? agora : Instant.now();
        }
    }

    /** Reflete no bem a ausência de reserva ativa. Ignorado em manutenção ou exclusão. */
    public void marcarComoDisponivel(Instant agora) {
        if (deletedAt != null || status == AtivoStatus.EM_MANUTENCAO) {
            return;
        }
        if (status != AtivoStatus.DISPONIVEL) {
            this.status = AtivoStatus.DISPONIVEL;
            this.updatedAt = agora != null ? agora : Instant.now();
        }
    }

    public void enviarParaManutencao(Instant agora) {
        garantirNaoExcluido();
        if (status != AtivoStatus.DISPONIVEL) {
            throw new AtivoManutencaoInvalidaException(
                    "Só é possível enviar para manutenção um ativo DISPONIVEL. Status atual: " + status);
        }
        this.status = AtivoStatus.EM_MANUTENCAO;
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    public void concluirManutencao(Instant agora) {
        if (status != AtivoStatus.EM_MANUTENCAO) {
            throw new AtivoManutencaoInvalidaException(
                    "Não há manutenção em andamento para concluir. Status atual: " + status);
        }
        this.status = AtivoStatus.DISPONIVEL;
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    /** Exclusão lógica. As guardas de reserva futura / manutenção aberta são do caso de uso. */
    public void excluir(Instant agora) {
        garantirNaoExcluido();
        this.deletedAt = agora != null ? agora : Instant.now();
        this.updatedAt = this.deletedAt;
    }

    public boolean estaExcluido() {
        return deletedAt != null;
    }

    private void garantirNaoExcluido() {
        if (deletedAt != null) {
            throw new IllegalStateException("Operação inválida sobre um ativo excluído: " + id);
        }
    }

    private static String exigirTextoNaoVazio(String valor, String mensagem) {
        Objects.requireNonNull(valor, mensagem);
        String limpo = valor.trim();
        if (limpo.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return limpo;
    }

    private static BigDecimal exigirValorNaoNegativo(BigDecimal valor) {
        Objects.requireNonNull(valor, "O valor de aquisição é obrigatório.");
        if (valor.signum() < 0) {
            throw new IllegalArgumentException("O valor de aquisição não pode ser negativo.");
        }
        return valor;
    }

    public AtivoPatrimonialId getId() {
        return id;
    }
    public String getNome() {
        return nome;
    }
    public CodigoTombamento getCodigoTombamento() {
        return codigoTombamento;
    }
    public String getDescricao() {
        return descricao;
    }
    public AtivoCategoria getCategoria() {
        return categoria;
    }
    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }
    public BigDecimal getValorAquisicao() {
        return valorAquisicao;
    }
    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }
    public boolean isReservavel() {
        return reservavel;
    }
    public AtivoStatus getStatus() {
        return status;
    }
    public UUID getCadastradoPorId() {
        return cadastradoPorId;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    public Instant getDeletedAt() {
        return deletedAt;
    }
    public long getVersion() {
        return version;
    }
}
