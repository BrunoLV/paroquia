package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.support.infrastructure.configuration.JpaAuditingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaAuditingConfig.class)
class LocalJpaRepositoryTest {

    @Autowired
    private LocalJpaRepository localRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    @Autowired
    private EventoLocalJpaRepository eventoLocalRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void devePersistirLocalComTodosCampos() {
        UUID id = UUID.randomUUID();
        LocalEntity local = new LocalEntity();
        local.setId(id);
        local.setNome("Salão Paroquial");
        local.setTipo("SALÃO");
        local.setEndereco("Rua das Flores, 123");
        local.setCep("01001-000");
        local.setLogradouro("Rua das Flores");
        local.setNumero("123");
        local.setBairro("Centro");
        local.setCidade("São Paulo");
        local.setUf("SP");
        local.setCapacidadeMaxima(200);
        local.setDescricao("Possui ar condicionado e projetor");
        local.setAtivo(true);

        localRepository.save(local);
        entityManager.flush();
        entityManager.clear();

        LocalEntity found = localRepository.findById(id).orElseThrow();
        assertEquals("Salão Paroquial", found.getNome());
        assertEquals("01001-000", found.getCep());
        assertEquals("SP", found.getUf());
        assertEquals(200, found.getCapacidadeMaxima());
        assertEquals("Possui ar condicionado e projetor", found.getDescricao());
    }

    @Test
    void deveIgnorarLocaisComSoftDeleteNasConsultasAtivas() {
        UUID id = UUID.randomUUID();
        LocalEntity local = new LocalEntity();
        local.setId(id);
        local.setNome("Sala 01 Excluída");
        local.setTipo("SALA");
        local.setAtivo(false);
        local.setDeletedAt(Instant.now());

        localRepository.save(local);
        entityManager.flush();
        entityManager.clear();

        assertFalse(localRepository.findByIdAndDeletedAtIsNull(id).isPresent());
        assertTrue(localRepository.findAllByDeletedAtIsNull().stream().noneMatch(l -> l.getId().equals(id)));
    }

    @Test
    void deveVerificarEventosFuturosNaoCanceladosDoLocal() {
        UUID localId = UUID.randomUUID();
        LocalEntity local = new LocalEntity();
        local.setId(localId);
        local.setNome("Capela dos Santos");
        local.setTipo("CAPELA");
        local.setAtivo(true);
        localRepository.save(local);

        Instant now = Instant.now();
        assertFalse(eventoRepository.existsByLocalIdAndFimUtcGreaterThanAndStatusNot(localId, now, "CANCELADO"));

        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setTitulo("Vigília Jovem");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        evento.setInicioUtc(now.plusSeconds(3600));
        evento.setFimUtc(now.plusSeconds(7200));
        evento.setStatus("CONFIRMADO");
        evento.setLocalId(localId);
        eventoRepository.save(evento);
        entityManager.flush();

        assertTrue(eventoRepository.existsByLocalIdAndFimUtcGreaterThanAndStatusNot(localId, now, "CANCELADO"));
    }

    @Test
    void deveConsiderarEventoFuturoVinculadoSomentePelaAssociacaoMultiLocal() {
        UUID localId = UUID.randomUUID();
        LocalEntity local = new LocalEntity();
        local.setId(localId);
        local.setNome("Salão Anexo");
        local.setTipo("SALAO_PAROQUIAL");
        local.setAtivo(true);
        localRepository.save(local);

        Instant now = Instant.now();
        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setTitulo("Festa Junina");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        evento.setInicioUtc(now.plusSeconds(3600));
        evento.setFimUtc(now.plusSeconds(7200));
        evento.setStatus("CONFIRMADO");
        eventoRepository.save(evento);
        eventoLocalRepository.save(new EventoLocalEntity(evento.getId(), localId));
        entityManager.flush();
        entityManager.clear();

        assertTrue(eventoRepository.existsByLocalIdAndFimUtcGreaterThanAndStatusNot(localId, now, "CANCELADO"));
        assertTrue(eventoRepository.existsByLocalId(localId));
    }
}
