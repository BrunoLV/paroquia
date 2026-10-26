package br.com.nsfatima.gestao.reserva.domain.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz de Reserva.
 * Modela o ciclo de vida completo de agendamento e custódia física de dependências e bens móveis,
 * garantindo atomicidade, invariantes de transição de estado e integridade de auditoria.
 */
public class Reserva implements Serializable {

    /** Antecedência do início até a qual um pedido pendente pode ser analisado (RN-RES-05). */
    public static final Duration ANTECEDENCIA_ANALISE = Duration.ofHours(48);

    private final ReservaId id;
    private final TipoItemReserva tipoItem;
    private final UUID itemId;
    private IntervaloReserva intervalo;
    private String descricao;
    private final UUID solicitanteId;
    private final UUID pastoralMovimentoId;
    private UUID eventoId;
    private UUID grupoRecorrenciaId;
    private StatusReserva status;
    private UUID aprovadorId;
    private Instant aprovadoEm;
    private String motivoCancelamento;
    private UUID canceladoPorId;
    private String checkAvarias;
    private final Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private long version;

    public Reserva(
            ReservaId id,
            TipoItemReserva tipoItem,
            UUID itemId,
            IntervaloReserva intervalo,
            String descricao,
            UUID solicitanteId,
            UUID pastoralMovimentoId,
            UUID eventoId,
            UUID grupoRecorrenciaId,
            StatusReserva status,
            UUID aprovadorId,
            Instant aprovadoEm,
            String motivoCancelamento,
            UUID canceladoPorId,
            String checkAvarias,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            long version
    ) {
        this.id = Objects.requireNonNull(id, "ID da reserva é obrigatório.");
        this.tipoItem = Objects.requireNonNull(tipoItem, "Tipo de item é obrigatório.");
        this.itemId = Objects.requireNonNull(itemId, "ID do item reservado é obrigatório.");
        this.intervalo = Objects.requireNonNull(intervalo, "Intervalo da reserva é obrigatório.");
        this.descricao = Objects.requireNonNull(descricao, "Descrição da finalidade da reserva é obrigatória.").trim();
        if (this.descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição da reserva não pode ser vazia.");
        }
        this.solicitanteId = Objects.requireNonNull(solicitanteId, "ID do solicitante é obrigatório.");
        this.pastoralMovimentoId = Objects.requireNonNull(
                pastoralMovimentoId, "ID da pastoral/movimento é obrigatório.");
        this.eventoId = eventoId;
        this.grupoRecorrenciaId = grupoRecorrenciaId;
        this.status = Objects.requireNonNull(status, "Status da reserva é obrigatório.");
        this.aprovadorId = aprovadorId;
        this.aprovadoEm = aprovadoEm;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladoPorId = canceladoPorId;
        this.checkAvarias = checkAvarias;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    /**
     * Fábrica de abertura de nova solicitação de reserva.
     */
    public static Reserva criar(
            ReservaId id,
            TipoItemReserva tipoItem,
            UUID itemId,
            IntervaloReserva intervalo,
            String descricao,
            UUID solicitanteId,
            UUID pastoralMovimentoId,
            UUID eventoId,
            UUID grupoRecorrenciaId,
            StatusReserva statusInicial,
            Instant agora
    ) {
        return new Reserva(
                id,
                tipoItem,
                itemId,
                intervalo,
                descricao,
                solicitanteId,
                pastoralMovimentoId,
                eventoId,
                grupoRecorrenciaId,
                statusInicial,
                statusInicial == StatusReserva.CONFIRMADA ? solicitanteId : null,
                statusInicial == StatusReserva.CONFIRMADA ? agora : null,
                null,
                null,
                null,
                agora,
                agora,
                null,
                0L
        );
    }

    /**
     * Deferimento de reserva pendente.
     */
    public void aprovar(UUID aprovadorId, Instant agora) {
        Objects.requireNonNull(aprovadorId, "Identificador do aprovador é obrigatório.");
        if (this.status != StatusReserva.PENDENTE) {
            throw new IllegalStateException(
                    "Apenas reservas com status PENDENTE podem ser aprovadas. Status atual: " + this.status);
        }
        this.status = StatusReserva.CONFIRMADA;
        this.aprovadorId = aprovadorId;
        this.aprovadoEm = agora != null ? agora : Instant.now();
        this.updatedAt = this.aprovadoEm;
    }

    /**
     * Rejeição de reserva pendente por coordenador ou administrador.
     */
    public void rejeitar(UUID aprovadorId, String motivo, Instant agora) {
        Objects.requireNonNull(aprovadorId, "Identificador do responsável pela rejeição é obrigatório.");
        Objects.requireNonNull(motivo, "Motivo da rejeição é obrigatório.");
        if (motivo.trim().isBlank()) {
            throw new IllegalArgumentException("O motivo da rejeição não pode ser vazio.");
        }
        if (this.status != StatusReserva.PENDENTE) {
            throw new IllegalStateException(
                    "Apenas reservas com status PENDENTE podem ser rejeitadas. Status atual: " + this.status);
        }
        this.status = StatusReserva.CANCELADA;
        this.canceladoPorId = aprovadorId;
        this.motivoCancelamento = motivo.trim();
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    /**
     * Cancelamento de reserva confirmada ou pendente. Um bem já retirado está sob custódia e só sai dela
     * pela devolução, portanto {@code RETIRADO} não é cancelável.
     * Administradores e Pároco exigem justificativa textual mínima de 15 caracteres.
     */
    public void cancelar(UUID operadorId, String motivo, boolean isAdminOuParoco, Instant agora) {
        Objects.requireNonNull(operadorId, "Identificador do operador de cancelamento é obrigatório.");
        Objects.requireNonNull(motivo, "Motivo do cancelamento é obrigatório.");
        String motivoFormatado = motivo.trim();
        if (isAdminOuParoco && motivoFormatado.length() < 15) {
            throw new IllegalArgumentException(
                    "Cancelamentos por administradores ou pároco exigem justificativa mínima de 15 caracteres.");
        }
        if (motivoFormatado.isBlank()) {
            throw new IllegalArgumentException("O motivo do cancelamento não pode ser vazio.");
        }
        if (!this.status.admiteCancelamento()) {
            throw new IllegalStateException("A reserva não pode ser cancelada no estado: " + this.status);
        }
        this.status = StatusReserva.CANCELADA;
        this.canceladoPorId = operadorId;
        this.motivoCancelamento = motivoFormatado;
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    /**
     * Registro de saída física de ativo patrimonial.
     */
    public void registrarRetirada(UUID operadorId, Instant agora) {
        Objects.requireNonNull(operadorId, "Identificador do operador de saída é obrigatório.");
        if (this.tipoItem != TipoItemReserva.ATIVO) {
            throw new IllegalStateException(
                    "Registro de saída física é aplicável exclusivamente a bens patrimoniais móveis (ATIVO).");
        }
        if (this.status != StatusReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Apenas reservas CONFIRMADAS podem ter saída registrada. Status atual: " + this.status);
        }
        this.status = StatusReserva.RETIRADO;
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    /**
     * Registro de devolução física com conferência obrigatória de integridade / avarias.
     */
    public void registrarDevolucao(UUID operadorId, String checkAvarias, Instant agora) {
        Objects.requireNonNull(operadorId, "Identificador do operador de devolução é obrigatório.");
        if (this.tipoItem != TipoItemReserva.ATIVO) {
            throw new IllegalStateException(
                    "Registro de devolução com avarias é aplicável exclusivamente a bens patrimoniais móveis (ATIVO).");
        }
        if (this.status != StatusReserva.RETIRADO) {
            throw new IllegalStateException(
                    "Apenas reservas com status RETIRADO podem ser devolvidas. Status atual: " + this.status);
        }
        Objects.requireNonNull(checkAvarias, "O relatório de conferência de avarias (check_avarias) é obrigatório.");
        if (checkAvarias.trim().isBlank()) {
            throw new IllegalArgumentException(
                    "O preenchimento do checklist/descrição de avarias é obrigatório para registrar devolução.");
        }
        this.status = StatusReserva.FINALIZADA;
        this.checkAvarias = checkAvarias.trim();
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    /**
     * Finalização de uso direto de local físico.
     */
    public void finalizarUsoLocal(Instant agora) {
        Objects.requireNonNull(agora, "Instante da finalização é obrigatório.");
        if (this.tipoItem != TipoItemReserva.LOCAL) {
            throw new IllegalStateException(
                    "Apenas locais podem ser finalizados diretamente sem checklist de devolução.");
        }
        if (this.status != StatusReserva.CONFIRMADA) {
            throw new IllegalStateException("Apenas reservas confirmadas podem ser finalizadas.");
        }
        if (agora.isBefore(this.intervalo.dataHoraInicio())) {
            throw new IllegalStateException("O uso do local só pode ser finalizado depois de iniciado.");
        }
        this.status = StatusReserva.FINALIZADA;
        this.updatedAt = agora;
    }

    /**
     * Instante em que o pedido pendente deixa de poder ser analisado: 48 horas antes do início ou, quando o
     * pedido foi feito com menos antecedência que isso, o próprio início. Assim um pedido tardio não expira
     * no instante em que nasce, antes que alguém possa analisá-lo.
     */
    public Instant prazoDeAnalise() {
        Instant limite = intervalo.dataHoraInicio().minus(ANTECEDENCIA_ANALISE);
        return createdAt.isAfter(limite) ? intervalo.dataHoraInicio() : limite;
    }

    /** Verdadeiro quando a reserva ainda está pendente e o prazo de análise já se esgotou. */
    public boolean analiseVencida(Instant agora) {
        return status == StatusReserva.PENDENTE && !agora.isBefore(prazoDeAnalise());
    }

    /**
     * Expiração de pedido pendente cujo prazo de análise venceu (RN-RES-05).
     */
    public void expirar(Instant agora) {
        if (this.status != StatusReserva.PENDENTE) {
            throw new IllegalStateException(
                    "Apenas reservas com status PENDENTE podem ser expiradas. Status atual: " + this.status);
        }
        this.status = StatusReserva.EXPIRADA;
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    public void atualizarIntervalo(IntervaloReserva novoIntervalo, Instant agora) {
        this.intervalo = Objects.requireNonNull(novoIntervalo, "Novo intervalo da reserva é obrigatório.");
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    public void atualizarDescricao(String novaDescricao, Instant agora) {
        Objects.requireNonNull(novaDescricao, "Nova descrição é obrigatória.");
        if (novaDescricao.trim().isBlank()) {
            throw new IllegalArgumentException("A descrição da reserva não pode ser vazia.");
        }
        this.descricao = novaDescricao.trim();
        this.updatedAt = agora != null ? agora : Instant.now();
    }

    public void softDelete(Instant agora) {
        this.deletedAt = agora != null ? agora : Instant.now();
        this.updatedAt = this.deletedAt;
    }

    // Getters
    public ReservaId getId() {
        return id;
    }

    public TipoItemReserva getTipoItem() {
        return tipoItem;
    }

    public UUID getItemId() {
        return itemId;
    }

    public IntervaloReserva getIntervalo() {
        return intervalo;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public UUID getPastoralMovimentoId() {
        return pastoralMovimentoId;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public UUID getGrupoRecorrenciaId() {
        return grupoRecorrenciaId;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public UUID getAprovadorId() {
        return aprovadorId;
    }

    public Instant getAprovadoEm() {
        return aprovadoEm;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public UUID getCanceladoPorId() {
        return canceladoPorId;
    }

    public String getCheckAvarias() {
        return checkAvarias;
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
