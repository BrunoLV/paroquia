package br.com.nsfatima.gestao.local.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocalServiceTest {

    private LocalService localService;
    private FakeLocalRepository localRepository;

    @BeforeEach
    void setUp() {
        localRepository = new FakeLocalRepository();
        localService = new LocalService(localRepository);
    }

    @Test
    void shouldCreateLocal() {
        Local created = localService.createLocal("Salão", "SALÃO", "Rua A", 100, "Wifi");
        
        assertNotNull(created.getId());
        assertEquals("Salão", created.getNome());
        assertTrue(created.isAtivo());
        assertTrue(localRepository.findById(created.getId()).isPresent());
    }

    @Test
    void shouldUpdateLocal() {
        Local local = new Local(UUID.randomUUID(), "Velho Nome", "SALA", "End", 10, "Nada", true);
        localRepository.save(local);

        Local updated = localService.updateLocal(local.getId(), "Novo Nome", "SALÃO", "Novo End", 20, "Ar", false);

        assertEquals("Novo Nome", updated.getNome());
        assertEquals(20, updated.getCapacidade());
        assertFalse(updated.isAtivo());
    }

    @Test
    void shouldThrowExceptionWhenDeletingLocalInUse() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Auditório", "AUDITORIO", "End", 50, "Som", true);
        localRepository.save(local);
        localRepository.setInUse(id, true);

        LocalBusinessException exception = assertThrows(LocalBusinessException.class, () -> localService.deleteLocal(id));
        assertTrue(exception.getMessage().contains("associated events"));
    }

    @Test
    void shouldDeleteLocalWhenNotInUse() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Auditório", "AUDITORIO", "End", 50, "Som", true);
        localRepository.save(local);
        localRepository.setInUse(id, false);

        localService.deleteLocal(id);
        assertTrue(localRepository.findById(id).isEmpty());
    }

    private static class FakeLocalRepository implements LocalRepository {
        private final List<Local> storage = new ArrayList<>();
        private final List<UUID> inUseIds = new ArrayList<>();

        @Override
        public void save(Local local) {
            storage.removeIf(l -> l.getId().equals(local.getId()));
            storage.add(local);
        }

        @Override
        public Optional<Local> findById(UUID id) {
            return storage.stream().filter(l -> l.getId().equals(id)).findFirst();
        }

        @Override
        public List<Local> findAll() {
            return new ArrayList<>(storage);
        }

        @Override
        public boolean isLocalInUse(UUID id) {
            return inUseIds.contains(id);
        }

        @Override
        public void delete(UUID id) {
            storage.removeIf(l -> l.getId().equals(id));
        }

        public void setInUse(UUID id, boolean inUse) {
            if (inUse) inUseIds.add(id);
            else inUseIds.remove(id);
        }
    }
    
    // Auxiliary assertFalse for completeness in LocalServiceTest
    private void assertFalse(boolean condition) {
        org.junit.jupiter.api.Assertions.assertFalse(condition);
    }
}
