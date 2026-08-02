package br.com.nsfatima.gestao.local.domain.service;

import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class LocalServiceTest {

    private LocalService localService;
    private FakeLocalRepository localRepository;

    @BeforeEach
    void setUp() {
        localRepository = new FakeLocalRepository();
        localService = new LocalService(localRepository);
    }

    @Test
    void deveCriarLocalComOrganizacao() {
        UUID orgId = UUID.randomUUID();
        Local created = localService.createLocal("Salão", "SALÃO", "Rua A", 100, "Wifi", orgId);

        assertNotNull(created.getId());
        assertEquals("Salão", created.getNome());
        assertEquals(orgId, created.getOrganizacaoId());
        assertTrue(created.isAtivo());
        assertTrue(localRepository.findById(created.getId()).isPresent());
    }

    @Test
    void deveCriarLocal() {
        Local created = localService.createLocal("Salão", "SALÃO", "Rua A", 100, "Wifi");

        assertNotNull(created.getId());
        assertEquals("Salão", created.getNome());
        assertTrue(created.isAtivo());
        assertTrue(localRepository.findById(created.getId()).isPresent());
    }

    @Test
    void deveAtualizarLocal() {
        UUID orgId = UUID.randomUUID();
        Local local = new Local(UUID.randomUUID(), "Velho Nome", "SALA", "End", 10, "Nada", true);
        localRepository.save(local);

        Local updated = localService.updateLocal(local.getId(), "Novo Nome", "SALÃO", "Novo End", 20, "Ar", false, orgId);

        assertEquals("Novo Nome", updated.getNome());
        assertEquals(20, updated.getCapacidade());
        assertEquals(orgId, updated.getOrganizacaoId());
        assertFalse(updated.isAtivo());
    }

    @Test
    void deveInativarLocalEmVezDeDeExcluindo() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Auditório", "AUDITORIO", "End", 50, "Som", true);
        localRepository.save(local);
        localRepository.setInUse(id, false);

        localService.deleteLocal(id);

        Optional<Local> found = localRepository.findById(id);
        assertTrue(found.isPresent());
        assertFalse(found.get().isAtivo());
    }

    @Test
    void deveInativarLocalMesmoQuandoEmUso() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Auditório", "AUDITORIO", "End", 50, "Som", true);
        localRepository.save(local);
        localRepository.setInUse(id, true);

        localService.deleteLocal(id);

        Optional<Local> found = localRepository.findById(id);
        assertTrue(found.isPresent());
        assertFalse(found.get().isAtivo());
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
        public List<Local> findByOrganizacaoId(UUID organizacaoId) {
            return storage.stream().filter(l -> organizacaoId != null && organizacaoId.equals(l.getOrganizacaoId())).toList();
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
            if (inUse) {
                inUseIds.add(id);
            } else {
                inUseIds.remove(id);
            }
        }
    }
}
