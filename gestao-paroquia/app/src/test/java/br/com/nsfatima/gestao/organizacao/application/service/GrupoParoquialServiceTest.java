package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.GrupoParoquialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.GrupoParoquial;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
class GrupoParoquialServiceTest {

    private final UUID cleroUnidadeId = UUID.randomUUID();
    private final UUID matrizId = UUID.randomUUID();
    private final UUID conselhoId = UUID.randomUUID();
    private final UUID pastoralOficialId = UUID.randomUUID();
    private final UUID movimentoOficialId = UUID.randomUUID();
    private GrupoParoquialService service;
    private GrupoParoquialRepository repository;
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private AuditoriaPersistenceService auditLogService;

    @BeforeEach
    void setUp() {
        repository = mock(GrupoParoquialRepository.class);
        unidadeTerritorialRepository = mock(UnidadeTerritorialJpaRepository.class);
        auditLogService = mock(AuditoriaPersistenceService.class);
        PastoralMovimentoOficialRepository pastoralOficialRepository = mock(PastoralMovimentoOficialRepository.class);
        service = new GrupoParoquialService(repository, auditLogService,
                new ValidadorHierarquiaOrganizacional(repository, pastoralOficialRepository,
                        unidadeTerritorialRepository));

        // Setup mock unidades territoriais (Clero/Matriz não são mais GrupoParoquial, track
        // linguagem_ubiqua_dominio)
        UnidadeTerritorialEntity cleroUnidade = new UnidadeTerritorialEntity();
        cleroUnidade.setId(cleroUnidadeId);
        cleroUnidade.setNome("Clero");
        cleroUnidade.setTipo(TipoUnidadeTerritorial.CLERO);
        cleroUnidade.setAtivo(true);

        UnidadeTerritorialEntity matrizUnidade = new UnidadeTerritorialEntity();
        matrizUnidade.setId(matrizId);
        matrizUnidade.setNome("Matriz");
        matrizUnidade.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matrizUnidade.setParentId(cleroUnidadeId);
        matrizUnidade.setAtivo(true);

        when(unidadeTerritorialRepository.findById(cleroUnidadeId)).thenReturn(Optional.of(cleroUnidade));
        when(unidadeTerritorialRepository.findById(matrizId)).thenReturn(Optional.of(matrizUnidade));

        // Setup mock parent
        GrupoParoquial conselho = new GrupoParoquial(conselhoId,
                "Conselho", TipoGrupo.CONSELHO, "conselho@test.com", "Coordenador", null, true,
                matrizId, null);

        when(repository.findById(conselhoId)).thenReturn(Optional.of(conselho));

        // Setup mock official pastorals
        PastoralMovimentoOficial pastoralOficial = new PastoralMovimentoOficial(pastoralOficialId,
                "Oficial Pastoral", TipoGrupo.PASTORAL);

        PastoralMovimentoOficial movimentoOficial = new PastoralMovimentoOficial(movimentoOficialId,
                "Oficial Movimento", TipoGrupo.MOVIMENTO);

        when(pastoralOficialRepository.findById(pastoralOficialId)).thenReturn(Optional.of(pastoralOficial));
        when(pastoralOficialRepository.findById(movimentoOficialId)).thenReturn(Optional.of(movimentoOficial));
    }

