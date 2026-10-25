package br.com.nsfatima.gestao.conselho.integration;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniaoId;
import br.com.nsfatima.gestao.conselho.domain.model.ConflitoInteresse;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.model.RegistroAuditoriaAta;
import br.com.nsfatima.gestao.conselho.domain.model.ResultadoDeliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.Votacao;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaHistoricoRepository;
import br.com.nsfatima.gestao.conselho.domain.repository.AtaRepository;
import br.com.nsfatima.gestao.conselho.domain.repository.FiltroAtas;
import br.com.nsfatima.gestao.conselho.domain.repository.PaginaAtas;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.AGORA;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.FIEL_A;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.conteudo;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.item;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.tresPresentes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Persistência das atas em PostgreSQL real: ida e volta do conteúdo, numeração, escopo e triggers. */
@SpringBootTest
@Transactional
class AtaRepositoryIntegrationTest {

    @Autowired
    private AtaRepository repository;
    @Autowired
    private AtaHistoricoRepository historico;
    @Autowired
    private JdbcTemplate jdbc;

    private ReferenciaOrganizacao novoConselho(SubtipoConselho subtipo) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO calendario.organizacao (id, nome, tipo, subtipo_conselho) "
                + "VALUES (?, ?, 'CONSELHO', ?)", id, "Conselho " + id, subtipo.name());
        return new ReferenciaOrganizacao(id, TipoOrganizacao.CONSELHO, subtipo);
    }

    private AtaReuniao novaAta(ReferenciaOrganizacao cpp) {
        Deliberacao deliberacao = new Deliberacao(1, "Aprovar a reforma", ResultadoDeliberacao.PARECER_FAVORAVEL,
                new Votacao(1, 0, 0), List.of(new ConflitoInteresse(FIEL_A, "Parente do fornecedor")));
        return AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP, cpp, null,
                conteudo(tresPresentes(), item(1, deliberacao)), "secretario-1", AGORA);
    }

    @Test
    @DisplayName("grava e recupera a ata com pauta, deliberações, votação e conflitos")
    void idaEVolta() {
        ReferenciaOrganizacao cpp = novoConselho(SubtipoConselho.CPP);
        AtaReuniao ata = novaAta(cpp);

        repository.salvar(ata);
        AtaReuniao lida = repository.buscarPorId(ata.getId()).orElseThrow();

        assertThat(lida.getConteudo()).isEqualTo(ata.getConteudo());
        assertThat(lida.getTipo()).isEqualTo(TipoReuniao.REUNIAO_CPP);
        assertThat(lida.getStatus()).isEqualTo(StatusAta.RASCUNHO);
        assertThat(lida.getOrganizacaoConvocanteId()).isEqualTo(cpp.id());
    }

    @Test
    @DisplayName("atualiza o rascunho existente em vez de duplicar")
    void atualizaRascunho() {
        AtaReuniao ata = novaAta(novoConselho(SubtipoConselho.CPP));
        repository.salvar(ata);

        ata.submeter(5, repository, Instant.parse("2026-06-02T12:00:00Z"));
        repository.salvar(ata);

        assertThat(repository.buscarPorId(ata.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusAta.PENDENTE_HOMOLOGACAO);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM calendario.ata_reuniao WHERE id = ?", Integer.class,
                ata.getId().value())).isEqualTo(1);
    }

    @Test
    @DisplayName("numera em sequência por organização e ano")
    void numeracao() {
        ReferenciaOrganizacao cpp = novoConselho(SubtipoConselho.CPP);
        AtaReuniao primeira = novaAta(cpp);
        repository.salvar(primeira);
        primeira.submeter(5, repository, AGORA);
        primeira.homologar("paroco", repository, AGORA);
        repository.salvar(primeira);

        assertThat(primeira.identificacao()).contains("001/2026");
        assertThat(repository.proximo(cpp.id(), 2026)).isEqualTo(2);
        assertThat(repository.proximo(cpp.id(), 2027)).isEqualTo(1);
        assertThat(repository.proximo(UUID.randomUUID(), 2026)).isEqualTo(1);
    }

    @Test
    @DisplayName("lista respeitando o escopo de organizações do ator")
    void listaComEscopo() {
        ReferenciaOrganizacao a = novoConselho(SubtipoConselho.CPP);
        ReferenciaOrganizacao b = novoConselho(SubtipoConselho.CPP);
        AtaReuniao ataA = repository.salvar(novaAta(a));
        repository.salvar(novaAta(b));

        PaginaAtas soDeA = repository.listar(
                new FiltroAtas(false, Set.of(a.id()), null, null, Set.of(), null, null), 0, 10);
        PaginaAtas semAcesso = repository.listar(
                new FiltroAtas(false, Set.of(), null, null, Set.of(), null, null), 0, 10);
        PaginaAtas irrestrito = repository.listar(
                new FiltroAtas(true, Set.of(), a.id(), TipoReuniao.REUNIAO_CPP, Set.of(StatusAta.RASCUNHO), null,
                        null), 0, 10);

        assertThat(soDeA.itens()).extracting(AtaReuniao::getId).containsExactly(ataA.getId());
        assertThat(soDeA.total()).isEqualTo(1);
        assertThat(semAcesso.itens()).isEmpty();
        assertThat(irrestrito.itens()).extracting(AtaReuniao::getId).containsExactly(ataA.getId());
    }

    @Test
    @DisplayName("o banco veda qualquer alteração e a exclusão de ata selada")
    void bancoVedaAlteracaoDeAtaSelada() {
        AtaReuniao ata = novaAta(novoConselho(SubtipoConselho.CPP));
        repository.salvar(ata);
        ata.submeter(5, repository, AGORA);
        ata.homologar("paroco", repository, AGORA);
        repository.salvar(ata);

        assertThatThrownBy(() -> jdbc.update("UPDATE calendario.ata_reuniao SET titulo = 'adulterado' WHERE id = ?",
                ata.getId().value())).isInstanceOf(DataAccessException.class).hasMessageContaining("imutavel");
    }

    @Test
    @DisplayName("o banco veda a exclusão de ata, mesmo rascunho")
    void bancoVedaExclusao() {
        AtaReuniao ata = novaAta(novoConselho(SubtipoConselho.CPP));
        repository.salvar(ata);

        assertThatThrownBy(() -> jdbc.update("DELETE FROM calendario.ata_reuniao WHERE id = ?", ata.getId().value()))
                .isInstanceOf(DataAccessException.class).hasMessageContaining("imutaveis");
    }

    @Test
    @DisplayName("a trilha é append-only: inclui, lista em ordem e não aceita alteração")
    void trilhaAppendOnly() {
        AtaReuniao ata = novaAta(novoConselho(SubtipoConselho.CPP));
        repository.salvar(ata);
        UUID registroId = UUID.randomUUID();

        historico.registrar(new RegistroAuditoriaAta(registroId, ata.getId(), "RASCUNHAR", null, "secretario-1",
                "SECRETARIO_CONSELHO", "127.0.0.1", null, "{}", AGORA));
        historico.registrar(new RegistroAuditoriaAta(UUID.randomUUID(), ata.getId(), "EXPORTAR_PDF", null, "paroco",
                "PAROCO", "127.0.0.1", null, "{\"layout\":\"ESTRUTURADA\"}", AGORA.plusSeconds(60)));

        assertThat(historico.listarPorAta(ata.getId())).extracting(RegistroAuditoriaAta::acao)
                .containsExactly("RASCUNHAR", "EXPORTAR_PDF");
        assertThatThrownBy(() -> jdbc.update("UPDATE calendario.ata_historico SET ator = 'x' WHERE id = ?",
                registroId)).isInstanceOf(DataAccessException.class);
    }
}
