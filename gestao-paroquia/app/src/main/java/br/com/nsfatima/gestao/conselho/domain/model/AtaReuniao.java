package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaImutavelException;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.exception.TipoReuniaoIncompativelException;
import br.com.nsfatima.gestao.conselho.domain.exception.TransicaoAtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.policy.QuorumPolicy;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniaoPolicy;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Agregado raiz da ata de uma reunião da paróquia ou de uma comunidade. Registra o que foi tratado e
 * deliberado, por tipo de reunião, e responde por três garantias:
 *
 * <ul>
 *   <li>Segregação de funções: quem redige não homologa. O redator conduz a ata até a submissão; a
 *   homologação é privativa do Pároco (RN-CON-02), e só para os tipos que a exigem.</li>
 *   <li>Selagem: ata homologada ou registrada é imutável (RN-CON-03); correções vêm por ata
 *   retificadora, que é uma nova ata ligada à original.</li>
 *   <li>Numeração: o número sequencial nasce junto com a selagem, nunca antes dela.</li>
 * </ul>
 *
 * <p>O conteúdo editável ({@link ConteudoAta}) só muda enquanto a ata é rascunho, por substituição
 * integral e validada contra as regras do tipo. As organizações são referenciadas apenas por ID.</p>
 */
public final class AtaReuniao {

    /** Estado completo para reconstituir o agregado a partir da persistência. */
    public record Estado(
            AtaReuniaoId id,
            TipoReuniao tipo,
            UUID organizacaoConvocanteId,
            UUID organizacaoAssociadaId,
            String redatorId,
            Instant criadoEmUtc,
            AtaReuniaoId ataRetificadaId,
            String justificativaRetificacao,
            ConteudoAta conteudo,
            StatusAta status,
            Integer numero,
            Integer ano,
            Integer membrosConvocados,
            String homologadorId,
            String motivoVeto,
            Instant decididoEmUtc) {
    }

    private final AtaReuniaoId id;
    private final TipoReuniao tipo;
    private final UUID organizacaoConvocanteId;
    private final UUID organizacaoAssociadaId;
    private final String redatorId;
    private final Instant criadoEmUtc;
    private final AtaReuniaoId ataRetificadaId;
    private final String justificativaRetificacao;

    private ConteudoAta conteudo;
    private StatusAta status;
    private Integer numero;
    private Integer ano;
    private Integer membrosConvocados;
    private String homologadorId;
    private String motivoVeto;
    private Instant decididoEmUtc;

    private AtaReuniao(Estado e) {
        this.id = Objects.requireNonNull(e.id(), "O ID da ata é obrigatório.");
        this.tipo = Objects.requireNonNull(e.tipo(), "O tipo da reunião é obrigatório.");
        this.organizacaoConvocanteId = Objects.requireNonNull(e.organizacaoConvocanteId(),
                "A organização convocante é obrigatória.");
        this.organizacaoAssociadaId = e.organizacaoAssociadaId();
        this.redatorId = Objects.requireNonNull(e.redatorId(), "O redator da ata é obrigatório.");
        this.criadoEmUtc = Objects.requireNonNull(e.criadoEmUtc(), "A data de criação da ata é obrigatória.");
        this.ataRetificadaId = e.ataRetificadaId();
        this.justificativaRetificacao = e.justificativaRetificacao();
        this.conteudo = Objects.requireNonNull(e.conteudo(), "O conteúdo da ata é obrigatório.");
        this.status = Objects.requireNonNull(e.status(), "O status da ata é obrigatório.");
        this.numero = e.numero();
        this.ano = e.ano();
        this.membrosConvocados = e.membrosConvocados();
        this.homologadorId = e.homologadorId();
        this.motivoVeto = e.motivoVeto();
        this.decididoEmUtc = e.decididoEmUtc();
        exigirCoerenciaDeSelagem();
        exigirConteudoCompativelComTipo(this.conteudo);
    }

    /** Reconstitui o agregado já persistido, revalidando suas invariantes. */
    public static AtaReuniao reconstituir(Estado estado) {
        return new AtaReuniao(estado);
    }

    /**
     * Abre uma nova ata em rascunho, conferindo que o tipo de reunião pode ser convocado pela organização.
     *
     * @param convocante organização que convoca a reunião
     * @param associada segunda organização, exigida só pela reunião conjunta (o CAEP); {@code null} nos demais
     * @throws TipoReuniaoIncompativelException se a organização não pode convocar o tipo
     */
    public static AtaReuniao rascunhar(AtaReuniaoId id, TipoReuniao tipo, ReferenciaOrganizacao convocante,
                                       ReferenciaOrganizacao associada, ConteudoAta conteudo, String redatorId,
                                       Instant agora) {
        exigirOrganizacoesCompativeis(tipo, convocante, associada);
        return new AtaReuniao(new Estado(id, tipo, convocante.id(), associada == null ? null : associada.id(),
                redatorId, agora, null, null, conteudo, StatusAta.RASCUNHO, null, null, null, null, null, null));
    }

