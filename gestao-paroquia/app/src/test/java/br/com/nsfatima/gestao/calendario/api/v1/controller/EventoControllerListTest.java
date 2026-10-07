package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class EventoControllerListTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private EventoRecorrenciaJpaRepository eventoRecorrenciaJpaRepository;

    @Autowired
    private OrganizacaoJpaRepository organizacaoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    @BeforeEach
    void setup() {
        observacaoEventoJpaRepository.deleteAll();
        aprovacaoJpaRepository.deleteAll();
        eventoEnvolvidoJpaRepository.deleteAll();
        eventoJpaRepository.findAll().forEach(e -> {
            if (e.getRecorrenciaId() != null) {
                e.setRecorrenciaId(null);
                eventoJpaRepository.save(e);
            }
        });
        eventoRecorrenciaJpaRepository.deleteAll();
        eventoJpaRepository.deleteAll();
        UUID orgId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        if (!organizacaoJpaRepository.existsById(orgId)) {
            var org = new OrganizacaoEntity();
            org.setId(orgId);
            org.setNome("Organizacao Default");
            org.setTipo(TipoOrganizacao.PASTORAL);
            org.setAtivo(true);
            organizacaoJpaRepository.save(org);
        }
    }

    @Test
    void deveListarEventosComPagination() throws Exception {
        for (int i = 0; i < 15; i++) {
            createEvento("Evento " + i, Instant.parse("2026-05-10T10:00:00Z"));
        }

        mockMvc.perform(get("/api/v1/eventos")
                        .param("page", "0")
                        .param("size", "10")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.totalElements").value(15));
    }

    @Test
    void deveFiltrarEventosPorDataRange() throws Exception {
        createEvento("Maio Event", Instant.parse("2026-05-15T10:00:00Z"));
        createEvento("Junho Event", Instant.parse("2026-06-15T10:00:00Z"));

        mockMvc.perform(get("/api/v1/eventos")
                        .param("dataInicio", "2026-05-01T00:00:00Z")
                        .param("dataFim", "2026-05-31T23:59:59Z")
                        .header("X-Actor-Role", "paroco")
                        .header("X-Actor-Org-Type", "CLERO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Maio Event"));
    }

    private void createEvento(String titulo, Instant inicio) {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(titulo);
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        entity.setInicioUtc(inicio);
        entity.setFimUtc(inicio.plusSeconds(3600));
        entity.setStatus("AGENDADO");
        eventoJpaRepository.save(entity);
    }
}
