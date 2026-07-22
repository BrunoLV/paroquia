package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ImportacaoServiceTest {

    @Mock
    private CreateMembroUseCase createMembroUseCase;

    @Mock
    private MembroJpaRepository membroJpaRepository;

    private ImportacaoService importacaoService;

    @BeforeEach
    void setUp() {
        importacaoService = new ImportacaoService(createMembroUseCase, membroJpaRepository);
    }

    @Test
    void shouldGerarCsvTemplateParaMembro() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("MEMBRO", "CSV");
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(content.contains("nomeCompleto,email,telefone,dataNascimento"));
        assertTrue(content.contains("João da Silva,joao.silva@email.com"));
    }

    @Test
    void shouldGerarXlsxTemplateParaEvento() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("EVENTO", "XLSX");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void shouldValidarArquivoCsvComSucessoEErros() throws IOException {
        String csvContent = "nomeCompleto,email,telefone\n" +
                "João Silva,joao@email.com,11999999999\n" +
                ",email_invalido,11888888888\n";

        MockMultipartFile file = new MockMultipartFile("file", "membros.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO", file);

        assertEquals(2, resultado.totalLinhas());
        assertEquals(1, resultado.totalValidos());
        assertEquals(1, resultado.totalInvalidos());
        assertFalse(resultado.itens().get(1).valido());
    }

    @Test
    void shouldValidarArquivoXlsx() throws IOException {
        byte[] xlsxBytes;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Membros");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("nomeCompleto");
            header.createCell(1).setCellValue("email");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Maria Agostinho");
            row1.createCell(1).setCellValue("maria@email.com");

            workbook.write(out);
            xlsxBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile("file", "membros.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);

        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO", file);

        assertEquals(1, resultado.totalLinhas());
        assertEquals(1, resultado.totalValidos());
        assertTrue(resultado.itens().get(0).valido());
    }
}
