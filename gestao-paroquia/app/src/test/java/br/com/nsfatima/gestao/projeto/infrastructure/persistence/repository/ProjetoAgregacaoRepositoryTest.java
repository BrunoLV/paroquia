package br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ProjetoAgregacaoRepositoryTest {

    @Autowired
    private EventoJpaRepository eventoRepository;

    @Autowired
    private ProjetoEventoJpaRepository projetoRepository;

    @Autowired
    private EventoEnvolvidoJpaRepository envolvidoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setupSchema() {
        // Ensure organizacoes table exists for the native query
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS calendario.organizacoes (id UUID PRIMARY KEY, nome VARCHAR(255))");
        jdbcTemplate.execute("DELETE FROM calendario.organizacoes");
    }

    @Test
    @DisplayName("Deve contar eventos por projeto e status corretamente")
    void deveContarEventosPorProjetoEStatus() {
        UUID projetoId = UUID.randomUUID();
        
        // Setup project
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(projetoId);
        projeto.setNome("Projeto Teste");
        projetoRepository.save(projeto);

        // Setup events
        saveEvento(projetoId, EventoStatusInput.CONFIRMADO, Instant.now().minus(1, ChronoUnit.DAYS));
        saveEvento(projetoId, EventoStatusInput.CONFIRMADO, Instant.now().plus(1, ChronoUnit.DAYS));
        saveEvento(projetoId, EventoStatusInput.RASCUNHO, Instant.now().plus(2, ChronoUnit.DAYS));

        long total = eventoRepository.countByProjetoId(projetoId);
        long confirmados = eventoRepository.countByProjetoIdAndStatus(projetoId, EventoStatusInput.CONFIRMADO.name());
        long rascunhos = eventoRepository.countByProjetoIdAndStatus(projetoId, EventoStatusInput.RASCUNHO.name());

        assertThat(total).isEqualTo(3);
        assertThat(confirmados).isEqualTo(2);
        assertThat(rascunhos).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve contar eventos realizados e pendentes por projeto e data")
    void deveContarEventosPorData() {
        UUID projetoId = UUID.randomUUID();
        Instant agora = Instant.now();

        // Setup project
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(projetoId);
        projeto.setNome("Projeto Teste");
        projetoRepository.save(projeto);

        // Realized (past)
        saveEvento(projetoId, EventoStatusInput.CONFIRMADO, agora.minus(2, ChronoUnit.DAYS));
        saveEvento(projetoId, EventoStatusInput.CONFIRMADO, agora.minus(1, ChronoUnit.DAYS));

        // Pending (future)
        saveEvento(projetoId, EventoStatusInput.CONFIRMADO, agora.plus(1, ChronoUnit.DAYS));

        long realizados = eventoRepository.countByProjetoIdAndFimUtcLessThan(projetoId, agora);
        long pendentes = eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(projetoId, agora);

        assertThat(realizados).isEqualTo(2);
        assertThat(pendentes).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve buscar nomes de organizações envolvidas no projeto")
    void deveBuscarNomesOrganizacoesEnvolvidas() {
        UUID projetoId = UUID.randomUUID();
        UUID orgProjeto = UUID.randomUUID();
        UUID orgEvento1 = UUID.randomUUID();
        UUID orgEnvolvido1 = UUID.randomUUID();

        // Setup organizations in external table
        jdbcTemplate.execute("INSERT INTO calendario.organizacoes (id, nome) VALUES ('" + orgProjeto + "', 'Pastoral do Projeto')");
        jdbcTemplate.execute("INSERT INTO calendario.organizacoes (id, nome) VALUES ('" + orgEvento1 + "', 'Pastoral do Evento')");
        jdbcTemplate.execute("INSERT INTO calendario.organizacoes (id, nome) VALUES ('" + orgEnvolvido1 + "', 'Pastoral Envolvida')");

        // Setup project
        ProjetoEventoEntity projeto = new ProjetoEventoEntity();
        projeto.setId(projetoId);
        projeto.setNome("Projeto Teste");
        projeto.setOrganizacaoResponsavelId(orgProjeto);
        projetoRepository.save(projeto);

        // Event 1 with involved
        EventoEntity evento1 = saveEvento(projetoId, EventoStatusInput.CONFIRMADO, Instant.now().plus(1, ChronoUnit.DAYS));
        evento1.setOrganizacaoResponsavelId(orgEvento1);
        eventoRepository.save(evento1);

        EventoEnvolvidoEntity envolvido = new EventoEnvolvidoEntity();
        envolvido.setEventoId(evento1.getId());
        envolvido.setOrganizacaoId(orgEnvolvido1);
        envolvidoRepository.save(envolvido);

        // Query distinct org names
        List<String> nomes = eventoRepository.findInvolvedOrganizationNames(projetoId);

        assertThat(nomes).containsExactlyInAnyOrder("Pastoral do Projeto", "Pastoral do Evento", "Pastoral Envolvida");
    }

    private EventoEntity saveEvento(UUID projetoId, EventoStatusInput status, Instant inicio) {
        UUID orgId = UUID.randomUUID();
        String uniqueNome = "Org Teste " + orgId;
        jdbcTemplate.execute("INSERT INTO calendario.organizacoes (id, nome) VALUES ('" + orgId + "', '" + uniqueNome + "')");

        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setProjetoId(projetoId);
        evento.setStatus(status.name());
        evento.setInicioUtc(inicio);
        evento.setFimUtc(inicio.plus(1, ChronoUnit.HOURS));
        evento.setTitulo("Evento");
        evento.setOrganizacaoResponsavelId(orgId);
        return eventoRepository.save(evento);
    }
}
