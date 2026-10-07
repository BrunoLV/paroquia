package br.com.nsfatima.gestao.organizacao.application.service;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.organizacao.domain.exception.OrganizacaoNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.ReconhecimentoEstatuto;
import br.com.nsfatima.gestao.organizacao.domain.model.AmbitoOrganismo;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.domain.repository.MandatoCoordenacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
class OrganizacaoServiceTest {

    private final UUID cleroUnidadeId = UUID.randomUUID();
    private final UUID matrizId = UUID.randomUUID();
    private final UUID conselhoId = UUID.randomUUID();
    private final UUID pastoralOficialId = UUID.randomUUID();
    private final UUID movimentoOficialId = UUID.randomUUID();
    private OrganizacaoService service;
    private OrganizacaoRepository repository;
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private AuditoriaPersistenceService auditLogService;
    private MandatoCoordenacaoRepository mandatoRepository;

    @BeforeEach
    void setUp() {
        repository = mock(OrganizacaoRepository.class);
        unidadeTerritorialRepository = mock(UnidadeTerritorialJpaRepository.class);
        auditLogService = mock(AuditoriaPersistenceService.class);
        mandatoRepository = mock(MandatoCoordenacaoRepository.class);
        PastoralMovimentoOficialRepository pastoralOficialRepository = mock(PastoralMovimentoOficialRepository.class);
        service = new OrganizacaoService(repository, auditLogService,
                new ValidadorHierarquiaOrganizacional(repository, pastoralOficialRepository,
                        unidadeTerritorialRepository),
                mandatoRepository, Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC));

        // Setup mock unidades territoriais (Clero/Matriz não são mais Organizacao, track
        // linguagem_ubiqua_dominio)
        UnidadeTerritorialEntity matrizUnidade = new UnidadeTerritorialEntity();
        matrizUnidade.setId(matrizId);
        matrizUnidade.setNome("Matriz");
        matrizUnidade.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matrizUnidade.setParentId(null);
        matrizUnidade.setAtivo(true);

        when(unidadeTerritorialRepository.findById(matrizId)).thenReturn(Optional.of(matrizUnidade));

        // Setup mock parent
        Organizacao conselho = new Organizacao(conselhoId,
                "Conselho", TipoOrganizacao.CONSELHO, "conselho@test.com", "Coordenador", null, true,
                matrizId, null, null, SubtipoConselho.CPP);

        when(repository.findById(conselhoId)).thenReturn(Optional.of(conselho));

        // Setup mock official pastorals
        PastoralMovimentoOficial pastoralOficial = new PastoralMovimentoOficial(pastoralOficialId,
                "Oficial Pastoral", TipoOrganizacao.PASTORAL);

        PastoralMovimentoOficial movimentoOficial = new PastoralMovimentoOficial(movimentoOficialId,
                "Oficial Movimento", TipoOrganizacao.MOVIMENTO, AmbitoOrganismo.DIOCESANO,
                new ReconhecimentoEstatuto("Bispo diocesano", LocalDate.of(2020, 1, 1), null));

