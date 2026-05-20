package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.ListEventosUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EventoFiltragemAvancadaIntegrationTest {

    @Autowired
    private ListEventosUseCase listEventosUseCase;

    @Autowired
    private EventoJpaRepository repository;

    @Autowired
    private EventoEnvolvidoJpaRepository envolvidoRepository;

    private UUID orgA = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
    private UUID orgB = UUID.fromString("00000000-0000-0000-0000-0000000000bb");

    @BeforeEach
    void setup() {
        repository.deleteAll();
        
        // Evento 1: Org A Responsavel, Categoria LITURGICO, Status CONFIRMADO
        EventoEntity e1 = createEvent("E1", orgA, CategoriaEvento.LITURGICO, EventoStatusInput.CONFIRMADO);
        repository.save(e1);

        // Evento 2: Org B Responsavel, Org A Envolvida, Categoria PASTORAL, Status RASCUNHO
        EventoEntity e2 = createEvent("E2", orgB, CategoriaEvento.PASTORAL, EventoStatusInput.RASCUNHO);
        repository.save(e2);
        EventoEnvolvidoEntity ee = new EventoEnvolvidoEntity();
        ee.setEventoId(e2.getId());
        ee.setOrganizacaoId(orgA);
        ee.setPapelParticipacao(PapelEnvolvido.APOIO);
        envolvidoRepository.save(ee);

        // Evento 3: Org B Responsavel, Categoria SOCIAL, Status CANCELADO
        EventoEntity e3 = createEvent("E3", orgB, CategoriaEvento.SOCIAL, EventoStatusInput.CANCELADO);
        repository.save(e3);
    }

    @Test
    void shouldFilterByEnvolvidoId() {
        // Org A is responsible for E1 and involved in E2
        EventoFiltroRequest filter = new EventoFiltroRequest(null, null, null, null, orgA, null, null);
        Page<EventoResponse> result = listEventosUseCase.execute(filter, PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(EventoResponse::titulo).containsExactlyInAnyOrder("E1", "E2");
    }

    @Test
    void shouldFilterByMultipleCategories() {
        EventoFiltroRequest filter = new EventoFiltroRequest(null, null, null, null, null, 
                List.of(CategoriaEvento.LITURGICO, CategoriaEvento.SOCIAL), null);
        Page<EventoResponse> result = listEventosUseCase.execute(filter, PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(EventoResponse::titulo).containsExactlyInAnyOrder("E1", "E3");
    }

    @Test
    void shouldFilterByMultipleStatuses() {
        EventoFiltroRequest filter = new EventoFiltroRequest(null, null, null, null, null, null, 
                List.of(EventoStatusInput.RASCUNHO, EventoStatusInput.CANCELADO));
        Page<EventoResponse> result = listEventosUseCase.execute(filter, PageRequest.of(0, 10));
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(EventoResponse::titulo).containsExactlyInAnyOrder("E2", "E3");
    }

    private EventoEntity createEvent(String titulo, UUID orgId, CategoriaEvento cat, EventoStatusInput status) {
        EventoEntity e = new EventoEntity();
        e.setId(UUID.randomUUID());
        e.setTitulo(titulo);
        e.setOrganizacaoResponsavelId(orgId);
        e.setCategoria(cat.name());
        e.setStatus(status.name());
        e.setInicioUtc(Instant.now());
        e.setFimUtc(Instant.now().plusSeconds(3600));
        e.setConflictState("NO_CONFLICT");
        return e;
    }
}