    private static void exigirOrganizacoesCompativeis(TipoReuniao tipo, ReferenciaOrganizacao convocante,
                                                      ReferenciaOrganizacao associada) {
        RegrasTipoReuniao regras = RegrasTipoReuniaoPolicy.para(tipo);
        if (!regras.aceitaConvocante(convocante)) {
            throw new TipoReuniaoIncompativelException(
                    "A organização informada não pode convocar: " + tipo.getRotulo() + ".");
        }
        if (regras.exigeOrganizacaoAssociada()) {
            if (associada == null || !regras.aceitaAssociada(associada)) {
                throw new TipoReuniaoIncompativelException(
                        "A reunião conjunta exige a organização associada (CAEP).");
            }
        } else if (associada != null) {
            throw new TipoReuniaoIncompativelException("Este tipo de reunião não admite organização associada.");
        }
    }

    /** Substitui o conteúdo do rascunho; ata selada ou já submetida não admite edição. */
    public void redigir(ConteudoAta novoConteudo) {
        exigirStatus(StatusAta.RASCUNHO, "Somente rascunhos podem ser editados.");
        exigirConteudoCompativelComTipo(Objects.requireNonNull(novoConteudo, "O conteúdo da ata é obrigatório."));
        this.conteudo = novoConteudo;
    }

    /**
     * Conclui a redação. Se o tipo exige homologação a ata vai ao Pároco; senão é registrada e selada de
     * imediato, já numerada.
     *
     * @param membrosConvocados total de membros ativos convocados, congelado para o quórum; ignorado
     *     nos tipos sem quórum
     */
    public void submeter(Integer membrosConvocados, NumeradorAta numerador, Instant agora) {
        exigirStatus(StatusAta.RASCUNHO, "Somente rascunhos podem ser submetidos.");
        exigirCompleta(membrosConvocados);
        this.membrosConvocados = regras().aplicaQuorum() ? membrosConvocados : null;
        this.motivoVeto = null;
        this.decididoEmUtc = null;
        if (regras().exigeHomologacao()) {
            this.status = StatusAta.PENDENTE_HOMOLOGACAO;
        } else {
            selar(StatusAta.REGISTRADA, numerador, agora);
        }
    }

    /** Homologação privativa do Pároco: sela a ata e lhe atribui o número. */
    public void homologar(String homologador, NumeradorAta numerador, Instant agora) {
        exigirStatus(StatusAta.PENDENTE_HOMOLOGACAO, "Somente atas pendentes de homologação podem ser decididas.");
        this.homologadorId = Objects.requireNonNull(homologador, "O homologador é obrigatório.");
        selar(StatusAta.HOMOLOGADA, numerador, agora);
    }

    /** Veto do Pároco, com motivo obrigatório; a ata volta ao redator por {@link #reabrir()}. */
    public void vetar(String homologador, String motivo, Instant agora) {
        exigirStatus(StatusAta.PENDENTE_HOMOLOGACAO, "Somente atas pendentes de homologação podem ser decididas.");
        if (motivo == null || motivo.isBlank()) {
            throw new AtaInvalidaException("O motivo do veto é obrigatório.");
        }
        this.status = StatusAta.VETADA;
        this.homologadorId = Objects.requireNonNull(homologador, "O homologador é obrigatório.");
        this.motivoVeto = motivo.trim();
        this.decididoEmUtc = Objects.requireNonNull(agora, "O instante da decisão é obrigatório.");
    }

    /** Devolve ao redator a ata vetada, mantendo o motivo do veto à vista até a nova submissão. */
    public void reabrir() {
        exigirStatus(StatusAta.VETADA, "Somente atas vetadas podem ser reabertas.");
        this.status = StatusAta.RASCUNHO;
    }

    /**
     * Abre a ata retificadora de uma ata selada. A original permanece intacta; a nova ata nasce em
     * rascunho, com o mesmo tipo, organizações e conteúdo, e percorre o fluxo normal do tipo.
     */
    public AtaReuniao abrirRetificacao(AtaReuniaoId novoId, String justificativa, String redator, Instant agora) {
        if (!status.selada()) {
            throw new TransicaoAtaInvalidaException("Somente atas homologadas ou registradas podem ser retificadas.");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new AtaInvalidaException("A justificativa da retificação é obrigatória.");
        }
        return new AtaReuniao(new Estado(novoId, tipo, organizacaoConvocanteId, organizacaoAssociadaId, redator,
                agora, id, justificativa.trim(), conteudo, StatusAta.RASCUNHO, null, null, null, null, null, null));
    }

