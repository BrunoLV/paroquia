package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdicionarVinculoUseCaseTest {

    private MembroGrupoJpaRepository vinculoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private PapelGlobalAuthorizationService papelGlobalAuthorizationService;
    private AdicionarVinculoUseCase useCase;
    private SecurityContext originalContext;

    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;

    @BeforeEach
    void setUp() {
        vinculoRepository = mock(MembroGrupoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        papelGlobalAuthorizationService = mock(PapelGlobalAuthorizationService.class);
        unidadeTerritorialRepository = mock(UnidadeTerritorialJpaRepository.class);
        useCase = new AdicionarVinculoUseCase(vinculoRepository, usuarioRepository, unidadeTerritorialRepository,
                papelGlobalAuthorizationService);
        originalContext = SecurityContextHolder.getContext();

        var defaultIgreja = new UnidadeTerritorialEntity();
        defaultIgreja.setId(UUID.randomUUID());
        defaultIgreja.setTipo(TipoUnidadeTerritorial.MATRIZ);
        defaultIgreja.setNome("Matriz Default");
        when(unidadeTerritorialRepository.findById(any(UUID.class))).thenReturn(Optional.of(defaultIgreja));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(String... roles) {
        var authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        var principal = new UsuarioDetails(UUID.randomUUID(), "actor", "pass", true, "", List.of());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    private UUID mockUsuarioComFiel(UUID usuarioId) {
        UUID fielId = UUID.randomUUID();
        var usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setFielId(fielId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        return fielId;
    }

    @Test
    void deveInativarAnteriorCoordenadorQuandoNovoUmEAdicionado() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(UUID.randomUUID());
        existing.setGrupoParoquialId(grupoParoquialId);
        existing.setPapel(PapelOrganizacional.COORDENADOR.name());
        existing.setAtivo(true);

        when(vinculoRepository.findByGrupoParoquialIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(eq(grupoParoquialId),
                eq(PapelOrganizacional.COORDENADOR.name()), any(UUID.class)))

                .thenReturn(Optional.of(existing));

        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        useCase.execute(usuarioId,
                grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.COORDENADOR, UUID.randomUUID());


        assertFalse(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveNegarCoordenadorDefinicaoQuandoNaoParoco() {
        mockAuthentication("ROLE_ADMIN", "ROLE_GLOBAL_ADMIN");
        when(papelGlobalAuthorizationService.isGlobalByUsuarioId(any())).thenReturn(true);

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        assertThrows(AccessDeniedException.class, () ->
                useCase.execute(usuarioId,
                        grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.COORDENADOR, UUID.randomUUID())

        );
    }

    @Test
    void devePermitirCoordenadorDefinicaoQuandoParoco() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);
        when(vinculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                useCase.execute(usuarioId,
                        grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.COORDENADOR, UUID.randomUUID())

        );
    }

    @Test
    void deveLancarExceptionQuandoIgrejaVinculoIdENuloParaPastoralOuMovimento() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId, grupoParoquialId, TipoGrupo.PASTORAL,
                        PapelOrganizacional.VOLUNTARIO, null)
        );
        assertEquals("Igreja de vinculo e obrigatoria para pastorais e movimentos", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId, grupoParoquialId, TipoGrupo.MOVIMENTO,
                        PapelOrganizacional.MEMBRO_ATIVO, null)
        );
        assertEquals("Igreja de vinculo e obrigatoria para pastorais e movimentos", ex2.getMessage());
    }

    @Test
    void deveLancarExceptionQuandoIgrejaVinculoIdNaoEncontrado() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);
        when(unidadeTerritorialRepository.findById(igrejaId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO,
                                igrejaId)

        );
        assertEquals("Igreja de vinculo nao encontrada: " + igrejaId, ex.getMessage());
    }

    @Test
    void deveLancarExceptionQuandoIgrejaVinculoTipoEInvalido() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        var invalidIgreja = new UnidadeTerritorialEntity();
        invalidIgreja.setId(igrejaId);
        invalidIgreja.setTipo(null);
        when(unidadeTerritorialRepository.findById(igrejaId)).thenReturn(Optional.of(invalidIgreja));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO,
                                igrejaId)

        );
        assertEquals("Igreja de vinculo deve ser do tipo MATRIZ ou COMUNIDADE", ex.getMessage());
    }

    @Test
    void devePermitirDuplicadoVinculosEmDiferenteIgrejas() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID churchA = UUID.randomUUID();
        UUID churchB = UUID.randomUUID();

        UUID fielId = mockUsuarioComFiel(usuarioId);
        when(vinculoRepository.findByFielIdAndGrupoParoquialIdAndPapelAndIgrejaVinculoId(fielId,
                grupoParoquialId, PapelOrganizacional.VOLUNTARIO.name(), churchA))

                .thenReturn(Optional.empty());
        when(vinculoRepository.findByFielIdAndGrupoParoquialIdAndPapelAndIgrejaVinculoId(fielId,
                grupoParoquialId, PapelOrganizacional.VOLUNTARIO.name(), churchB))

                .thenReturn(Optional.empty());

        var defaultIgrejaA = new UnidadeTerritorialEntity();
        defaultIgrejaA.setId(churchA);
        defaultIgrejaA.setTipo(TipoUnidadeTerritorial.MATRIZ);
        when(unidadeTerritorialRepository.findById(churchA)).thenReturn(Optional.of(defaultIgrejaA));

        var defaultIgrejaB = new UnidadeTerritorialEntity();
        defaultIgrejaB.setId(churchB);
        defaultIgrejaB.setTipo(TipoUnidadeTerritorial.COMUNIDADE);
        when(unidadeTerritorialRepository.findById(churchB)).thenReturn(Optional.of(defaultIgrejaB));

        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> useCase.execute(usuarioId,
                grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO, churchA));

        assertDoesNotThrow(() -> useCase.execute(usuarioId,
                grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO, churchB));

    }

    @Test
    void deveImpedirDuplicadoVinculosEmMesmaIgreja() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        UUID fielId = mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(fielId);
        existing.setGrupoParoquialId(grupoParoquialId);
        existing.setPapel(PapelOrganizacional.VOLUNTARIO.name());
        existing.setIgrejaVinculoId(churchId);
        existing.setAtivo(true);

        when(vinculoRepository.findByFielIdAndGrupoParoquialIdAndPapelAndIgrejaVinculoId(fielId,
                grupoParoquialId, PapelOrganizacional.VOLUNTARIO.name(), churchId))

                .thenReturn(Optional.of(existing));

        UUID resultId = useCase.execute(usuarioId,
                grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO, churchId);

        assertEquals(existing.getId(), resultId);
        verify(vinculoRepository, never()).save(any(MembroGrupoEntity.class));
    }

    @Test
    void deveReativarInativoVinculoEmVezDeDeDuplicado() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        UUID fielId = mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(fielId);
        existing.setGrupoParoquialId(grupoParoquialId);
        existing.setPapel(PapelOrganizacional.VOLUNTARIO.name());
        existing.setIgrejaVinculoId(churchId);
        existing.setAtivo(false);

        when(vinculoRepository.findByFielIdAndGrupoParoquialIdAndPapelAndIgrejaVinculoId(fielId,
                grupoParoquialId, PapelOrganizacional.VOLUNTARIO.name(), churchId))

                .thenReturn(Optional.of(existing));
        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID resultId = useCase.execute(usuarioId,
                grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO, churchId);

        assertEquals(existing.getId(), resultId);
        assertTrue(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveApenasInativarAnteriorCoordenadorEmEspecificoIgreja() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID churchA = UUID.randomUUID();

        mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(UUID.randomUUID());
        existing.setGrupoParoquialId(grupoParoquialId);
        existing.setPapel(PapelOrganizacional.COORDENADOR.name());
        existing.setIgrejaVinculoId(churchA);
        existing.setAtivo(true);

        when(vinculoRepository.findByGrupoParoquialIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(grupoParoquialId,
                PapelOrganizacional.COORDENADOR.name(), churchA))

                .thenReturn(Optional.of(existing));
        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(usuarioId, grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.COORDENADOR,
                churchA);

        assertFalse(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveRebaixarAnteriorCoordenadorParaMembroPapelQuandoNovoUmEAssigned() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID newCoordUsuarioId = UUID.randomUUID();
        UUID oldCoordFielId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        mockUsuarioComFiel(newCoordUsuarioId);

        MembroGrupoEntity existingCoord = new MembroGrupoEntity();
        existingCoord.setId(UUID.randomUUID());
        existingCoord.setFielId(oldCoordFielId);
        existingCoord.setGrupoParoquialId(grupoParoquialId);
        existingCoord.setPapel(PapelOrganizacional.COORDENADOR.name());
        existingCoord.setIgrejaVinculoId(churchId);
        existingCoord.setAtivo(true);

        when(vinculoRepository.findByGrupoParoquialIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(grupoParoquialId,
                PapelOrganizacional.COORDENADOR.name(), churchId))

                .thenReturn(Optional.of(existingCoord));
        when(vinculoRepository.findByFielIdAndGrupoParoquialIdAndPapelAndIgrejaVinculoId(oldCoordFielId,
                grupoParoquialId, PapelOrganizacional.MEMBRO_ATIVO.name(), churchId))

                .thenReturn(Optional.empty());

        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(newCoordUsuarioId, grupoParoquialId, TipoGrupo.PASTORAL,
                PapelOrganizacional.COORDENADOR, churchId);

        assertFalse(existingCoord.isAtivo());
        ArgumentCaptor<MembroGrupoEntity> captor = ArgumentCaptor.forClass(MembroGrupoEntity.class);
        verify(vinculoRepository, atLeast(2)).save(captor.capture());

        boolean demotedSaved = captor.getAllValues().stream()
                .anyMatch(m -> oldCoordFielId.equals(m.getFielId())
                        && PapelOrganizacional.MEMBRO_ATIVO.name().equals(m.getPapel()) && m.isAtivo());
        assertTrue(demotedSaved, "Previous coordinator should have an active MEMBRO_ATIVO vinculo saved");
    }

    @Test
    void deveLancarExceptionQuandoUsuarioNaoEncontrado() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID grupoParoquialId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        grupoParoquialId, TipoGrupo.PASTORAL, PapelOrganizacional.VOLUNTARIO,
                        UUID.randomUUID())
        );

        assertTrue(ex.getMessage().contains("Usuario nao encontrado"));
    }
}
