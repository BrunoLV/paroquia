package br.com.nsfatima.gestao.clero.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Representa a estrutura de governo canônico e institucional da paróquia perante sua diocese e o clero.
 */
public class GovernoEclesiastico {
    private final UUID id;
    private UUID dioceseId;
    private UUID parocoId;
    private LocalDate dataPosseParoco;
    private String decretoErecaoParoquia;
    private LocalDate dataInstalacaoParoquia;
    private String livroTomboNumero;

    public GovernoEclesiastico(UUID id, UUID dioceseId, UUID parocoId, LocalDate dataPosseParoco,
                               String decretoErecaoParoquia, LocalDate dataInstalacaoParoquia,
                               String livroTomboNumero) {
        this.id = Objects.requireNonNull(id, "ID do governo eclesiástico é obrigatório.");
        this.dioceseId = Objects.requireNonNull(dioceseId, "Diocese vinculada é obrigatória.");
        this.parocoId = parocoId;
        this.dataPosseParoco = dataPosseParoco;
        this.decretoErecaoParoquia = decretoErecaoParoquia != null ? decretoErecaoParoquia.trim() : null;
        this.dataInstalacaoParoquia = dataInstalacaoParoquia;
        this.livroTomboNumero = livroTomboNumero != null ? livroTomboNumero.trim() : null;
    }

    public void atualizar(UUID dioceseId, UUID parocoId, LocalDate dataPosseParoco,
                          String decretoErecaoParoquia, LocalDate dataInstalacaoParoquia,
                          String livroTomboNumero) {
        this.dioceseId = Objects.requireNonNull(dioceseId, "Diocese vinculada é obrigatória.");
        this.parocoId = parocoId;
        this.dataPosseParoco = dataPosseParoco;
        this.decretoErecaoParoquia = decretoErecaoParoquia != null ? decretoErecaoParoquia.trim() : null;
        this.dataInstalacaoParoquia = dataInstalacaoParoquia;
        this.livroTomboNumero = livroTomboNumero != null ? livroTomboNumero.trim() : null;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDioceseId() {
        return dioceseId;
    }

    public UUID getParocoId() {
        return parocoId;
    }

    public LocalDate getDataPosseParoco() {
        return dataPosseParoco;
    }

    public String getDecretoErecaoParoquia() {
        return decretoErecaoParoquia;
    }

    public LocalDate getDataInstalacaoParoquia() {
        return dataInstalacaoParoquia;
    }

    public String getLivroTomboNumero() {
        return livroTomboNumero;
    }
}
