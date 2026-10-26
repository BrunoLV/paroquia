package br.com.nsfatima.gestao.importacao.application.service;

import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoConfirmacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoLinhaDTO;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova contra PostgreSQL real (Testcontainers) o isolamento transacional por linha de RN-IMP-01: uma linha que
 * só falha ao gravar (nome duplicado, detectado pela constraint única no commit) não derruba as demais nem gera
 * {@code UnexpectedRollbackException}.
 */
@SpringBootTest
@WithMockUser(roles = "ADMIN")
class ImportacaoConfirmacaoIntegrationTest {

    @Autowired
    private ImportacaoService importacaoService;

    @Autowired
    private OrganizacaoJpaRepository organizacaoJpaRepository;

    @Autowired
    private UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;

    private UUID novaMatriz() {
        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(UUID.randomUUID());
        matriz.setNome("Matriz Importação " + matriz.getId());
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setAtivo(true);
        return unidadeTerritorialJpaRepository.save(matriz).getId();
    }

    private Map<String, String> conselho(String nome, UUID matrizId) {
        return Map.of("nome", nome, "tipo", "CONSELHO", "subtipoConselho", "CPP", "parentId", matrizId.toString());
    }

    @Test
    void falhaDeGravacaoDeUmaLinhaNaoPerdeAsDemais() {
        UUID matriz = novaMatriz();
        String sufixo = UUID.randomUUID().toString();
        String primeiro = "Conselho Importado A " + sufixo;
        String ultimo = "Conselho Importado B " + sufixo;

        ImportacaoConfirmacaoResultadoDTO resultado = importacaoService.confirmarImportacao("ORGANIZACAO", List.of(
                new ImportacaoLinhaDTO(2, conselho(primeiro, matriz)),
                new ImportacaoLinhaDTO(3, conselho(primeiro, matriz)),
                new ImportacaoLinhaDTO(4, conselho(ultimo, matriz))));

        assertThat(resultado.erros()).as("erros: %s", resultado.erros()).hasSize(1);
        assertThat(resultado.registrosImportados()).isEqualTo(2);
        assertThat(resultado.erros()).singleElement()
                .satisfies(erro -> assertThat(erro.linha()).isEqualTo(3));
        assertThat(organizacaoJpaRepository.findByNomeIgnoreCase(primeiro)).isPresent();
        assertThat(organizacaoJpaRepository.findByNomeIgnoreCase(ultimo)).isPresent();
    }

    @Test
    void linhaInvalidaNaConfirmacaoViraErroSemGravar() {
        String nome = "Conselho Rejeitado " + UUID.randomUUID();

        ImportacaoConfirmacaoResultadoDTO resultado = importacaoService.confirmarImportacao("ORGANIZACAO", List.of(
                new ImportacaoLinhaDTO(2, Map.of("nome", nome, "tipo", "INEXISTENTE"))));

        assertThat(resultado.registrosImportados()).isZero();
        assertThat(resultado.erros()).singleElement()
                .satisfies(erro -> assertThat(erro.mensagem()).contains("'tipo'"));
        assertThat(organizacaoJpaRepository.findByNomeIgnoreCase(nome)).isEmpty();
    }
}
