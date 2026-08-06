package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
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
    private AdicionarVinculoUseCase useCase;
    private SecurityContext originalContext;

    private OrganizacaoJpaRepository organizacaoRepository;

    @BeforeEach
    void setUp() {
        vinculoRepository = mock(MembroGrupoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        useCase = new AdicionarVinculoUseCase(vinculoRepository, usuarioRepository, organizacaoRepository);
        originalContext = SecurityContextHolder.getContext();

        var defaultIgreja = new OrganizacaoEntity();
        defaultIgreja.setId(UUID.randomUUID());
        defaultIgreja.setTipo(TipoOrganizacao.MATRIZ);
        defaultIgreja.setNome("Matriz Default");
        when(organizacaoRepository.findById(any(UUID.class))).thenReturn(Optional.of(defaultIgreja));
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
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(UUID.randomUUID());
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL.name());
        existing.setAtivo(true);

        when(vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(eq(organizacaoId),
                eq(PapelOrganizacional.COORD_PASTORAL_LOCAL.name()), any(UUID.class)))

                .thenReturn(Optional.of(existing));

        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL, UUID.randomUUID());


        assertFalse(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveNegarConselhoCoordenadorDefinicaoQuandoNaoParocoOuAdmin() {
        mockAuthentication("ROLE_USER");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        assertThrows(AccessDeniedException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.CONSELHO_PASTORAL, PapelOrganizacional.PRESIDENTE_CPC, null)

        );
    }

    @Test
    void devePermitirConselhoCoordenadorDefinicaoQuandoParocoOuAdmin() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);
        when(vinculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.CONSELHO_PASTORAL, PapelOrganizacional.PRESIDENTE_CPC, null)

        );
    }

    @Test
    void deveNegarParocoDefinicaoQuandoNaoAdmin() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        assertThrows(AccessDeniedException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.CLERO, PapelOrganizacional.PAROCO, null)
        );
    }

    @Test
    void devePermitirParocoDefinicaoQuandoAdmin() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);
        when(vinculoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.CLERO, PapelOrganizacional.PAROCO, null)
        );
    }

    @Test
    void deveLancarExceptionQuandoIgrejaVinculoIdENuloParaPastoralOuMovimento() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.PASTORAL,
                        PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, null)
        );
        assertEquals("Igreja de vinculo e obrigatoria para pastorais e movimentos", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.MOVIMENTO,
                        PapelOrganizacional.MEMBRO_MOVIMENTO, null)
        );
        assertEquals("Igreja de vinculo e obrigatoria para pastorais e movimentos", ex2.getMessage());
    }

    @Test
    void deveLancarExceptionQuandoIgrejaVinculoIdNaoEncontrado() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);
        when(organizacaoRepository.findById(igrejaId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                                igrejaId)

        );
        assertEquals("Igreja de vinculo nao encontrada: " + igrejaId, ex.getMessage());
    }

    @Test
    void deveLancarExceptionQuandoIgrejaVinculoTipoEInvalido() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();
        mockUsuarioComFiel(usuarioId);

        var invalidIgreja = new OrganizacaoEntity();
        invalidIgreja.setId(igrejaId);
        invalidIgreja.setTipo(TipoOrganizacao.PASTORAL);
        when(organizacaoRepository.findById(igrejaId)).thenReturn(Optional.of(invalidIgreja));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                                igrejaId)

        );
        assertEquals("Igreja de vinculo deve ser do tipo MATRIZ ou COMUNIDADE", ex.getMessage());
    }

    @Test
    void devePermitirDuplicadoVinculosEmDiferenteIgrejas() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchA = UUID.randomUUID();
        UUID churchB = UUID.randomUUID();

        UUID fielId = mockUsuarioComFiel(usuarioId);
        when(vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(fielId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchA))

                .thenReturn(Optional.empty());
        when(vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(fielId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchB))

                .thenReturn(Optional.empty());

        var defaultIgrejaA = new OrganizacaoEntity();
        defaultIgrejaA.setId(churchA);
        defaultIgrejaA.setTipo(TipoOrganizacao.MATRIZ);
        when(organizacaoRepository.findById(churchA)).thenReturn(Optional.of(defaultIgrejaA));

        var defaultIgrejaB = new OrganizacaoEntity();
        defaultIgrejaB.setId(churchB);
        defaultIgrejaB.setTipo(TipoOrganizacao.COMUNIDADE);
        when(organizacaoRepository.findById(churchB)).thenReturn(Optional.of(defaultIgrejaB));

        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchA));

        assertDoesNotThrow(() -> useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchB));

    }

    @Test
    void deveImpedirDuplicadoVinculosEmMesmaIgreja() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        UUID fielId = mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(fielId);
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name());
        existing.setIgrejaVinculoId(churchId);
        existing.setAtivo(true);

        when(vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(fielId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existing));

        UUID resultId = useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchId);

        assertEquals(existing.getId(), resultId);
        verify(vinculoRepository, never()).save(any(MembroGrupoEntity.class));
    }

    @Test
    void deveReativarInativoVinculoEmVezDeDeDuplicado() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        UUID fielId = mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(fielId);
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name());
        existing.setIgrejaVinculoId(churchId);
        existing.setAtivo(false);

        when(vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(fielId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existing));
        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID resultId = useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchId);

        assertEquals(existing.getId(), resultId);
        assertTrue(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveApenasInativarAnteriorCoordenadorEmEspecificoIgreja() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchA = UUID.randomUUID();

        mockUsuarioComFiel(usuarioId);

        MembroGrupoEntity existing = new MembroGrupoEntity();
        existing.setId(UUID.randomUUID());
        existing.setFielId(UUID.randomUUID());
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL.name());
        existing.setIgrejaVinculoId(churchA);
        existing.setAtivo(true);

        when(vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                PapelOrganizacional.COORD_PASTORAL_LOCAL.name(), churchA))

                .thenReturn(Optional.of(existing));
        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL,
                churchA);

        assertFalse(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveRebaixarAnteriorCoordenadorParaMembroPapelQuandoNovoUmEAssigned() {
        mockAuthentication("ROLE_ADMIN");

        UUID newCoordUsuarioId = UUID.randomUUID();
        UUID oldCoordFielId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        mockUsuarioComFiel(newCoordUsuarioId);

        MembroGrupoEntity existingCoord = new MembroGrupoEntity();
        existingCoord.setId(UUID.randomUUID());
        existingCoord.setFielId(oldCoordFielId);
        existingCoord.setOrganizacaoId(organizacaoId);
        existingCoord.setPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL.name());
        existingCoord.setIgrejaVinculoId(churchId);
        existingCoord.setAtivo(true);

        when(vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                PapelOrganizacional.COORD_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existingCoord));
        when(vinculoRepository.findByFielIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(oldCoordFielId,
                organizacaoId, "VOLUNTARIO_PASTORAL_LOCAL", churchId))

                .thenReturn(Optional.empty());

        when(vinculoRepository.save(any(MembroGrupoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(newCoordUsuarioId, organizacaoId, TipoOrganizacao.PASTORAL,
                PapelOrganizacional.COORD_PASTORAL_LOCAL, churchId);

        assertFalse(existingCoord.isAtivo());
        // Verify demoted VOLUNTARIO vinculo was created and saved
        ArgumentCaptor<MembroGrupoEntity> captor = ArgumentCaptor.forClass(MembroGrupoEntity.class);
        verify(vinculoRepository, atLeast(2)).save(captor.capture());

        boolean demotedSaved = captor.getAllValues().stream()
                .anyMatch(m -> oldCoordFielId.equals(m.getFielId())
                        && "VOLUNTARIO_PASTORAL_LOCAL".equals(m.getPapel()) && m.isAtivo());
        assertTrue(demotedSaved, "Previous coordinator should have an active VOLUNTARIO vinculo saved");
    }

    @Test
    void deveLancarExceptionQuandoUsuarioNaoEncontrado() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        var ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                        UUID.randomUUID())
        );

        assertTrue(ex.getMessage().contains("Usuario nao encontrado"));
    }
}
