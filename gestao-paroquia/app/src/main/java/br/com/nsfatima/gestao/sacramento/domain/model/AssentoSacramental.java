package br.com.nsfatima.gestao.sacramento.domain.model;

import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoImutavelException;
import br.com.nsfatima.gestao.sacramento.domain.exception.AssentoInvalidoException;
import br.com.nsfatima.gestao.sacramento.domain.policy.RequisitosAssentoPolicy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Agregado raiz do assento sacramental (Cân. 535): termo com fé pública que nasce {@code DRAFT},
 * é selado pelo Pároco ({@code SEALED}, teor somente-leitura) e jamais é excluído — apenas
 * {@code CANCELADO} com motivo.
 *
 * <ul>
 *   <li>O teor só é editável em rascunho; depois de selado, correções viram {@link Ressalva}.</li>
 *   <li>Vincular/desvincular Fiel ou Clérigo troca só o ponteiro, nunca o snapshot textual.</li>
 *   <li>Averbações existem apenas em Batismo selado e são append-only.</li>
 *   <li>A visibilidade (livro comum/reservado) é definida na lavratura e não muda.</li>
 * </ul>
 */
public class AssentoSacramental {

    private final AssentoSacramentalId id;
    private final TipoSacramento tipo;
    private final Visibilidade visibilidade;
    private final LocalDate dataLavratura;
    private final String lavradoPor;
    private final Instant criadoEmUtc;

    private StatusAssento status;
    private TeorAssento teor;
    private String homologadoPor;
    private Instant homologadoEmUtc;
    private String motivoCancelamento;
    private String canceladoPor;
    private Instant canceladoEmUtc;
    private final List<Ressalva> ressalvas;
    private final List<Averbacao> averbacoes;
    private final List<NotificacaoCanonica> notificacoes;

    /** Reconstitui o agregado a partir da persistência. */
    public AssentoSacramental(AssentoSacramentalId id, TipoSacramento tipo, Visibilidade visibilidade,
                              LocalDate dataLavratura, String lavradoPor, Instant criadoEmUtc, StatusAssento status,
                              TeorAssento teor, String homologadoPor, Instant homologadoEmUtc,
                              String motivoCancelamento, String canceladoPor, Instant canceladoEmUtc,
                              List<Ressalva> ressalvas, List<Averbacao> averbacoes,
                              List<NotificacaoCanonica> notificacoes) {
        this.id = Objects.requireNonNull(id, "ID do assento é obrigatório.");
        this.tipo = Objects.requireNonNull(tipo, "Tipo de sacramento é obrigatório.");
        this.visibilidade = Objects.requireNonNull(visibilidade, "Visibilidade é obrigatória.");
        this.dataLavratura = Objects.requireNonNull(dataLavratura, "Data da lavratura é obrigatória.");
        this.lavradoPor = lavradoPor;
        this.criadoEmUtc = criadoEmUtc;
        this.status = Objects.requireNonNull(status, "Status é obrigatório.");
        this.teor = Objects.requireNonNull(teor, "Teor é obrigatório.");
        this.homologadoPor = homologadoPor;
        this.homologadoEmUtc = homologadoEmUtc;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladoPor = canceladoPor;
        this.canceladoEmUtc = canceladoEmUtc;
        this.ressalvas = new ArrayList<>(Objects.requireNonNullElse(ressalvas, List.of()));
        this.averbacoes = new ArrayList<>(Objects.requireNonNullElse(averbacoes, List.of()));
        this.notificacoes = new ArrayList<>(Objects.requireNonNullElse(notificacoes, List.of()));
    }

    /** Lavra um novo rascunho; Penitência não é um {@link TipoSacramento} e portanto inexiste aqui. */
    public static AssentoSacramental lavrar(AssentoSacramentalId id, TipoSacramento tipo, Visibilidade visibilidade,
                                            TeorAssento teor, String lavradoPor, Instant agora) {
        return new AssentoSacramental(id, tipo, visibilidade, LocalDate.now(), lavradoPor, agora, StatusAssento.DRAFT,
                teor, null, null, null, null, null, List.of(), List.of(), List.of());
    }

    public void substituirTeor(TeorAssento novoTeor) {
        if (status != StatusAssento.DRAFT) {
            throw new AssentoImutavelException("Somente rascunhos podem ter o teor alterado; use retificação.");
        }
        this.teor = Objects.requireNonNull(novoTeor, "Teor é obrigatório.");
    }

    /** Sela o termo se a policy do tipo não apontar pendências. */
    public void homologar(RequisitosAssentoPolicy policy, String parocoUsuario, Instant agora) {
        if (status != StatusAssento.DRAFT) {
            throw new AssentoImutavelException("Apenas rascunhos podem ser homologados. Status atual: " + status);
        }
        if (policy.tipo() != tipo) {
            throw new IllegalArgumentException("Policy de " + policy.tipo() + " não se aplica a " + tipo);
        }
        List<String> pendencias = policy.pendencias(teor);
        if (!pendencias.isEmpty()) {
            throw new AssentoInvalidoException(pendencias);
        }
        this.status = StatusAssento.SEALED;
        this.homologadoPor = parocoUsuario;
        this.homologadoEmUtc = agora;
    }

    public void cancelar(String motivo, String parocoUsuario, Instant agora) {
        if (status == StatusAssento.CANCELADO) {
            throw new AssentoImutavelException("Assento já cancelado.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Motivo do cancelamento é obrigatório.");
        }
        this.status = StatusAssento.CANCELADO;
        this.motivoCancelamento = motivo;
        this.canceladoPor = parocoUsuario;
        this.canceladoEmUtc = agora;
    }

