package br.com.nsfatima.gestao.clero.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz do cadastro pessoal e canônico de um clérigo.
 *
 * <p>Concentra as invariantes que antes viviam como o método privado {@code aplicar} de 20 setters em
 * {@code GerenciarClerigoUseCase}:</p>
 * <ul>
 *   <li>o grau da ordem e as datas de ordenação são sempre consistentes ({@link GrauOrdem#validarOrdenacoes});</li>
 *   <li>{@code paisOrigem} é normalizado para maiúsculas;</li>
 *   <li>inativar um clérigo é uma transição explícita (marca {@code deletedAt}); encerrar os encargos
 *   vigentes é orquestrado pela camada de aplicação como consistência eventual.</li>
 * </ul>
 *
 * <p>O construtor completo é de <em>reconstituição</em> (confia no que está persistido); a validação de
 * negócio acontece em {@link #cadastrar} e {@link #atualizarCadastro}.</p>
 */
public class Clerigo {

    private final UUID id;
    private String nomeCivil;
    private String nomeEclesiastico;
    private LocalDate dataNascimento;
    private String paisOrigem;
    private String cpf;
    private String rg;
    private String documentoEstrangeiro;
    private String email;
    private String celular;
    private boolean whatsapp;
    private String telefoneFixo;
    private String endereco;
    private GrauOrdem grauOrdem;
    private String dioceseIncardinacao;
    private LocalDate dataOrdenacaoDiaconal;
    private LocalDate dataOrdenacaoPresbiteral;
    private LocalDate dataOrdenacaoEpiscopal;
    private boolean ativo;
    private Instant deletedAt;
    private final Instant criadoEmUtc;
    private Instant atualizadoEmUtc;
    private final String criadoPor;
    private String atualizadoPor;

    public Clerigo(
            UUID id,
            String nomeCivil,
            String nomeEclesiastico,
            LocalDate dataNascimento,
            String paisOrigem,
            String cpf,
            String rg,
            String documentoEstrangeiro,
            String email,
            String celular,
            boolean whatsapp,
            String telefoneFixo,
            String endereco,
            GrauOrdem grauOrdem,
            String dioceseIncardinacao,
            LocalDate dataOrdenacaoDiaconal,
            LocalDate dataOrdenacaoPresbiteral,
            LocalDate dataOrdenacaoEpiscopal,
            boolean ativo,
            Instant deletedAt,
            Instant criadoEmUtc,
            Instant atualizadoEmUtc,
            String criadoPor,
            String atualizadoPor) {
        this.id = Objects.requireNonNull(id, "ID do clérigo é obrigatório.");
        this.nomeCivil = nomeCivil;
        this.nomeEclesiastico = nomeEclesiastico;
        this.dataNascimento = dataNascimento;
        this.paisOrigem = paisOrigem;
        this.cpf = cpf;
        this.rg = rg;
        this.documentoEstrangeiro = documentoEstrangeiro;
        this.email = email;
        this.celular = celular;
        this.whatsapp = whatsapp;
        this.telefoneFixo = telefoneFixo;
        this.endereco = endereco;
        this.grauOrdem = grauOrdem;
        this.dioceseIncardinacao = dioceseIncardinacao;
        this.dataOrdenacaoDiaconal = dataOrdenacaoDiaconal;
        this.dataOrdenacaoPresbiteral = dataOrdenacaoPresbiteral;
        this.dataOrdenacaoEpiscopal = dataOrdenacaoEpiscopal;
        this.ativo = ativo;
        this.deletedAt = deletedAt;
        this.criadoEmUtc = criadoEmUtc != null ? criadoEmUtc : Instant.now();
        this.atualizadoEmUtc = atualizadoEmUtc != null ? atualizadoEmUtc : this.criadoEmUtc;
        this.criadoPor = criadoPor;
        this.atualizadoPor = atualizadoPor;
    }

    /** Cadastra um novo clérigo, validando o grau da ordem e as datas de ordenação. */
    public static Clerigo cadastrar(UUID id, DadosCadastraisClerigo dados, String actor, Instant agora) {
        Objects.requireNonNull(dados, "Dados cadastrais do clérigo são obrigatórios.");
        validar(dados);
        String ator = actor == null ? "system" : actor;
        Clerigo clerigo = new Clerigo(id,
                dados.nomeCivil(), dados.nomeEclesiastico(), dados.dataNascimento(),
                normalizarPais(dados.paisOrigem()), dados.cpf(), dados.rg(), dados.documentoEstrangeiro(),
                dados.email(), dados.celular(), dados.whatsapp(), dados.telefoneFixo(), dados.endereco(),
                dados.grauOrdem(), dados.dioceseIncardinacao(),
                dados.dataOrdenacaoDiaconal(), dados.dataOrdenacaoPresbiteral(), dados.dataOrdenacaoEpiscopal(),
                true, null, agora, agora, ator, ator);
        return clerigo;
    }

    /** Atualiza os dados civis/canônicos, revalidando a consistência das ordenações. */
    public void atualizarCadastro(DadosCadastraisClerigo dados, String actor, Instant agora) {
        Objects.requireNonNull(dados, "Dados cadastrais do clérigo são obrigatórios.");
        validar(dados);
        this.nomeCivil = dados.nomeCivil();
        this.nomeEclesiastico = dados.nomeEclesiastico();
        this.dataNascimento = dados.dataNascimento();
        this.paisOrigem = normalizarPais(dados.paisOrigem());
        this.cpf = dados.cpf();
        this.rg = dados.rg();
        this.documentoEstrangeiro = dados.documentoEstrangeiro();
        this.email = dados.email();
        this.celular = dados.celular();
        this.whatsapp = dados.whatsapp();
        this.telefoneFixo = dados.telefoneFixo();
        this.endereco = dados.endereco();
        this.grauOrdem = dados.grauOrdem();
        this.dioceseIncardinacao = dados.dioceseIncardinacao();
        this.dataOrdenacaoDiaconal = dados.dataOrdenacaoDiaconal();
        this.dataOrdenacaoPresbiteral = dados.dataOrdenacaoPresbiteral();
        this.dataOrdenacaoEpiscopal = dados.dataOrdenacaoEpiscopal();
        marcarAtualizado(actor, agora);
    }

    /** Inativa o clérigo (soft delete). Os encargos vigentes devem ser encerrados pela orquestração. */
    public void inativar(String actor, Instant agora) {
        this.ativo = false;
        this.deletedAt = agora != null ? agora : Instant.now();
        marcarAtualizado(actor, this.deletedAt);
    }

    private void marcarAtualizado(String actor, Instant agora) {
        this.atualizadoPor = actor == null ? "system" : actor;
        this.atualizadoEmUtc = agora != null ? agora : Instant.now();
    }

    private static void validar(DadosCadastraisClerigo dados) {
        Objects.requireNonNull(dados.grauOrdem(), "Grau da ordem é obrigatório.")
                .validarOrdenacoes(dados.dataOrdenacaoDiaconal(), dados.dataOrdenacaoPresbiteral(),
                        dados.dataOrdenacaoEpiscopal());
    }

    private static String normalizarPais(String paisOrigem) {
        return paisOrigem == null ? null : paisOrigem.toUpperCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getNomeCivil() {
        return nomeCivil;
    }

    public String getNomeEclesiastico() {
        return nomeEclesiastico;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public String getCpf() {
        return cpf;
    }

    public String getRg() {
        return rg;
    }

    public String getDocumentoEstrangeiro() {
        return documentoEstrangeiro;
    }

    public String getEmail() {
        return email;
    }

    public String getCelular() {
        return celular;
    }

    public boolean isWhatsapp() {
        return whatsapp;
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public String getEndereco() {
        return endereco;
    }

    public GrauOrdem getGrauOrdem() {
        return grauOrdem;
    }

    public String getDioceseIncardinacao() {
        return dioceseIncardinacao;
    }

    public LocalDate getDataOrdenacaoDiaconal() {
        return dataOrdenacaoDiaconal;
    }

    public LocalDate getDataOrdenacaoPresbiteral() {
        return dataOrdenacaoPresbiteral;
    }

    public LocalDate getDataOrdenacaoEpiscopal() {
        return dataOrdenacaoEpiscopal;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public Instant getAtualizadoEmUtc() {
        return atualizadoEmUtc;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }
}
