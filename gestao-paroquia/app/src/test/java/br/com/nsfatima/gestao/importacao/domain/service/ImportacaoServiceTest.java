package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoConfirmacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoLinhaDTO;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.importacao.application.service.ImportacaoLinhaExecutor;
import br.com.nsfatima.gestao.importacao.application.service.ImportacaoService;
import br.com.nsfatima.gestao.importacao.application.service.LeitorArquivoImportacao;
import br.com.nsfatima.gestao.importacao.domain.model.DesfechoLinha;
import br.com.nsfatima.gestao.importacao.domain.model.TipoEntidadeImportacao;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportacaoServiceTest {

    @Mock
    private ImportacaoLinhaExecutor linhaExecutor;

    private ImportacaoService importacaoService;

    @BeforeEach
    void setUp() {
        importacaoService = new ImportacaoService(new LeitorArquivoImportacao(), linhaExecutor);
    }

    private static MockMultipartFile csv(String conteudo) {
        return new MockMultipartFile("file", "membros.csv", "text/csv", conteudo.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void modeloDeMembroNaoSugerePapeisProibidosNemInvalidos() throws IOException {
        String content = new String(importacaoService.gerarTemplate("MEMBRO", "CSV"), StandardCharsets.UTF_8);

        assertThat(content).contains("usuarioUsername,organizacaoNome,papel,igrejaId");
        assertThat(content).contains("MEMBRO_ATIVO").contains("VOLUNTARIO");
        assertThat(content).doesNotContain("COORDENADOR");
    }

    @Test
    void modeloXlsxDeEventoTemColunaDeJustificativa() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("EVENTO", "XLSX");

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getSheetAt(0).getRow(0).getLastCellNum()).isEqualTo((short) 9);
        }
    }

    @Test
    void validarUsaOMesmoCaminhoDoExecutorEApontaALinhaDoArquivo() throws IOException {
        lenient().doThrow(new IllegalArgumentException("Usuário não encontrado: x"))
                .when(linhaExecutor).validar(any(), argThat(d -> "x".equals(d.get("usuarioUsername"))));

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO", csv("""
                usuarioUsername,organizacaoNome,papel
                joao,Pastoral da Música,MEMBRO_ATIVO
                x,Pastoral da Música,MEMBRO_ATIVO
                """));

        assertThat(resultado.totalLinhas()).isEqualTo(2);
        assertThat(resultado.totalValidos()).isEqualTo(1);
        assertThat(resultado.totalInvalidos()).isEqualTo(1);
        assertThat(resultado.itens().get(1).linha()).isEqualTo(3);
        assertThat(resultado.itens().get(1).mensagemErro()).isEqualTo("Usuário não encontrado: x");
        verify(linhaExecutor, never()).processar(any(), any());
    }

    @Test
    void validarEscondeDetalheInternoDeFalhaInesperada() throws IOException {
        doThrow(new IllegalStateException("could not execute statement [insert into ...]"))
                .when(linhaExecutor).validar(any(), any());

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO",
                csv("usuarioUsername\njoao\n"));

        assertThat(resultado.itens().getFirst().mensagemErro()).doesNotContain("insert");
    }

    @Test
    void validarLeXlsx() throws IOException {
        byte[] xlsxBytes;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Membros");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("usuarioUsername");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("maria");
            workbook.write(out);
            xlsxBytes = out.toByteArray();
        }
        MockMultipartFile file = new MockMultipartFile("file", "membros.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO", file);

        assertThat(resultado.totalValidos()).isEqualTo(1);
        verify(linhaExecutor).validar(TipoEntidadeImportacao.MEMBRO, Map.of("usuarioUsername", "maria"));
    }

    @Test
    void validarRejeitaTipoDesconhecidoArquivoVazioGrandeOuComLinhasDemais() {
        assertThatThrownBy(() -> importacaoService.validarArquivo("INEXISTENTE", csv("a\n1\n")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Tipo de importação desconhecido");
        assertThatThrownBy(() -> importacaoService.validarArquivo("MEMBRO", csv("")))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("vazio");

        MockMultipartFile grande = new MockMultipartFile("file", "m.csv", "text/csv",
                new byte[(int) ImportacaoService.TAMANHO_MAXIMO_BYTES + 1]);
        assertThatThrownBy(() -> importacaoService.validarArquivo("MEMBRO", grande))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("tamanho máximo");

        StringBuilder linhas = new StringBuilder("usuarioUsername\n");
        for (int i = 0; i <= LeitorArquivoImportacao.MAX_LINHAS; i++) {
            linhas.append("u").append(i).append('\n');
        }
        assertThatThrownBy(() -> importacaoService.validarArquivo("MEMBRO", csv(linhas.toString())))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("linhas");
    }

    @Test
    void confirmarIsolaAFalhaDeUmaLinhaEDevolveOsErrosPorLinha() {
        Map<String, String> boa = Map.of("nome", "A");
        Map<String, String> ruim = Map.of("nome", "B");
        Map<String, String> pendente = Map.of("nome", "C");
        when(linhaExecutor.processar(TipoEntidadeImportacao.EVENTO, boa)).thenReturn(DesfechoLinha.IMPORTADA);
        when(linhaExecutor.processar(TipoEntidadeImportacao.EVENTO, ruim))
                .thenThrow(new IllegalArgumentException("Organização responsável não encontrada."));
        when(linhaExecutor.processar(TipoEntidadeImportacao.EVENTO, pendente))
                .thenReturn(DesfechoLinha.AGUARDANDO_APROVACAO);

        ImportacaoConfirmacaoResultadoDTO resultado = importacaoService.confirmarImportacao("evento", List.of(
                new ImportacaoLinhaDTO(2, boa), new ImportacaoLinhaDTO(3, ruim), new ImportacaoLinhaDTO(4, pendente)));

        assertThat(resultado.registrosImportados()).isEqualTo(1);
        assertThat(resultado.enviadosParaAprovacao()).isEqualTo(1);
        assertThat(resultado.erros()).singleElement().satisfies(erro -> {
            assertThat(erro.linha()).isEqualTo(3);
            assertThat(erro.mensagem()).isEqualTo("Organização responsável não encontrada.");
        });
    }

    @Test
    void confirmarNaoVazaDetalheInternoNemTrataTipoDesconhecido() {
        Map<String, String> dados = Map.of("nome", "A");
        when(linhaExecutor.processar(TipoEntidadeImportacao.ORGANIZACAO, dados))
                .thenThrow(new IllegalStateException("duplicate key value violates unique constraint"));

        ImportacaoConfirmacaoResultadoDTO resultado = importacaoService.confirmarImportacao("ORGANIZACAO",
                List.of(new ImportacaoLinhaDTO(2, dados)));

        assertThat(resultado.erros().getFirst().mensagem()).doesNotContain("constraint");
        assertThatThrownBy(() -> importacaoService.confirmarImportacao("X", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
