package br.com.nsfatima.gestao.sacramento.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Snapshot textual de uma pessoa no dia da celebração (Late Binding, spec §5.1). Os dados em texto
 * são imutáveis após a lavratura; apenas os ponteiros opcionais {@code fielId}/{@code clerigoId}
 * podem ser vinculados ou desvinculados, sem jamais tocar o texto histórico.
 *
 * @param catolico condição canônica atestada (usada na elegibilidade de padrinho, Cân. 874)
 * @param crismado idem; padrinho exige crisma
 * @param idadeNaCelebracao idade atestada na data do ato
 * @param condicaoCanonica para ministros: BISPO, PRESBITERO ou DIACONO
 * @param vinculoPendente vínculo proposto pela Secretaria, ainda não aprovado pelo Pároco
 */
public record Participante(
        UUID id,
        PapelParticipante papel,
        String nomeHistorico,
        String documento,
        String localNascimento,
        Boolean catolico,
        Boolean crismado,
        Integer idadeNaCelebracao,
        String condicaoCanonica,
        UUID fielId,
        UUID clerigoId,
        boolean vinculoPendente) {

    /** Participante sem vínculo pendente (snapshot recém-lavrado ou vínculo já aprovado). */
    public Participante(UUID id, PapelParticipante papel, String nomeHistorico, String documento,
                        String localNascimento, Boolean catolico, Boolean crismado, Integer idadeNaCelebracao,
                        String condicaoCanonica, UUID fielId, UUID clerigoId) {
        this(id, papel, nomeHistorico, documento, localNascimento, catolico, crismado, idadeNaCelebracao,
                condicaoCanonica, fielId, clerigoId, false);
    }

    public Participante {
        Objects.requireNonNull(id, "ID do participante é obrigatório.");
        Objects.requireNonNull(papel, "Papel do participante é obrigatório.");
        if (nomeHistorico == null || nomeHistorico.isBlank()) {
            throw new IllegalArgumentException("Nome histórico do participante é obrigatório.");
        }
        if (fielId != null && clerigoId != null) {
            throw new IllegalArgumentException("Participante não pode apontar simultaneamente a fiel e clérigo.");
        }
    }

    /**
     * Vincula ao cadastro de fiel, preservando integralmente o snapshot textual.
     *
     * @param pendente {@code true} quando proposto pela Secretaria, aguardando aprovação do Pároco
     */
    public Participante comFiel(UUID novoFielId, boolean pendente) {
        Objects.requireNonNull(novoFielId, "Fiel é obrigatório para vincular.");
        if (papel.aceitaClerigo()) {
            throw new IllegalArgumentException("O papel " + papel + " é vinculado a cadastro de clérigo.");
        }
        return new Participante(id, papel, nomeHistorico, documento, localNascimento, catolico, crismado,
                idadeNaCelebracao, condicaoCanonica, novoFielId, null, pendente);
    }

    /**
     * Vincula ao cadastro de clérigo, preservando integralmente o snapshot textual.
     *
     * @param pendente {@code true} quando proposto pela Secretaria, aguardando aprovação do Pároco
     */
    public Participante comClerigo(UUID novoClerigoId, boolean pendente) {
        Objects.requireNonNull(novoClerigoId, "Clérigo é obrigatório para vincular.");
        if (!papel.aceitaClerigo()) {
            throw new IllegalArgumentException("O papel " + papel + " é vinculado a cadastro de fiel.");
        }
        return new Participante(id, papel, nomeHistorico, documento, localNascimento, catolico, crismado,
                idadeNaCelebracao, condicaoCanonica, null, novoClerigoId, pendente);
    }

    /** Aprova o vínculo proposto pela Secretaria. */
    public Participante comVinculoAprovado() {
        return new Participante(id, papel, nomeHistorico, documento, localNascimento, catolico, crismado,
                idadeNaCelebracao, condicaoCanonica, fielId, clerigoId, false);
    }

    public Participante semVinculo() {
        return new Participante(id, papel, nomeHistorico, documento, localNascimento, catolico, crismado,
                idadeNaCelebracao, condicaoCanonica, null, null, false);
    }

    /**
     * Indica se há ponteiro a fiel ou clérigo.
     *
     * @return {@code true} se vinculado, com vínculo aprovado ou pendente
     */
    public boolean vinculado() {
        return fielId != null || clerigoId != null;
    }
}
