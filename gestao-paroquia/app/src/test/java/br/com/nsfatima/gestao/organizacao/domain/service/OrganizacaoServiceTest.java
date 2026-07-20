package br.com.nsfatima.gestao.organizacao.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizationBusinessException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganizacaoServiceTest {

    private OrganizacaoService service;
    private OrganizacaoRepository repository;
    private AuditLogPersistenceService auditLogService;

    private UUID cleroId = UUID.randomUUID();
    private UUID matrizId = UUID.randomUUID();
    private UUID conselhoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(OrganizacaoRepository.class);
        auditLogService = mock(AuditLogPersistenceService.class);
        service = new OrganizacaoService(repository, auditLogService);

        // Setup mock parents
        Organizacao clero = new Organizacao(cleroId, "Clero", TipoOrganizacao.CLERO, "clero@test.com", "Coordenador", null, true, null);
        Organizacao matriz = new Organizacao(matrizId, "Matriz", TipoOrganizacao.MATRIZ, "matriz@test.com", "Coordenador", null, true, cleroId);
        Organizacao conselho = new Organizacao(conselhoId, "Conselho", TipoOrganizacao.CONSELHO, "conselho@test.com", "Coordenador", null, true, matrizId);

        when(repository.findById(cleroId)).thenReturn(Optional.of(clero));
        when(repository.findById(matrizId)).thenReturn(Optional.of(matriz));
        when(repository.findById(conselhoId)).thenReturn(Optional.of(conselho));
    }

    @Test
    void shouldCreateOrganization() {
        Organizacao created = service.createOrganization("PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", conselhoId);
        
        assertNotNull(created.getId());
        assertEquals("PJ", created.getNome());
        assertEquals("Coordenador A", created.getCoordenador());
        assertEquals("Vice Coordenador B", created.getViceCoordenador());
        assertTrue(created.isAtivo());
        verify(repository).save(any(Organizacao.class));
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void shouldUpdateOrganization() {
        UUID id = UUID.randomUUID();
        Organizacao org = new Organizacao(id, "PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true, conselhoId);
        when(repository.findById(id)).thenReturn(Optional.of(org));

        service.updateOrganization(id, "PJ Novo", TipoOrganizacao.PASTORAL, "pj2@test.com", "Novo Coordenador", "Novo Vice", false, conselhoId);

        assertEquals("PJ Novo", org.getNome());
        assertEquals(TipoOrganizacao.PASTORAL, org.getTipo());
        assertEquals("Novo Coordenador", org.getCoordenador());
        assertEquals("Novo Vice", org.getViceCoordenador());
        assertFalse(org.isAtivo());
        verify(repository).save(org);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void shouldThrowExceptionWhenDeletingOrgInUse() {
        UUID id = UUID.randomUUID();
        when(repository.hasDependencies(id)).thenReturn(true);

        assertThrows(OrganizationBusinessException.class, () -> service.deleteOrganization(id));
        verify(repository, never()).delete(id);
        verify(auditLogService, never()).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void shouldDeleteOrgWhenNotInUse() {
        UUID id = UUID.randomUUID();
        when(repository.hasDependencies(id)).thenReturn(false);

        service.deleteOrganization(id);
        verify(repository).delete(id);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void shouldCreateOrganizationWithConselhoEconomico() {
        Organizacao created = service.createOrganization("Conselho Econômico", TipoOrganizacao.CONSELHO_ECONOMICO, "financeiro@test.com", "Pedro Finanças", "Ana Administração", matrizId);
        
        assertNotNull(created.getId());
        assertEquals(TipoOrganizacao.CONSELHO_ECONOMICO, created.getTipo());
    }

    @Test
    void shouldFailToCreateMatrizWithoutCleroParent() {
        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Matriz Sem Pai", TipoOrganizacao.MATRIZ, "test@test.com", "Coord", "Vice", null)
        );
        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Matriz Com Pai Incorreto", TipoOrganizacao.MATRIZ, "test@test.com", "Coord", "Vice", conselhoId)
        );
    }

    @Test
    void shouldFailToCreateCouncilWithoutMatrizOrComunidadeParent() {
        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Conselho Sem Pai", TipoOrganizacao.CONSELHO, "test@test.com", "Coord", "Vice", null)
        );
        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Conselho Com Pai Incorreto", TipoOrganizacao.CONSELHO, "test@test.com", "Coord", "Vice", cleroId)
        );
    }

    @Test
    void shouldFailToCreatePastoralWithoutCouncilParent() {
        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Pastoral Sem Pai", TipoOrganizacao.PASTORAL, "test@test.com", "Coord", "Vice", null)
        );
        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Pastoral Com Pai Incorreto", TipoOrganizacao.PASTORAL, "test@test.com", "Coord", "Vice", matrizId)
        );
    }

    @Test
    void shouldFailToCreateOrgIfParentIsInactive() {
        UUID inactiveId = UUID.randomUUID();
        Organizacao inactiveParent = new Organizacao(inactiveId, "Conselho Inativo", TipoOrganizacao.CONSELHO, "conselho@test.com", "Coordenador", null, false, matrizId);
        when(repository.findById(inactiveId)).thenReturn(Optional.of(inactiveParent));

        assertThrows(OrganizationBusinessException.class, () ->
            service.createOrganization("Pastoral Novo", TipoOrganizacao.PASTORAL, "test@test.com", "Coord", "Vice", inactiveId)
        );
    }

    @Test
    void shouldAllowCreatingCleroWithNullParent() {
        Organizacao created = service.createOrganization("Clero Novo", TipoOrganizacao.CLERO, "clero@test.com", "Coord", "Vice", null);
        assertNotNull(created.getId());
        assertEquals(TipoOrganizacao.CLERO, created.getTipo());
        assertNull(created.getParentId());
    }
}