    /** Registra retificação material em termo selado sem alterar o teor original. */
    public Ressalva retificar(String campo, String valorOriginal, String valorCorrigido, String justificativa,
                              String parocoUsuario, Instant agora) {
        if (status != StatusAssento.SEALED) {
            throw new AssentoImutavelException("Retificação exige assento homologado; "
                    + "rascunhos são editados diretamente.");
        }
        Ressalva ressalva = new Ressalva(UUID.randomUUID(), campo, valorOriginal, valorCorrigido, justificativa,
                parocoUsuario, agora);
        ressalvas.add(ressalva);
        return ressalva;
    }

    /**
     * Vincula o participante a um fiel. Vínculo da Secretaria entra pendente e só vale após o Pároco aprovar;
     * o do Pároco já nasce aprovado. Participante com vínculo (aprovado ou pendente) não aceita outro.
     */
    public void vincularFiel(UUID participanteId, UUID fielId, boolean pendente) {
        trocarParticipante(participanteId, p -> {
            exigirSemVinculo(p);
            return p.comFiel(fielId, pendente);
        });
    }

    /** Idem {@link #vincularFiel}, para clérigos. */
    public void vincularClerigo(UUID participanteId, UUID clerigoId, boolean pendente) {
        trocarParticipante(participanteId, p -> {
            exigirSemVinculo(p);
            return p.comClerigo(clerigoId, pendente);
        });
    }

    /** Pároco aprova um vínculo proposto pela Secretaria. */
    public void aprovarVinculo(UUID participanteId) {
        trocarParticipante(participanteId, p -> {
            exigirPendente(p);
            return p.comVinculoAprovado();
        });
    }

    /** Pároco rejeita um vínculo proposto: o ponteiro é removido e o texto lavrado permanece. */
    public void rejeitarVinculo(UUID participanteId) {
        trocarParticipante(participanteId, p -> {
            exigirPendente(p);
            return p.semVinculo();
        });
    }

    public void desvincular(UUID participanteId) {
        trocarParticipante(participanteId, Participante::semVinculo);
    }

    public Averbacao inserirAverbacao(Averbacao averbacao) {
        if (tipo != TipoSacramento.BATISMO) {
            throw new AssentoImutavelException("Averbações só existem à margem do assento de Batismo.");
        }
        if (status != StatusAssento.SEALED) {
            throw new AssentoImutavelException("Averbação exige assento de batismo homologado.");
        }
        averbacoes.add(averbacao);
        return averbacao;
    }

    public void homologarAverbacao(UUID averbacaoId, String parocoUsuario, Instant agora) {
        for (int i = 0; i < averbacoes.size(); i++) {
            if (averbacoes.get(i).id().equals(averbacaoId)) {
                averbacoes.set(i, averbacoes.get(i).homologar(parocoUsuario, agora));
                return;
            }
        }
        throw new IllegalArgumentException("Averbação não encontrada neste assento.");
    }

    public NotificacaoCanonica registrarNotificacao(NotificacaoCanonica notificacao) {
        if (tipo == TipoSacramento.BATISMO) {
            throw new AssentoImutavelException("Batismo é o assento de origem e não notifica outra paróquia.");
        }
        if (status != StatusAssento.SEALED) {
            throw new AssentoImutavelException("Notificação canônica exige assento homologado.");
        }
        notificacoes.add(notificacao);
        return notificacao;
    }

    private static void exigirSemVinculo(Participante p) {
        if (p.vinculado()) {
            throw new AssentoImutavelException("Participante já possui vínculo; desvincule antes de vincular de novo.");
        }
    }

    private static void exigirPendente(Participante p) {
        if (!p.vinculoPendente()) {
            throw new AssentoImutavelException("Participante não possui vínculo pendente de aprovação.");
        }
    }

    private void trocarParticipante(UUID participanteId, UnaryOperator<Participante> troca) {
        if (status == StatusAssento.CANCELADO) {
            throw new AssentoImutavelException("Assento cancelado não admite novos vínculos.");
        }
        List<Participante> novos = new ArrayList<>(teor.participantes());
        for (int i = 0; i < novos.size(); i++) {
            if (novos.get(i).id().equals(participanteId)) {
                novos.set(i, troca.apply(novos.get(i)));
                this.teor = new TeorAssento(teor.referenciaLivro(), teor.paroquiaCustodiante(), teor.dataCelebracao(),
                        teor.localCelebracao(), novos, teor.dadosEspecificos(), teor.dadoSensivel());
                return;
            }
        }
        throw new IllegalArgumentException("Participante não encontrado neste assento.");
    }

    public AssentoSacramentalId getId() {
        return id;
    }

    public TipoSacramento getTipo() {
        return tipo;
    }

    public Visibilidade getVisibilidade() {
        return visibilidade;
    }

    public LocalDate getDataLavratura() {
        return dataLavratura;
    }

    public String getLavradoPor() {
        return lavradoPor;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public StatusAssento getStatus() {
        return status;
    }

    public TeorAssento getTeor() {
        return teor;
    }

    public String getHomologadoPor() {
        return homologadoPor;
    }

    public Instant getHomologadoEmUtc() {
        return homologadoEmUtc;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public Instant getCanceladoEmUtc() {
        return canceladoEmUtc;
    }

    public List<Ressalva> getRessalvas() {
        return List.copyOf(ressalvas);
    }

    public List<Averbacao> getAverbacoes() {
        return List.copyOf(averbacoes);
    }

    public List<NotificacaoCanonica> getNotificacoes() {
        return List.copyOf(notificacoes);
    }
}
