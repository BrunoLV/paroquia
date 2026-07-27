package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.membro.application.usecase.CreateMembroUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.domain.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportacaoServiceTest {

    @Mock
    private CreateMembroUseCase createMembroUseCase;

    @Mock
    private MembroJpaRepository membroJpaRepository;

    @Mock
    private UsuarioJpaRepository usuarioJpaRepository;

    @Mock
    private OrganizacaoService organizacaoService;

    @Mock
    private EventoJpaRepository eventoJpaRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OrganizacaoJpaRepository organizacaoJpaRepository;

    @Mock
    private PastoralMovimentoOficialRepository pastoralOficialRepository;

    private ImportacaoService importacaoService;

    @BeforeEach
    void setUp() {
        importacaoService = new ImportacaoService(
                createMembroUseCase,
                membroJpaRepository,
                usuarioJpaRepository,
                organizacaoService,
                eventoJpaRepository,
                passwordEncoder,
                organizacaoJpaRepository,
                pastoralOficialRepository,
                new ImportacaoFileParser()
        );
    }


    @Test
    void shouldGerarCsvTemplateParaMembro() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("MEMBRO", "CSV");
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(content.contains("usuarioUsername,organizacaoNome,papel,igrejaId"));
        assertTrue(content.contains("joao.silva@email.com,Pastoral da Comunicação"));
    }

    @Test
    void shouldGerarXlsxTemplateParaEvento() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("EVENTO", "XLSX");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void shouldValidarArquivoCsvComSucessoEErros() throws IOException {
        when(usuarioJpaRepository.findByUsernameIgnoreCase("joao@email.com"))
                .thenReturn(Optional.of(new UsuarioEntity()));
        when(organizacaoJpaRepository.findByNomeIgnoreCase("Pastoral da Música"))
                .thenReturn(Optional.of(new OrganizacaoEntity()));

        String csvContent = """
                usuarioUsername,organizacaoNome,papel
                joao@email.com,Pastoral da Música,COORDENADOR
                desconhecido@email.com,Inexistente,MEMBRO
                """;

        MockMultipartFile file = new MockMultipartFile("file",
                "membros.csv", "text/csv", csvContent.getBytes(StandardCharsets.UTF_8));


        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO", file);

        assertEquals(2, resultado.totalLinhas());
        assertEquals(1, resultado.totalValidos());
        assertEquals(1, resultado.totalInvalidos());
        assertFalse(resultado.itens().get(1).valido());
    }

    @Test
    void shouldValidarArquivoXlsx() throws IOException {
        when(usuarioJpaRepository.findByUsernameIgnoreCase("maria@email.com"))
                .thenReturn(Optional.of(new UsuarioEntity()));
        when(organizacaoJpaRepository.findByNomeIgnoreCase("Pastoral da Catequese"))
                .thenReturn(Optional.of(new OrganizacaoEntity()));

        byte[] xlsxBytes;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Membros");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("usuarioUsername");
            header.createCell(1).setCellValue("organizacaoNome");
            header.createCell(2).setCellValue("papel");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("maria@email.com");
            row1.createCell(1).setCellValue("Pastoral da Catequese");
            row1.createCell(2).setCellValue("MEMBRO");

            workbook.write(out);
            xlsxBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile("file",
                "membros.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", xlsxBytes);


        ImportacaoValidacaoResultadoDTO resultado = importacaoService.validarArquivo("MEMBRO", file);

        assertEquals(1, resultado.totalLinhas());
        assertEquals(1, resultado.totalValidos());
        assertTrue(resultado.itens().getFirst().valido());
    }

    @Test
    void shouldConfirmarImportacaoParaMembroEUsuarioEOrganizacaoEEvento() {
        var user = new UsuarioEntity();
        user.setId(UUID.randomUUID());
        var org = new OrganizacaoEntity();
        org.setId(UUID.randomUUID());

        when(usuarioJpaRepository.findByUsernameIgnoreCase("joao@email.com"))
                .thenReturn(Optional.of(user));
        when(organizacaoJpaRepository.findByNomeIgnoreCase("Pastoral da Música"))
                .thenReturn(Optional.of(org));

        Map<String, String> membroMap = Map.of(
                "usuarioUsername", "joao@email.com",
                "organizacaoNome", "Pastoral da Música",
                "papel", "COORDENADOR"
        );
        int countMembro = importacaoService.confirmarImportacao("MEMBRO", List.of(membroMap), "admin");
        assertEquals(1, countMembro);
        verify(createMembroUseCase).execute(any(), eq("admin"));

        Map<String, String> usuarioMap = Map.of(
                "nome", "Carlos Eduardo",
                "username", "carlos.eduardo@email.com"
        );
        int countUsuario = importacaoService.confirmarImportacao("USUARIO", List.of(usuarioMap), "admin");
        assertEquals(1, countUsuario);
        verify(usuarioJpaRepository).save(any());

        Map<String, String> orgMap = Map.of(
                "nome", "Pastoral da Música",
                "tipo", "PASTORAL"
        );
        int countOrg = importacaoService.confirmarImportacao("ORGANIZACAO", List.of(orgMap), "admin");
        assertEquals(1, countOrg);
        verify(organizacaoService).createOrganization(eq("Pastoral da Música"),
                any(), any(), any(), any(), any(), any());


        Map<String, String> eventoMap = Map.of(
                "nome", "Missa Dominical",
                "dataInicio", "2026-08-02 09:00"
        );
        int countEvento = importacaoService.confirmarImportacao("EVENTO", List.of(eventoMap), "admin");
        assertEquals(1, countEvento);
        verify(eventoJpaRepository).save(any());
    }
}
