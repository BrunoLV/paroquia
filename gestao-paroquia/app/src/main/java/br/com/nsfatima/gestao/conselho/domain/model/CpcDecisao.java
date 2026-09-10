package br.com.nsfatima.gestao.conselho.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz de uma ata/decisão de conselho paroquial (CPC/CPP).
 *
 * <p>Encapsula o fluxo de duas etapas do contexto — registro do fato e posterior homologação —
 * e as invariantes que antes viviam como setters coordenados em
 * {@code HomologarCpcDecisaoUseCase}:</p>
 *
 * <ul>
 *   <li>Toda decisão nasce {@link StatusHomologacao#PENDENTE_HOMOLOGACAO}.</li>
 *   <li>Só uma decisão pendente pode ser homologada ou vetada; decidir de novo é rejeitado
 *   (antes uma decisão já {@code HOMOLOGADO} podia ser re-vetada silenciosamente).</li>
 *   <li>Vetar exige motivo não-vazio; homologar limpa qualquer motivo de veto anterior.</li>
 * </ul>
 */
public class CpcDecisao {

    private final UUID id;
    private final String titulo;
    private final String pautaDescricao;
    private final LocalDate dataReuniao;
    private final UUID comunidadeId;
    private final String conselhoTipo;
    private final String solicitanteId;
    private final String solicitantePapel;
    private final Instant criadoEmUtc;

    private StatusHomologacao statusHomologacao;
    private String homologadorId;
    private String motivoVeto;
    private Instant decididoEmUtc;

    public CpcDecisao(
            UUID id,
            String titulo,
            String pautaDescricao,
            LocalDate dataReuniao,
            UUID comunidadeId,
            String conselhoTipo,
            StatusHomologacao statusHomologacao,
            String solicitanteId,
            String solicitantePapel,
            String homologadorId,
            String motivoVeto,
            Instant criadoEmUtc,
            Instant decididoEmUtc) {
        this.id = Objects.requireNonNull(id, "ID da decisão é obrigatório.");
        this.titulo = exigirNaoBranco(titulo, "Título da decisão é obrigatório.");
        this.pautaDescricao = exigirNaoBranco(pautaDescricao, "Descrição da pauta é obrigatória.");
        this.dataReuniao = Objects.requireNonNull(dataReuniao, "Data da reunião é obrigatória.");
        this.comunidadeId = Objects.requireNonNull(comunidadeId, "Comunidade da decisão é obrigatória.");
        this.conselhoTipo = conselhoTipo == null || conselhoTipo.isBlank() ? "CPC" : conselhoTipo;
        this.statusHomologacao = Objects.requireNonNull(statusHomologacao, "Status de homologação é obrigatório.");
        this.solicitanteId = solicitanteId;
        this.solicitantePapel = solicitantePapel;
        this.homologadorId = homologadorId;
        this.motivoVeto = motivoVeto;
        this.criadoEmUtc = criadoEmUtc != null ? criadoEmUtc : Instant.now();
        this.decididoEmUtc = decididoEmUtc;
    }

    /**
     * Registra uma nova ata/decisão, sempre pendente de homologação.
     *
     * @param solicitantePapel papel/role do ator autenticado (nunca o nome pessoal — LGPD)
     */
    public static CpcDecisao registrar(
            UUID id,
            String titulo,
            String pautaDescricao,
            LocalDate dataReuniao,
            UUID comunidadeId,
            String solicitanteId,
            String solicitantePapel,
            Instant agora) {
        return new CpcDecisao(id, titulo, pautaDescricao, dataReuniao, comunidadeId, "CPC",
                StatusHomologacao.PENDENTE_HOMOLOGACAO, solicitanteId, solicitantePapel, null, null, agora, null);
    }

    public void homologar(String homologadorId, Instant agora) {
        assegurarPendente();
        this.statusHomologacao = StatusHomologacao.HOMOLOGADO;
        this.homologadorId = homologadorId;
        this.motivoVeto = null;
        this.decididoEmUtc = agora != null ? agora : Instant.now();
    }

    public void vetar(String homologadorId, String motivo, Instant agora) {
        assegurarPendente();
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo do veto é obrigatório ao vetar uma decisão");
        }
        this.statusHomologacao = StatusHomologacao.VETADO;
        this.homologadorId = homologadorId;
        this.motivoVeto = motivo;
        this.decididoEmUtc = agora != null ? agora : Instant.now();
    }

    private void assegurarPendente() {
        if (this.statusHomologacao != StatusHomologacao.PENDENTE_HOMOLOGACAO) {
            throw new IllegalArgumentException(
                    "Apenas decisões pendentes de homologação podem ser decididas. Status atual: "
                            + this.statusHomologacao);
        }
    }

    private static String exigirNaoBranco(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
        return valor;
    }

    public UUID getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getPautaDescricao() {
        return pautaDescricao;
    }

    public LocalDate getDataReuniao() {
        return dataReuniao;
    }

    public UUID getComunidadeId() {
        return comunidadeId;
    }

    public String getConselhoTipo() {
        return conselhoTipo;
    }

    public StatusHomologacao getStatusHomologacao() {
        return statusHomologacao;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public String getSolicitantePapel() {
        return solicitantePapel;
    }

    public String getHomologadorId() {
        return homologadorId;
    }

    public String getMotivoVeto() {
        return motivoVeto;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }
}
