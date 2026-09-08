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
    void deveCriarLocalComIgreja() {
        UUID igrejaId = UUID.randomUUID();
        EnderecoLocal endereco = new EnderecoLocal("01001-000", "Praça da Sé", "100", null, "Centro", "São Paulo", "SP");
        Local created = localService.createLocal("Salão São Pedro", "SALAO", endereco, 100, "Salão amplo", igrejaId);

        assertNotNull(created.getId());
        assertEquals("Salão São Pedro", created.getNome());
        assertEquals(igrejaId, created.getIgrejaId());
        assertEquals(100, created.getCapacidadeMaxima());
        assertTrue(created.isAtivo());
        assertTrue(localRepository.findById(created.getId()).isPresent());
    }

    @Test
    void deveCriarLocalSemIgreja() {
        EnderecoLocal endereco = new EnderecoLocal(null, "Rua das Rosas", "50", null, "Bairro", "Cidade", "SP");
        Local created = localService.createLocal("Salão Geral", "SALAO", endereco, 80, null, null);

        assertNotNull(created.getId());
        assertEquals("Salão Geral", created.getNome());
        assertTrue(created.isAtivo());
        assertTrue(localRepository.findById(created.getId()).isPresent());
    }

    @Test
    void deveAtualizarLocal() {
        UUID igrejaId = UUID.randomUUID();
        EnderecoLocal enderecoAntigo = new EnderecoLocal(null, "Rua Antiga", "10", null, null, null, null);
        Local local = new Local(UUID.randomUUID(), "Velho Nome", "SALA", enderecoAntigo, 10, "Obs antiga", true, null);
        localRepository.save(local);

        EnderecoLocal enderecoNovo = new EnderecoLocal(null, "Rua Nova", "20", null, "Centro", "Cidade", "SP");
        Local updated = localService.updateLocal(local.getId(), "Novo Nome", "SALAO", enderecoNovo, 30, "Obs nova", false, igrejaId);

        assertEquals("Novo Nome", updated.getNome());
        assertEquals(30, updated.getCapacidadeMaxima());
        assertEquals(igrejaId, updated.getIgrejaId());
        assertFalse(updated.isAtivo());
    }

    @Test
    void deveExcluirLogicamenteLocalSemEventosFuturos() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Auditório", "AUDITORIO", EnderecoLocal.vazio(), 50, "Som", true, null);
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
        Local local = new Local(id, "Salão São Pedro", "SALAO", EnderecoLocal.vazio(), 150, "Som", true, null);
        localRepository.save(local);
        localRepository.setFutureEvents(id, true);

        LocalBusinessException ex = assertThrows(LocalBusinessException.class, () -> localService.deleteLocal(id));
        assertTrue(ex.getMessage().contains("eventos futuros"));
    }

    @Test
    void naoDevePermitirInativarLocalComEventosFuturos() {
        UUID id = UUID.randomUUID();
        Local local = new Local(id, "Salão São Pedro", "SALAO", EnderecoLocal.vazio(), 150, "Som", true, null);
        localRepository.save(local);
        localRepository.setFutureEvents(id, true);

        LocalBusinessException ex = assertThrows(LocalBusinessException.class,
                () -> localService.updateLocal(id, "Salão São Pedro", "SALAO", EnderecoLocal.vazio(), 150, "Som", false, null));
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
