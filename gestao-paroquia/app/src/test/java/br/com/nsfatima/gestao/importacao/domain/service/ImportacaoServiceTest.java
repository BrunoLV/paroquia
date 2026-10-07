package br.com.nsfatima.gestao.importacao.domain.service;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.importacao.api.v1.dto.ImportacaoValidacaoResultadoDTO;
import br.com.nsfatima.gestao.membro.application.usecase.AdicionarMembroUseCase;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.application.service.OrganizacaoService;
import br.com.nsfatima.gestao.importacao.application.service.LeitorArquivoImportacao;
import br.com.nsfatima.gestao.importacao.application.service.ImportacaoService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ImportacaoServiceTest {

    @Mock
    private AdicionarMembroUseCase adicionarMembroUseCase;

    @Mock
    private UsuarioJpaRepository usuarioJpaRepository;

    @Mock
    private OrganizacaoService organizacaoService;

    @Mock
    private EventoJpaRepository eventoJpaRepository;

    @Mock
    private OrganizacaoJpaRepository organizacaoJpaRepository;

    @Mock
    private UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;

    @Mock
    private PastoralMovimentoOficialRepository pastoralOficialRepository;

    private ImportacaoService importacaoService;

    @BeforeEach
    void setUp() {
        importacaoService = new ImportacaoService(
                adicionarMembroUseCase,
                usuarioJpaRepository,
                organizacaoService,
                eventoJpaRepository,
                organizacaoJpaRepository,
                unidadeTerritorialJpaRepository,
                pastoralOficialRepository,
                new LeitorArquivoImportacao(),
                null,
                null,
                null,
                null
        );
    }


    @Test
    void deveGerarCsvTemplateParaMembro() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("MEMBRO", "CSV");
        String content = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(content.contains("usuarioUsername,organizacaoNome,papel,igrejaId"));
        assertTrue(content.contains("joao.silva@email.com,Pastoral da Comunicação"));
    }

    @Test
    void deveGerarXlsxTemplateParaEvento() throws IOException {
        byte[] bytes = importacaoService.gerarTemplate("EVENTO", "XLSX");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void deveValidarArquivoCsvComSucessoEErros() throws IOException {
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
    void deveValidarArquivoXlsx() throws IOException {
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
    void deveConfirmarImportacaoParaMembroEOrganizacaoEEvento() {
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
        verify(adicionarMembroUseCase).execute(any(), eq("admin"));

        Map<String, String> orgMap = Map.of(
                "nome", "Pastoral da Música",
                "tipo", "PASTORAL"
        );
        int countOrg = importacaoService.confirmarImportacao("ORGANIZACAO", List.of(orgMap), "admin");
        assertEquals(1, countOrg);
        verify(organizacaoService).criar(eq("Pastoral da Música"),
                any(), any(), any(), any(), any(), any(), any(), any());


        Map<String, String> eventoMap = Map.of(
                "nome", "Missa Dominical",
                "dataInicio", "2026-08-02 09:00"
        );
        int countEvento = importacaoService.confirmarImportacao("EVENTO", List.of(eventoMap), "admin");
        assertEquals(1, countEvento);
        verify(eventoJpaRepository).save(any());
    }

    @Test
    void testShouldRealizarResolucaoSilenciosaEmAnoFechado() {
        var anoRepoMock = mock(AnoParoquialJpaRepository.class);
        var anoFechado = new AnoParoquialEntity();
        anoFechado.setAno(2026);
        anoFechado.setStatus(AnoParoquialStatus.FECHADO);

        when(anoRepoMock.findByAno(2026)).thenReturn(Optional.of(anoFechado));

        ImportacaoService serviceComAno = new ImportacaoService(
                adicionarMembroUseCase, usuarioJpaRepository, organizacaoService,
                eventoJpaRepository, organizacaoJpaRepository, unidadeTerritorialJpaRepository,
                pastoralOficialRepository,
                new LeitorArquivoImportacao(), anoRepoMock, null, null, null
        );

        Map<String, String> eventoMap = Map.of(
                "nome", "Missa em Ano Fechado",
                "dataInicio", "2026-08-02 09:00",
                "status", "CONFIRMADO"
        );

        int count = serviceComAno.confirmarImportacao("EVENTO", List.of(eventoMap), "admin");
        assertEquals(1, count);
        verify(eventoJpaRepository, atLeastOnce()).save(argThat(entity ->
                "PENDENTE".equals(entity.getStatus())
                        && EstadoConflitoAgenda.PENDENTE.equals(entity.getEstadoConflito())
        ));
    }
}
