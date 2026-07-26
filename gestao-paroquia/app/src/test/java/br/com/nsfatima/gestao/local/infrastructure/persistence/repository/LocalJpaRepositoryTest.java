package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LocalJpaRepositoryTest {

    @Autowired
    private LocalJpaRepository localRepository;

    @Autowired
    private EventoJpaRepository eventoRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistLocalWithAllFields() {
        UUID id = UUID.randomUUID();
        LocalEntity local = new LocalEntity();
        local.setId(id);
        local.setNome("Salão Paroquial");
        local.setTipo("SALÃO");
        local.setEndereco("Rua das Flores, 123");
        local.setCapacidade(200);
        local.setCaracteristicas("Possui ar condicionado e projetor");
        local.setAtivo(true);

        localRepository.save(local);
        entityManager.flush();
        entityManager.clear();

        LocalEntity found = localRepository.findById(id).orElseThrow();
        assertEquals("Salão Paroquial", found.getNome());
        assertEquals("Rua das Flores, 123", found.getEndereco());
        assertEquals(200, found.getCapacidade());
        assertEquals("Possui ar condicionado e projetor", found.getCaracteristicas());
    }

    @Test
    void shouldCheckIfLocalHasAssociatedEvents() {
        UUID localId = UUID.randomUUID();
        LocalEntity local = new LocalEntity();
        local.setId(localId);
        local.setNome("Capela");
        local.setTipo("CAPELA");
        local.setAtivo(true);
        localRepository.save(local);

        assertFalse(eventoRepository.existsByLocalId(localId));

        EventoEntity evento = new EventoEntity();
        evento.setId(UUID.randomUUID());
        evento.setTitulo("Missa");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        evento.setInicioUtc(Instant.now());
        evento.setFimUtc(Instant.now().plusSeconds(3600));
        evento.setStatus("CONFIRMADO");
        evento.setLocalId(localId);
        eventoRepository.save(evento);

        assertTrue(eventoRepository.existsByLocalId(localId));
    }
}
