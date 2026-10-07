package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.organizacao.domain.policy.PoliticaDuracaoMandato;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz do mandato de coordenação de uma organização: encargo eclesial temporário exercido por um
 * {@code Fiel} que só é legítimo após a confirmação do Pároco (Cân. 519 e 317).
 *
 * <p>Invariantes: nasce {@code PROPOSTO}; só um mandato proposto pode ser homologado ou vetado; vetar exige
 * motivo; só um mandato homologado pode ser encerrado; a duração respeita {@link PoliticaDuracaoMandato}.
 * Associa-se à organização e ao fiel apenas por identificador.</p>
 */
public class MandatoCoordenacao {

    private final UUID id;
    private final UUID organizacaoId;
    private final UUID fielId;
    private final FuncaoCoordenacao funcao;
    private final LocalDate inicio;
    private final LocalDate fim;
    private StatusMandato status;
    private String homologadoPor;
    private String motivoVeto;
    private Instant decididoEmUtc;

    public MandatoCoordenacao(UUID id, UUID organizacaoId, UUID fielId, FuncaoCoordenacao funcao,
                              LocalDate inicio, LocalDate fim, StatusMandato status, String homologadoPor,
                              String motivoVeto, Instant decididoEmUtc) {
        this.id = Objects.requireNonNull(id, "ID do mandato é obrigatório.");
        this.organizacaoId = Objects.requireNonNull(organizacaoId, "Organização do mandato é obrigatória.");
        this.fielId = Objects.requireNonNull(fielId, "Fiel do mandato é obrigatório.");
        this.funcao = Objects.requireNonNull(funcao, "Função do mandato é obrigatória.");
        PoliticaDuracaoMandato.violacao(inicio, fim).ifPresent(msg -> {
            throw new IllegalArgumentException(msg);
        });
        this.inicio = inicio;
        this.fim = fim;
        this.status = Objects.requireNonNull(status, "Status do mandato é obrigatório.");
        this.homologadoPor = homologadoPor;
        this.motivoVeto = motivoVeto;
        this.decididoEmUtc = decididoEmUtc;
    }

    /**
     * Registra uma nova proposta de mandato, sempre pendente de homologação do Pároco.
     */
    public static MandatoCoordenacao propor(UUID organizacaoId, UUID fielId, FuncaoCoordenacao funcao,
                                            LocalDate inicio, LocalDate fim) {
        return new MandatoCoordenacao(UUID.randomUUID(), organizacaoId, fielId, funcao, inicio, fim,
                StatusMandato.PROPOSTO, null, null, null);
    }

    /**
     * Homologa a proposta: o fiel passa a ter mandato legítimo. Um período já encerrado não pode ser homologado.
     *
     * @param homologador papel/identificador de quem homologou (nunca o nome pessoal — LGPD)
     * @param hoje data corrente, para recusar períodos já vencidos
     */
    public void homologar(String homologador, LocalDate hoje, Instant agora) {
        exigirStatus(StatusMandato.PROPOSTO, "Somente um mandato proposto pode ser homologado.");
        if (fim.isBefore(hoje)) {
            throw new IllegalStateException("O período do mandato já terminou.");
        }
        this.status = StatusMandato.HOMOLOGADO;
        this.homologadoPor = homologador;
        this.motivoVeto = null;
        this.decididoEmUtc = agora;
    }

    /**
     * Veta a proposta; o motivo é obrigatório.
     */
    public void vetar(String homologador, String motivo, Instant agora) {
        exigirStatus(StatusMandato.PROPOSTO, "Somente um mandato proposto pode ser vetado.");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("O motivo do veto é obrigatório.");
        }
        this.status = StatusMandato.VETADO;
        this.homologadoPor = homologador;
        this.motivoVeto = motivo.trim();
        this.decididoEmUtc = agora;
    }

    /**
     * Encerra um mandato homologado, ao fim do prazo ou por decisão pastoral.
     */
    public void encerrar(Instant agora) {
        exigirStatus(StatusMandato.HOMOLOGADO, "Somente um mandato homologado pode ser encerrado.");
        this.status = StatusMandato.ENCERRADO;
        this.decididoEmUtc = agora;
    }

    /**
     * Indica se o mandato está em vigor na data informada.
     *
     * @param data data de referência
     * @return {@code true} se homologado e dentro do período
     */
    public boolean vigenteEm(LocalDate data) {
        return status == StatusMandato.HOMOLOGADO && !data.isBefore(inicio) && !data.isAfter(fim);
    }

    /**
     * Indica se o mandato ainda disputa o cargo (proposto ou homologado), impedindo outro concomitante.
     */
    public boolean ocupaCargo() {
        return status == StatusMandato.PROPOSTO || status == StatusMandato.HOMOLOGADO;
    }

    /**
     * Indica se o período deste mandato se sobrepõe ao período informado.
     */
    public boolean sobrepoe(LocalDate outroInicio, LocalDate outroFim) {
        return !outroInicio.isAfter(fim) && !outroFim.isBefore(inicio);
    }

    private void exigirStatus(StatusMandato esperado, String mensagem) {
        if (status != esperado) {
            throw new IllegalStateException(mensagem);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public UUID getFielId() {
        return fielId;
    }

    public FuncaoCoordenacao getFuncao() {
        return funcao;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public StatusMandato getStatus() {
        return status;
    }

    public String getHomologadoPor() {
        return homologadoPor;
    }

    public String getMotivoVeto() {
        return motivoVeto;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }
}
