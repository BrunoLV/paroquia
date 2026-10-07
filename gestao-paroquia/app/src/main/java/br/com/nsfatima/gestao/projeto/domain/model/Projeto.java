package br.com.nsfatima.gestao.projeto.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz de um Projeto pastoral.
 *
 * <p>Concentra a máquina de estados {@code ABERTO -> EM_ANDAMENTO -> CONCLUIDO}
 * ({@code CANCELADO} a partir de {@code ABERTO} ou {@code EM_ANDAMENTO}; {@code CONCLUIDO} e
 * {@code CANCELADO} são terminais) e a invariante {@code fim >= inicio}, que antes viviam como
 * {@code setStatus(...)} cru e blocos de {@code if} espalhados por {@code AtualizarProjetoUseCase} e
 * {@code TransicionarProjetoUseCase}.</p>
 */
public class Projeto {

    private final UUID id;
    private String nome;
    private String descricao;
    private UUID organizacaoResponsavelId;
    private Instant inicio;
    private Instant fim;
    private ProjetoStatus status;

    public Projeto(
            UUID id,
            String nome,
            String descricao,
            UUID organizacaoResponsavelId,
            Instant inicio,
            Instant fim,
            ProjetoStatus status) {
        this.id = Objects.requireNonNull(id, "ID do projeto é obrigatório.");
        this.nome = exigirNaoBranco(nome);
        this.descricao = descricao;
        this.organizacaoResponsavelId = organizacaoResponsavelId;
        this.status = Objects.requireNonNull(status, "Status do projeto é obrigatório.");
        aplicarPeriodo(inicio, fim);
    }

    /** Abre um novo projeto no status {@code ABERTO}. */
    public static Projeto abrir(
            UUID id,
            String nome,
            String descricao,
            UUID organizacaoResponsavelId,
            Instant inicio,
            Instant fim) {
        return new Projeto(id, nome, descricao, organizacaoResponsavelId, inicio, fim, ProjetoStatus.ABERTO);
    }

    public void iniciar() {
        if (this.status != ProjetoStatus.ABERTO) {
            throw new IllegalStateException("Somente projetos ABERTO podem ser iniciados");
        }
        this.status = ProjetoStatus.EM_ANDAMENTO;
    }

    public void concluir() {
        if (this.status != ProjetoStatus.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente projetos EM_ANDAMENTO podem ser concluídos");
        }
        this.status = ProjetoStatus.CONCLUIDO;
    }

    public void cancelar() {
        if (this.status != ProjetoStatus.ABERTO && this.status != ProjetoStatus.EM_ANDAMENTO) {
            throw new IllegalStateException("Somente projetos ABERTO ou EM_ANDAMENTO podem ser cancelados");
        }
        this.status = ProjetoStatus.CANCELADO;
    }

    public void renomear(String novoNome) {
        this.nome = exigirNaoBranco(novoNome);
    }

    public void alterarDescricao(String novaDescricao) {
        this.descricao = novaDescricao;
    }

    public void transferirResponsavel(UUID novoOrganizacaoResponsavelId) {
        this.organizacaoResponsavelId = novoOrganizacaoResponsavelId;
    }

    public void redefinirPeriodo(Instant novoInicio, Instant novoFim) {
        aplicarPeriodo(novoInicio, novoFim);
    }

    private void aplicarPeriodo(Instant novoInicio, Instant novoFim) {
        if (novoInicio != null && novoFim != null && novoFim.isBefore(novoInicio)) {
            throw new IllegalArgumentException("Data de fim do projeto não pode ser anterior à data de início");
        }
        this.inicio = novoInicio;
        this.fim = novoFim;
    }

    private static String exigirNaoBranco(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do projeto é obrigatório.");
        }
        return nome;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getOrganizacaoResponsavelId() {
        return organizacaoResponsavelId;
    }

    public Instant getInicio() {
        return inicio;
    }

    public Instant getFim() {
        return fim;
    }

    public ProjetoStatus getStatus() {
        return status;
    }
}
