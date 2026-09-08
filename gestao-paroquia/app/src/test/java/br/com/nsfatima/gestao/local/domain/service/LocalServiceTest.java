package br.com.nsfatima.gestao.local.domain.service;

import br.com.nsfatima.gestao.local.domain.exception.LocalBusinessException;
import br.com.nsfatima.gestao.local.domain.model.EnderecoLocal;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalServiceTest {

    private LocalService localService;
    private FakeLocalRepository localRepository;

    @BeforeEach
    void setUp() {
        localRepository = new FakeLocalRepository();
        localService = new LocalService(localRepository);
    }

    @Test
    void deveCriarLocalComGrupoParoquial() {
        UUID orgId = UUID.randomUUID();
        Local created = localService.createLocal("Salão", "SALÃO", "Rua A", 100, "Wifi", orgId);

        assertNotNull(created.getId());
        assertEquals("Salão", created.getNome());
        assertEquals(orgId, created.getGrupoParoquialId());
        assertTrue(created.isAtivo());
        assertTrue(localRepository.findById(created.getId()).isPresent());
    }

    @Test
    void deveCriarLocalCompleto() {
        UUID igrejaId = UUID.randomUUID();
        EnderecoLocal endereco = new EnderecoLocal(
                "01001-000", "Praça da Sé", "100", "Sala 1", "Sé", "São Paulo", "SP");
        Local created = localService.createLocalCompleto("Auditório Bento XVI", "AUDITORIO", "Praça da Sé, 100",
                endereco, 250, "Projetor e ar condicionado", igrejaId);

        assertNotNull(created.getId());
        assertEquals("Auditório Bento XVI", created.getNome());
        assertEquals(250, created.getCapacidadeMaxima());
        assertEquals("01001-000", created.getEnderecoLocal().cep());
        assertEquals("SP", created.getEnderecoLocal().uf());
        assertTrue(created.isAtivo());
        assertFalse(created.isExcluido());
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

        Local updated = localService.updateLocal(local.getId(), "Novo Nome", "SALÃO", "Novo End", 20, "Ar", false,
                orgId);

        assertEquals("Novo Nome", updated.getNome());
        assertEquals(20, updated.getCapacidade());
        assertEquals(orgId, updated.getGrupoParoquialId());
        assertFalse(updated.isAtivo());
    }

    @Test
    void deveExcluirLogicamenteLocalSemEventosFuturos() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Auditório", "AUDITORIO", "End", 50, "Som", true);
        localRepository.save(local);
        localRepository.setFutureEvents(id, false);

        localService.deleteLocal(id);

        Optional<Local> found = localRepository.findById(id);
        assertTrue(found.isPresent());
        assertFalse(found.get().isAtivo());
        assertTrue(found.get().isExcluido());
        assertNotNull(found.get().getDeletedAt());
    }

    @Test
    void naoDevePermitirExcluirLocalComEventosFuturos() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Salão São Pedro", "SALAO", "End", 150, "Som", true);
        localRepository.save(local);
        localRepository.setFutureEvents(id, true);

        LocalBusinessException ex = assertThrows(LocalBusinessException.class, () -> localService.deleteLocal(id));
        assertTrue(ex.getMessage().contains("eventos futuros"));
    }

    @Test
    void naoDevePermitirInativarLocalComEventosFuturos() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Salão São Pedro", "SALAO", "End", 150, "Som", true);
        localRepository.save(local);
        localRepository.setFutureEvents(id, true);

        LocalBusinessException ex = assertThrows(LocalBusinessException.class,
                () -> localService.updateLocal(id, "Salão São Pedro", "SALAO", "End", 150, "Som", false));
        assertTrue(ex.getMessage().contains("eventos futuros"));
    }

    private static class FakeLocalRepository implements LocalRepository {
        private final List<Local> storage = new ArrayList<>();
        private final List<UUID> inUseIds = new ArrayList<>();
        private final List<UUID> futureEventsIds = new ArrayList<>();

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
        public List<Local> findByGrupoParoquialId(UUID grupoParoquialId) {
            return storage.stream()
                    .filter(l -> grupoParoquialId != null && grupoParoquialId.equals(l.getGrupoParoquialId()))
                    .toList();
        }

        @Override
        public boolean isLocalInUse(UUID id) {
            return inUseIds.contains(id);
        }

        @Override
        public boolean hasFutureEvents(UUID id) {
            return futureEventsIds.contains(id);
        }

        @Override
        public void delete(UUID id) {
            storage.removeIf(l -> l.getId().equals(id));
        }

        public void setFutureEvents(UUID id, boolean hasFutureEvents) {
            if (hasFutureEvents) {
                futureEventsIds.add(id);
            } else {
                futureEventsIds.remove(id);
            }
        }
    }
}