    @Test
    void deveCriarGrupoParoquial() {
        GrupoParoquial created = service.createOrganization("PJ",
                TipoGrupo.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", conselhoId,
                movimentoOficialId);


        assertNotNull(created.getId());
        assertEquals("PJ", created.getNome());
        assertEquals("Coordenador A", created.getCoordenador());
        assertEquals("Vice Coordenador B", created.getViceCoordenador());
        assertTrue(created.isAtivo());
        verify(repository).save(any(GrupoParoquial.class));
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveAtualizarGrupoParoquial() {
        UUID id = UUID.randomUUID();
        GrupoParoquial org = new GrupoParoquial(id,
                "PJ", TipoGrupo.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true,
                conselhoId, movimentoOficialId);

        when(repository.findById(id)).thenReturn(Optional.of(org));

        service.updateOrganization(id,
                "PJ Novo", TipoGrupo.PASTORAL, "pj2@test.com", "Novo Coordenador", "Novo Vice", false,
                conselhoId, pastoralOficialId);


        assertEquals("PJ Novo", org.getNome());
        assertEquals(TipoGrupo.PASTORAL, org.getTipo());
        assertEquals("Novo Coordenador", org.getCoordenador());
        assertEquals("Novo Vice", org.getViceCoordenador());
        assertFalse(org.isAtivo());
        verify(repository).save(org);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveLancarExceptionQuandoExcluindoOrgEmUso() {
        UUID id = UUID.randomUUID();
        GrupoParoquial org = new GrupoParoquial(id,
                "PJ", TipoGrupo.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true,
                conselhoId, movimentoOficialId);

        when(repository.findById(id)).thenReturn(Optional.of(org));
        when(repository.hasDependencies(id)).thenReturn(true);

        assertThrows(GrupoParoquialNegocioException.class, () -> service.deleteOrganization(id));
        verify(repository, never()).delete(id);
        verify(auditLogService, never()).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveExcluirOrgQuandoNaoEmUso() {
        UUID id = UUID.randomUUID();
        GrupoParoquial org = new GrupoParoquial(id,
                "PJ", TipoGrupo.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true,
                conselhoId, movimentoOficialId);

        when(repository.findById(id)).thenReturn(Optional.of(org));
        when(repository.hasDependencies(id)).thenReturn(false);

        service.deleteOrganization(id);
        verify(repository).delete(id);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveCriarGrupoParoquialComConselho() {
        GrupoParoquial created = service.createOrganization("Conselho",
                TipoGrupo.CONSELHO, "financeiro@test.com", "Pedro Finanças", "Ana Administração",
                matrizId, null);


        assertNotNull(created.getId());
        assertEquals(TipoGrupo.CONSELHO, created.getTipo());
    }

    @Test
    void deveFalhaParaCriarConselhoSemMatrizOuComunidadePai() {
        assertThrows(GrupoParoquialNegocioException.class, () ->
                service.createOrganization("Conselho Sem Pai",
                        TipoGrupo.CONSELHO, "test@test.com", "Coord", "Vice", null, null)

        );
        assertThrows(GrupoParoquialNegocioException.class, () ->
                service.createOrganization("Conselho Com Pai Incorreto",
                        TipoGrupo.CONSELHO, "test@test.com", "Coord", "Vice", cleroUnidadeId, null)

        );
    }

    @Test
    void deveFalhaParaCriarPastoralSemConselhoPai() {
        assertThrows(GrupoParoquialNegocioException.class, () ->
                service.createOrganization("Pastoral Sem Pai",
                        TipoGrupo.PASTORAL, "test@test.com", "Coord", "Vice", null, pastoralOficialId)

        );
        assertThrows(GrupoParoquialNegocioException.class, () ->
                service.createOrganization("Pastoral Com Pai Incorreto",
                        TipoGrupo.PASTORAL, "test@test.com", "Coord", "Vice", matrizId, pastoralOficialId)

        );
    }

    @Test
    void deveFalhaParaCriarOrgSePaiEInativo() {
        UUID inactiveId = UUID.randomUUID();
        GrupoParoquial inactiveParent = new GrupoParoquial(inactiveId,
                "Conselho Inativo", TipoGrupo.CONSELHO, "conselho@test.com", "Coordenador", null,
                false, matrizId, null);

        when(repository.findById(inactiveId)).thenReturn(Optional.of(inactiveParent));

        assertThrows(GrupoParoquialNegocioException.class, () ->
                service.createOrganization("Pastoral Novo",
                        TipoGrupo.PASTORAL, "test@test.com", "Coord", "Vice", inactiveId, pastoralOficialId)

        );
    }

    @Test
    void devePermitirCriandoMinisterioComNuloPai() {
        GrupoParoquial created = service.createOrganization("Ministério Novo",
                TipoGrupo.MINISTERIO, "ministerio@test.com", "Coord", "Vice", null, null);

        assertNotNull(created.getId());
        assertEquals(TipoGrupo.MINISTERIO, created.getTipo());
        assertNull(created.getParentId());
    }
}
