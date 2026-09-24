package br.com.nsfatima.gestao.calendario.integration.exportacao;

import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class ExportacaoCalendarioIntegrationTest {

    private static final String ORG_ID = "00000000-0000-0000-0000-0000000000aa";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private EventoJpaRepository eventRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    @DisplayName("Should export calendar events to CSV successfully")
    void deveExportarParaCsv() throws Exception {
        // Create sample event
        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Missa de Domingo Teste");
        event.setGrupoParoquialResponsavelId(UUID.fromString(ORG_ID));
        event.setStatus("CONFIRMADO");
        event.setInicioUtc(Instant.parse("2026-05-10T10:00:00Z"));
        event.setFimUtc(Instant.parse("2026-05-10T11:00:00Z"));
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/eventos/exportar")
                        .param("formato", "CSV")
                        .param("opcao", "MES")
                        .param("ano", "2026")
                        .param("mes", "5"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"calendario_mes_2026_05.csv\"")))

                .andExpect(content().string(containsString("Missa de Domingo Teste")))
                .andExpect(content().string(containsString("Título;Descrição;Categoria;Formato;Turno/Horário;")));
    }

    @Test
    @DisplayName("Should export calendar events to Excel successfully")
    void deveExportarParaExcel() throws Exception {
        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Reunião de Conselho");
        event.setGrupoParoquialResponsavelId(UUID.fromString(ORG_ID));
        event.setStatus("CONFIRMADO");
        event.setInicioUtc(Instant.parse("2026-06-01T15:00:00Z"));
        event.setFimUtc(Instant.parse("2026-06-01T17:00:00Z"));
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/eventos/exportar")
                        .param("formato", "EXCEL")
                        .param("opcao", "ANO")
                        .param("ano", "2026"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))

                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"calendario_ano_2026.xlsx\"")));

    }

    @Test
    @DisplayName("Should export calendar events to PDF successfully")
    void deveExportarParaPdf() throws Exception {
        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Festa da Padroeira");
        event.setGrupoParoquialResponsavelId(UUID.fromString(ORG_ID));
        event.setStatus("CONFIRMADO");
        event.setInicioUtc(Instant.parse("2026-05-31T09:00:00Z"));
        event.setFimUtc(Instant.parse("2026-05-31T22:00:00Z"));
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/eventos/exportar")
                        .param("formato", "PDF")
                        .param("opcao", "PERIODO")
                        .param("dataInicio", "2026-05-01T00:00:00Z")
                        .param("dataFim", "2026-06-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"calendario_periodo.pdf\"")));

    }

    @Test
    @DisplayName("Should export calendar events to PDF in GRADE mode successfully")
    void deveExportarParaPdfEmModoGrade() throws Exception {
        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Festa da Padroeira");
        event.setGrupoParoquialResponsavelId(UUID.fromString(ORG_ID));
        event.setStatus("CONFIRMADO");
        event.setInicioUtc(Instant.parse("2026-05-31T09:00:00Z"));
        event.setFimUtc(Instant.parse("2026-05-31T22:00:00Z"));
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/eventos/exportar")
                        .param("formato", "PDF")
                        .param("opcao", "MES")
                        .param("ano", "2026")
                        .param("mes", "5")
                        .param("visualizacao", "GRADE"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"));
    }

    @Test
    @DisplayName("Should export calendar events to PowerPoint successfully")
    void deveExportarParaPowerPoint() throws Exception {
        EventoEntity event = new EventoEntity();
        event.setId(UUID.randomUUID());
        event.setTitulo("Retiro Espiritual");
        event.setGrupoParoquialResponsavelId(UUID.fromString(ORG_ID));
        event.setStatus("CONFIRMADO");
        event.setInicioUtc(Instant.parse("2026-07-10T09:00:00Z"));
        event.setFimUtc(Instant.parse("2026-07-10T18:00:00Z"));
        eventRepository.save(event);

        mockMvc.perform(get("/api/v1/eventos/exportar")
                        .param("formato", "POWERPOINT")
                        .param("opcao", "MES")
                        .param("ano", "2026")
                        .param("mes", "7"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"calendario_mes_2026_07.pptx\"")));
    }

    @Test
    @DisplayName("Should export calendar events with category and organization filters")
    void deveExportarComFiltra() throws Exception {
        EventoEntity event1 = new EventoEntity();
        event1.setId(UUID.randomUUID());
        event1.setTitulo("Festa Liturgica");
        event1.setGrupoParoquialResponsavelId(UUID.fromString(ORG_ID));
        event1.setCategoria(CategoriaEvento.LITURGICO);
        event1.setStatus("CONFIRMADO");
        event1.setInicioUtc(Instant.parse("2026-05-15T18:00:00Z"));
        event1.setFimUtc(Instant.parse("2026-05-15T20:00:00Z"));
        eventRepository.save(event1);

        EventoEntity event2 = new EventoEntity();
        event2.setId(UUID.randomUUID());
        event2.setTitulo("Reuniao Administrativa");
        event2.setGrupoParoquialResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000bb"));
        event2.setCategoria(CategoriaEvento.ADMINISTRATIVO);
        event2.setStatus("CONFIRMADO");
        event2.setInicioUtc(Instant.parse("2026-05-16T18:00:00Z"));
        event2.setFimUtc(Instant.parse("2026-05-16T20:00:00Z"));
        eventRepository.save(event2);

        mockMvc.perform(get("/api/v1/eventos/exportar")
                        .param("formato", "CSV")
                        .param("opcao", "MES")
                        .param("ano", "2026")
                        .param("mes", "5")
                        .param("categorias", "LITURGICO")
                        .param("grupoParoquialIds", ORG_ID))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andExpect(content().string(containsString("Festa Liturgica")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Reuniao Administrativa"))));
    }
}