        when(pastoralOficialRepository.findById(pastoralOficialId)).thenReturn(Optional.of(pastoralOficial));
        when(pastoralOficialRepository.findById(movimentoOficialId)).thenReturn(Optional.of(movimentoOficial));
    }

    @Test
    void deveCriarOrganizacao() {
        Organizacao created = service.criar("PJ",
                TipoOrganizacao.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", conselhoId,
                movimentoOficialId);


        assertNotNull(created.getId());
        assertEquals("PJ", created.getNome());
        assertNull(created.getCoordenador());
        assertNull(created.getViceCoordenador());
        assertTrue(created.isAtivo());
        verify(repository).save(any(Organizacao.class));
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveAtualizarOrganizacao() {
        UUID id = UUID.randomUUID();
        Organizacao org = new Organizacao(id,
                "PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true,
                conselhoId, movimentoOficialId);

        when(repository.findById(id)).thenReturn(Optional.of(org));

        service.atualizar(id,
                "PJ Novo", TipoOrganizacao.PASTORAL, "pj2@test.com", "Novo Coordenador", "Novo Vice", false,
                conselhoId, pastoralOficialId, SetorPastoral.FORMACAO_ENSINAR, null);


        assertEquals("PJ Novo", org.getNome());
        assertEquals(TipoOrganizacao.PASTORAL, org.getTipo());
        assertEquals("Coordenador A", org.getCoordenador());
        assertEquals("Vice Coordenador B", org.getViceCoordenador());
        assertFalse(org.isAtivo());
        verify(repository).save(org);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveLancarExceptionQuandoExcluindoOrgEmUso() {
        UUID id = UUID.randomUUID();
        Organizacao org = new Organizacao(id,
                "PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true,
                conselhoId, movimentoOficialId);

        when(repository.findById(id)).thenReturn(Optional.of(org));
        when(repository.hasDependencies(id)).thenReturn(true);

        assertThrows(OrganizacaoNegocioException.class, () -> service.excluir(id));
        verify(repository, never()).delete(id);
        verify(auditLogService, never()).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveExcluirOrgQuandoNaoEmUso() {
        UUID id = UUID.randomUUID();
        Organizacao org = new Organizacao(id,
                "PJ", TipoOrganizacao.MOVIMENTO, "pj@test.com", "Coordenador A", "Vice Coordenador B", true,
                conselhoId, movimentoOficialId);

        when(repository.findById(id)).thenReturn(Optional.of(org));
        when(repository.hasDependencies(id)).thenReturn(false);

        service.excluir(id);
        verify(repository).delete(id);
        verify(auditLogService).log(anyString(), anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    void deveCriarOrganizacaoComConselho() {
        Organizacao created = service.criar("Conselho",
                TipoOrganizacao.CONSELHO, "financeiro@test.com", "Pedro Finanças", "Ana Administração",
                matrizId, null, null, SubtipoConselho.CAEP);


        assertNotNull(created.getId());
        assertEquals(TipoOrganizacao.CONSELHO, created.getTipo());
    }

    @Test
    void deveFalhaParaCriarConselhoSemMatrizOuComunidadePai() {
        assertThrows(OrganizacaoNegocioException.class, () ->
                service.criar("Conselho Sem Pai",
                        TipoOrganizacao.CONSELHO, "test@test.com", "Coord", "Vice", null, null)

        );
        assertThrows(OrganizacaoNegocioException.class, () ->
                service.criar("Conselho Com Pai Incorreto",
                        TipoOrganizacao.CONSELHO, "test@test.com", "Coord", "Vice", cleroUnidadeId, null)

        );
    }

    @Test
    void deveFalhaParaCriarPastoralSemConselhoPai() {
        assertThrows(OrganizacaoNegocioException.class, () ->
                service.criar("Pastoral Sem Pai",
                        TipoOrganizacao.PASTORAL, "test@test.com", "Coord", "Vice", null, pastoralOficialId)

        );
        assertThrows(OrganizacaoNegocioException.class, () ->
                service.criar("Pastoral Com Pai Incorreto",
                        TipoOrganizacao.PASTORAL, "test@test.com", "Coord", "Vice", matrizId, pastoralOficialId)

        );
    }

    @Test
    void deveFalhaParaCriarOrgSePaiEInativo() {
        UUID inactiveId = UUID.randomUUID();
        Organizacao inactiveParent = new Organizacao(inactiveId,
                "Conselho Inativo", TipoOrganizacao.CONSELHO, "conselho@test.com", "Coordenador", null,
                false, matrizId, null);

        when(repository.findById(inactiveId)).thenReturn(Optional.of(inactiveParent));

        assertThrows(OrganizacaoNegocioException.class, () ->
                service.criar("Pastoral Novo",
                        TipoOrganizacao.PASTORAL, "test@test.com", "Coord", "Vice", inactiveId, pastoralOficialId,
                        SetorPastoral.FORMACAO_ENSINAR, null)

        );
    }

    @Test
    void deveCriarServicoParoquialArticuladoAoCpp() {
        Organizacao created = service.criar("Coroinhas",
                TipoOrganizacao.SERVICO_PAROQUIAL, "coroinhas@test.com", "Coord", "Vice", conselhoId, null,
                SetorPastoral.SANTIFICACAO_LITURGIA, null);

        assertNotNull(created.getId());
        assertEquals(TipoOrganizacao.SERVICO_PAROQUIAL, created.getTipo());
        assertEquals(SetorPastoral.SANTIFICACAO_LITURGIA, created.getSetorPastoral());
    }

    @Test
    void naoDeveCriarServicoParoquialSemConselhoPastoral() {
        assertThrows(OrganizacaoNegocioException.class, () -> service.criar("Coroinhas",
                TipoOrganizacao.SERVICO_PAROQUIAL, "c@test.com", "Coord", "Vice", null, null,
                SetorPastoral.SANTIFICACAO_LITURGIA, null));
    }

    @Test
    void naoDeveDesativarOrganizacaoComMandatoVigente() {
        UUID id = UUID.randomUUID();
        Organizacao org = new Organizacao(id, "Coroinhas", TipoOrganizacao.SERVICO_PAROQUIAL, "c@test.com", null,
                null, true, conselhoId, null, SetorPastoral.SANTIFICACAO_LITURGIA, null);
        when(repository.findById(id)).thenReturn(Optional.of(org));
        when(mandatoRepository.existeVigente(id, LocalDate.of(2026, 6, 1))).thenReturn(true);

        assertThrows(OrganizacaoNegocioException.class, () -> service.atualizar(id, "Coroinhas",
                TipoOrganizacao.SERVICO_PAROQUIAL, "c@test.com", null, null, false, conselhoId, null,
                SetorPastoral.SANTIFICACAO_LITURGIA, null));
        verify(repository, never()).save(any(Organizacao.class));
    }
}
