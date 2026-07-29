package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddMembershipUseCaseTest {

    private MembroOrganizacaoJpaRepository membershipRepository;
    private UsuarioJpaRepository usuarioRepository;
    private AddMembershipUseCase useCase;
    private SecurityContext originalContext;

    private OrganizacaoJpaRepository organizacaoRepository;

    @BeforeEach
    void setUp() {
        membershipRepository = mock(MembroOrganizacaoJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        useCase = new AddMembershipUseCase(membershipRepository, usuarioRepository, organizacaoRepository);
        originalContext = SecurityContextHolder.getContext();

        var defaultIgreja =
                new br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity();
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
        var authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        var principal = new UsuarioDetails(UUID.randomUUID(), "actor", "pass", true, "", List.of());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void shouldInactivatePreviousCoordinatorWhenNewOneIsAdded() {
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

        when(membershipRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(eq(organizacaoId),
                eq(PapelOrganizacional.COORD_PASTORAL_LOCAL.name()), any(UUID.class)))

                .thenReturn(Optional.of(existing));

        when(membershipRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL, UUID.randomUUID());


        assertFalse(existing.isAtivo());
        verify(membershipRepository).save(existing);
    }

    @Test
    void shouldDenyCouncilCoordinatorDefinitionWhenNotParocoOrAdmin() {
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
    void shouldAllowCouncilCoordinatorDefinitionWhenParocoOrAdmin() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.CONSELHO_PASTORAL, PapelOrganizacional.PRESIDENTE_CPC, null)

        );
    }

    @Test
    void shouldDenyParocoDefinitionWhenNotAdmin() {
        mockAuthentication("ROLE_CLERO_PAROCO");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        assertThrows(AccessDeniedException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.CLERO, PapelOrganizacional.PAROCO, null)
        );
    }

    @Test
    void shouldAllowParocoDefinitionWhenAdmin() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(membershipRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.CLERO, PapelOrganizacional.PAROCO, null)
        );
    }

    @Test
    void shouldThrowExceptionWhenIgrejaVinculoIdIsNullForPastoralOrMovimento() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, null)
        );
        assertEquals("Igreja de vinculo e obrigatoria para pastorais e movimentos", ex1.getMessage());

        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.MOVIMENTO, PapelOrganizacional.MEMBRO_MOVIMENTO, null)
        );
        assertEquals("Igreja de vinculo e obrigatoria para pastorais e movimentos", ex2.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIgrejaVinculoIdNotFound() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(organizacaoRepository.findById(igrejaId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, igrejaId)

        );
        assertEquals("Igreja de vinculo nao encontrada: " + igrejaId, ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenIgrejaVinculoTypeIsInvalid() {
        mockAuthentication("ROLE_ADMIN");
        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID igrejaId = UUID.randomUUID();
        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);

        var invalidIgreja =
                new br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity();
        invalidIgreja.setId(igrejaId);
        invalidIgreja.setTipo(TipoOrganizacao.PASTORAL);
        when(organizacaoRepository.findById(igrejaId)).thenReturn(Optional.of(invalidIgreja));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, igrejaId)

        );
        assertEquals("Igreja de vinculo deve ser do tipo MATRIZ ou COMUNIDADE", ex.getMessage());
    }

    @Test
    void shouldAllowDuplicateMembershipsInDifferentChurches() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();
        UUID churchA = UUID.randomUUID();
        UUID churchB = UUID.randomUUID();

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchA))

                .thenReturn(Optional.empty());
        when(membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchB))

                .thenReturn(Optional.empty());

        var defaultIgrejaA =
                new br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity();
        defaultIgrejaA.setId(churchA);
        defaultIgrejaA.setTipo(TipoOrganizacao.MATRIZ);
        when(organizacaoRepository.findById(churchA)).thenReturn(Optional.of(defaultIgrejaA));

        var defaultIgrejaB =
                new br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity();
        defaultIgrejaB.setId(churchB);
        defaultIgrejaB.setTipo(TipoOrganizacao.COMUNIDADE);
        when(organizacaoRepository.findById(churchB)).thenReturn(Optional.of(defaultIgrejaB));

        when(membershipRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() -> useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchA));

        assertDoesNotThrow(() -> useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchB));

    }

    @Test
    void shouldPreventDuplicateMembershipsInSameChurch() {
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

        when(membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existing));

        UUID resultId = useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchId);

        assertEquals(existing.getId(), resultId);
        verify(membershipRepository, never()).save(any(MembroOrganizacaoEntity.class));
    }

    @Test
    void shouldReactivateInactiveMembershipInsteadOfDuplicate() {
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

        when(membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(usuarioId,
                organizacaoId, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existing));
        when(membershipRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID resultId = useCase.execute(usuarioId,
                organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, churchId);

        assertEquals(existing.getId(), resultId);
        assertTrue(existing.isAtivo());
        verify(membershipRepository).save(existing);
    }

    @Test
    void shouldOnlyInactivatePreviousCoordinatorAtSpecificChurch() {
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

        when(membershipRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                PapelOrganizacional.COORD_PASTORAL_LOCAL.name(), churchA))

                .thenReturn(Optional.of(existing));
        when(membershipRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(usuarioId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL, churchA);

        assertFalse(existing.isAtivo());
        verify(membershipRepository).save(existing);
    }

    @Test
    void shouldDemotePreviousCoordinatorToMembroRoleWhenNewOneIsAssigned() {
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

        when(membershipRepository.findByOrganizacaoIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(organizacaoId,
                PapelOrganizacional.COORD_PASTORAL_LOCAL.name(), churchId))

                .thenReturn(Optional.of(existingCoord));
        when(membershipRepository.findByUsuarioIdAndOrganizacaoIdAndPapelAndIgrejaVinculoId(oldCoordId,
                organizacaoId, "VOLUNTARIO_PASTORAL_LOCAL", churchId))

                .thenReturn(Optional.empty());

        when(membershipRepository.save(any(MembroOrganizacaoEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(newCoordId, organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.COORD_PASTORAL_LOCAL, churchId);

        assertFalse(existingCoord.isAtivo());
        // Verify demoted VOLUNTARIO membership was created and saved
        ArgumentCaptor<MembroOrganizacaoEntity> captor = ArgumentCaptor.forClass(MembroOrganizacaoEntity.class);
        verify(membershipRepository, atLeast(2)).save(captor.capture());

        boolean demotedSaved = captor.getAllValues().stream()
                .anyMatch(m -> oldCoordId.equals(m.getUsuarioId()) && "VOLUNTARIO_PASTORAL_LOCAL".equals(m.getPapel()) && m.isAtivo());
        assertTrue(demotedSaved, "Previous coordinator should have an active VOLUNTARIO membership saved");
    }

    @Test
    void shouldThrowExceptionWhenUserIgrejaIdDiffersFromIgrejaVinculoId() {
        mockAuthentication("ROLE_ADMIN");

        UUID usuarioId = UUID.randomUUID();
        UUID userChurchId = UUID.randomUUID();
        UUID differentChurchId = UUID.randomUUID();
        UUID organizacaoId = UUID.randomUUID();

        var usuarioEntity = new br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity();
        usuarioEntity.setId(usuarioId);
        usuarioEntity.setIgrejaId(userChurchId);

        when(usuarioRepository.existsById(usuarioId)).thenReturn(true);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuarioEntity));

        var ex = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(usuarioId,
                        organizacaoId, TipoOrganizacao.PASTORAL, PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL, differentChurchId)

        );

        assertTrue(ex.getMessage().contains("O usuário pertence a outra igreja/comunidade"));
    }
}
