package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;

class AdicionarVinculoUseCaseTest {

    private MembroOrganizacaoJpaRepository vinculoRepository;
    private UsuarioJpaRepository usuarioRepository;
    private AdicionarVinculoUseCase useCase;
    private SecurityContext originalContext;

    private OrganizacaoJpaRepository organizacaoRepository;

    @BeforeEach
    void setUp() {
        vinculoRepository = mock(MembroOrganizacaoJpaRepository.class);
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

    @Test
    void deveInativarAnteriorCoordenadorQuandoNovoUmEAdicionado() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        MembroOrganizacaoEntity existing = new MembroOrganizacaoEntity();
        existing.setId(UUID.randomUUID());
        existing.setUsuarioId(UUID.randomUUID());
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL.name());
        existing.setAtivo(true);

        when(vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(eq(organizacaoId),
                eq(PapelOrganizacional.COORD_PASTORAL_LOCAL.name()), any(UUID.class)))

                .thenReturn(Optional.of(existing));

        when(vinculoRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(
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
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

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
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
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
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.CLERO, PapelOrganizacional.PAROCO, null)
        );
    }

    @Test
    void devePermitirParocoDefinicaoQuandoAdmin() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
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
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

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
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
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
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

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

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchA))

                .thenReturn(Optional.empty());
        when(vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
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

        when(vinculoRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

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

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        MembroOrganizacaoEntity existing = new MembroOrganizacaoEntity();
        existing.setId(UUID.randomUUID());
        existing.setUsuarioId(usuarioId);
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name());
        existing.setIgrejaVinculoId(churchId);
        existing.setAtivo(true);

        when(vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existing));

        UUID resultId = useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchId);

        assertEquals(existing.getId(), resultId);
        verify(vinculoRepository, never()).save(any(MembroOrganizacaoEntity.class));
    }

    @Test
    void deveReativarInativoVinculoEmVezDeDeDuplicado() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        MembroOrganizacaoEntity existing = new MembroOrganizacaoEntity();
        existing.setId(UUID.randomUUID());
        existing.setUsuarioId(usuarioId);
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name());
        existing.setIgrejaVinculoId(churchId);
        existing.setAtivo(false);

        when(vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existing));
        when(vinculoRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

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

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        MembroOrganizacaoEntity existing = new MembroOrganizacaoEntity();
        existing.setId(UUID.randomUUID());
        existing.setUsuarioId(UUID.randomUUID());
        existing.setOrganizacaoId(organizacaoId);
        existing.setPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL.name());
        existing.setIgrejaVinculoId(churchA);
        existing.setAtivo(true);

        when(vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                PapelOrganizacional.COORD_PASTORAL_LOCAL.name(), churchA))

                .thenReturn(Optional.of(existing));
        when(vinculoRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL,
                churchA);

        assertFalse(existing.isAtivo());
        verify(vinculoRepository).save(existing);
    }

    @Test
    void deveRebaixarAnteriorCoordenadorParaMembroPapelQuandoNovoUmEAssigned() {
        mockAuthentication("ROLE_ADMIN");

        UUID newCoordId = UUID.randomUUID();
        UUID oldCoordId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        when(usuarioRepository.existsById(newCoordId)).thenReturn(true);

        MembroOrganizacaoEntity existingCoord = new MembroOrganizacaoEntity();
        existingCoord.setId(UUID.randomUUID());
        existingCoord.setUsuarioId(oldCoordId);
        existingCoord.setOrganizacaoId(organizacaoId);
        existingCoord.setPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL.name());
        existingCoord.setIgrejaVinculoId(churchId);
        existingCoord.setAtivo(true);

        when(vinculoRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                PapelOrganizacional.COORD_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existingCoord));
        when(vinculoRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(oldCoordId,
                organizacaoId, "VOLUNTARIO_PASTORAL_LOCAL", churchId))

                .thenReturn(Optional.empty());

        when(vinculoRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(newCoordId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL,
                churchId);

        assertFalse(existingCoord.isAtivo());
        // Verify demoted VOLUNTARIO vinculo was created and saved
        ArgumentCaptor<MembroOrganizacaoEntity> captor = ArgumentCaptor.forClass(MembroOrganizacaoEntity.class);
        verify(vinculoRepository, atLeast(2)).save(captor.capture());

        boolean demotedSaved = captor.getAllValues().stream()
                .anyMatch(m -> oldCoordId.equals(m.getUsuarioId())
                        && "VOLUNTARIO_PASTORAL_LOCAL".equals(m.getPapel()) && m.isAtivo());
        assertTrue(demotedSaved, "Previous coordinator should have an active VOLUNTARIO vinculo saved");
    }

    @Test
    void deveLancarExceptionQuandoUsuarioIgrejaIdDiffersDeIgrejaVinculoId() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID userChurchId = UUID.randomUUID();
        UUID differentChurchId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        var usuarioEntity = new UsuarioEntity();
        usuarioEntity.setId(usuarioId);
        usuarioEntity.setIgrejaId(userChurchId);

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEntity));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL,
                                differentChurchId)

        );

        assertTrue(ex.getMessage().contains("O usuário pertence a outra igreja/comunidade"));
    }
}
