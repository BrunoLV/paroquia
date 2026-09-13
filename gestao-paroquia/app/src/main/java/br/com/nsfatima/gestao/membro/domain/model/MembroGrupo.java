package br.com.nsfatima.gestao.membro.domain.model;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz do vínculo de um {@code Fiel} a uma organização paroquial, com o papel que exerce ali.
 *
 * <p>Concentra as invariantes que antes viviam como sequências de {@code setX(...)} nos casos de uso
 * {@code AdicionarMembroUseCase} / {@code UpdateMembroUseCase} / {@code PromoverACoordenadorUseCase}:</p>
 * <ul>
 *   <li>o papel nunca é nulo e nunca é um papel <em>global</em> ({@code PAROCO}/{@code VIGARIO}/{@code ADMIN}) —
 *   esses só são concedidos via {@code AtribuirPapelGlobalUseCase};</li>
 *   <li>o vínculo nasce ativo; ativar/desativar é explícito.</li>
 * </ul>
 */
public class MembroGrupo {

    private final UUID id;
    private UUID fielId;
    private UUID grupoParoquialId;
    private TipoGrupo tipoGrupo;
    private PapelOrganizacional papel;
    private UUID igrejaVinculoId;
    private LocalDate dataEntrada;
    private boolean ativo;

    public MembroGrupo(
            UUID id,
            UUID fielId,
            UUID grupoParoquialId,
            TipoGrupo tipoGrupo,
            PapelOrganizacional papel,
            UUID igrejaVinculoId,
            LocalDate dataEntrada,
            boolean ativo) {
        this.id = Objects.requireNonNull(id, "ID do vínculo é obrigatório.");
        this.fielId = Objects.requireNonNull(fielId, "Fiel do vínculo é obrigatório.");
        this.grupoParoquialId = Objects.requireNonNull(grupoParoquialId, "Organização do vínculo é obrigatória.");
        this.tipoGrupo = Objects.requireNonNull(tipoGrupo, "Tipo de grupo é obrigatório.");
        this.igrejaVinculoId = igrejaVinculoId;
        this.dataEntrada = dataEntrada;
        this.ativo = ativo;
        atribuirPapel(papel);
    }

    /** Abre um novo vínculo ativo. */
    public static MembroGrupo novo(
            UUID id,
            UUID fielId,
            UUID grupoParoquialId,
            TipoGrupo tipoGrupo,
            PapelOrganizacional papel,
            UUID igrejaVinculoId,
            LocalDate dataEntrada) {
        return new MembroGrupo(id, fielId, grupoParoquialId, tipoGrupo, papel, igrejaVinculoId,
                dataEntrada != null ? dataEntrada : LocalDate.now(), true);
    }

    public final void atribuirPapel(PapelOrganizacional novoPapel) {
        Objects.requireNonNull(novoPapel, "Papel do vínculo é obrigatório.");
        if (novoPapel.isGlobal()) {
            throw new IllegalArgumentException(
                    "Papel " + novoPapel + " é global — atribua via AtribuirPapelGlobalUseCase, não MembroGrupo");
        }
        this.papel = novoPapel;
    }

    /** Reaponta os dados cadastrais do vínculo (fluxo de edição). */
    public void alterarVinculo(
            UUID fielId,
            UUID grupoParoquialId,
            TipoGrupo tipoGrupo,
            PapelOrganizacional papel,
            UUID igrejaVinculoId,
            LocalDate dataEntrada) {
        this.fielId = Objects.requireNonNull(fielId, "Fiel do vínculo é obrigatório.");
        this.grupoParoquialId = Objects.requireNonNull(grupoParoquialId, "Organização do vínculo é obrigatória.");
        this.tipoGrupo = Objects.requireNonNull(tipoGrupo, "Tipo de grupo é obrigatório.");
        this.igrejaVinculoId = igrejaVinculoId;
        if (dataEntrada != null) {
            this.dataEntrada = dataEntrada;
        }
        atribuirPapel(papel);
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    /** {@code true} se o papel atual é de coordenação — deve sincronizar a liderança da organização. */
    public boolean exercerCoordenacao() {
        return papel.isCoordenacao();
    }

    public UUID getId() {
        return id;
    }

    public UUID getFielId() {
        return fielId;
    }

    public UUID getGrupoParoquialId() {
        return grupoParoquialId;
    }

    public TipoGrupo getTipoGrupo() {
        return tipoGrupo;
    }

    public PapelOrganizacional getPapel() {
        return papel;
    }

    public UUID getIgrejaVinculoId() {
        return igrejaVinculoId;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