    private void selar(StatusAta destino, NumeradorAta numerador, Instant agora) {
        int anoDaAta = agora.atZone(ZoneOffset.UTC).getYear();
        this.numero = numerador.proximo(organizacaoConvocanteId, anoDaAta);
        this.ano = anoDaAta;
        this.status = destino;
        this.motivoVeto = null;
        this.decididoEmUtc = agora;
    }

    private void exigirStatus(StatusAta esperado, String mensagem) {
        if (status == esperado) {
            return;
        }
        if (status.selada()) {
            throw new AtaImutavelException("Ata selada não pode ser alterada; use a ata retificadora.");
        }
        throw new TransicaoAtaInvalidaException(mensagem + " Situação atual: " + status + ".");
    }

    private void exigirCoerenciaDeSelagem() {
        boolean numerada = numero != null && ano != null;
        if (status.selada() != numerada) {
            throw new AtaInvalidaException("Somente atas seladas têm número, e toda ata selada tem número.");
        }
    }

    private void exigirCompleta(Integer membros) {
        if (conteudo.itens().isEmpty()) {
            throw new AtaInvalidaException("Registre ao menos um item de pauta antes de submeter a ata.");
        }
        if (!conteudo.presenca().informada()) {
            throw new AtaInvalidaException("Registre a presença antes de submeter a ata.");
        }
        if (regras().aplicaQuorum() && (membros == null || membros < 1)) {
            throw new AtaInvalidaException("O órgão não tem membros ativos para compor o quórum.");
        }
    }

    private void exigirConteudoCompativelComTipo(ConteudoAta c) {
        RegrasTipoReuniao regras = regras();
        PresencaReuniao presenca = c.presenca();
        boolean nominal = regras.modoPresenca().nominal();
        if (nominal && presenca.total() != null) {
            throw new AtaInvalidaException("Este tipo de reunião exige presença nominal.");
        }
        if (!nominal && presenca.ehNominal()) {
            throw new AtaInvalidaException("Este tipo de reunião registra apenas o total de participantes.");
        }
        for (ItemPauta item : c.itens()) {
            for (Deliberacao deliberacao : item.deliberacoes()) {
                exigirDeliberacaoCompativel(deliberacao, regras, presenca);
            }
        }
    }

    private static void exigirDeliberacaoCompativel(Deliberacao d, RegrasTipoReuniao regras,
                                                    PresencaReuniao presenca) {
        if (d.votacao() != null) {
            if (!regras.permiteVotacao()) {
                throw new AtaInvalidaException("Este tipo de reunião não registra votação.");
            }
            if (d.votacao().total() > presenca.presentes() - d.conflitos().size()) {
                throw new AtaInvalidaException("Os votos excedem os presentes aptos a votar.");
            }
        }
        for (ConflitoInteresse conflito : d.conflitos()) {
            if (presenca.ehNominal() && !presenca.fieisPresentes().contains(conflito.declaranteFielId())) {
                throw new AtaInvalidaException("O declarante do conflito de interesses deve estar presente.");
            }
        }
    }

    /** Regras do tipo de reunião desta ata. */
    public RegrasTipoReuniao regras() {
        return RegrasTipoReuniaoPolicy.para(tipo);
    }

    /** Quórum apurado, presente apenas nos tipos que o aplicam e depois da submissão. */
    public Optional<ResultadoQuorum> quorum() {
        if (!regras().aplicaQuorum() || membrosConvocados == null) {
            return Optional.empty();
        }
        return Optional.of(QuorumPolicy.avaliar(membrosConvocados, conteudo.presenca().presentes()));
    }

    /** Número da ata no formato {@code 003/2026}, ou vazio enquanto ainda é minuta. */
    public Optional<String> identificacao() {
        if (numero == null) {
            return Optional.empty();
        }
        return Optional.of(String.format("%03d/%d", numero, ano));
    }

    public AtaReuniaoId getId() {
        return id;
    }

    public TipoReuniao getTipo() {
        return tipo;
    }

    public UUID getOrganizacaoConvocanteId() {
        return organizacaoConvocanteId;
    }

    public UUID getOrganizacaoAssociadaId() {
        return organizacaoAssociadaId;
    }

    public String getRedatorId() {
        return redatorId;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public AtaReuniaoId getAtaRetificadaId() {
        return ataRetificadaId;
    }

    public String getJustificativaRetificacao() {
        return justificativaRetificacao;
    }

    public ConteudoAta getConteudo() {
        return conteudo;
    }

    public StatusAta getStatus() {
        return status;
    }

    public Integer getNumero() {
        return numero;
    }

    public Integer getAno() {
        return ano;
    }

    public Integer getMembrosConvocados() {
        return membrosConvocados;
    }

    public String getHomologadorId() {
        return homologadorId;
    }

    public String getMotivoVeto() {
        return motivoVeto;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }
}
