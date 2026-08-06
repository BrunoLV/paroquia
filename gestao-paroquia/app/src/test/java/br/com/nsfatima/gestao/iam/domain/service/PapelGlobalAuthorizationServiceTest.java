package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PapelGlobalAuthorizationServiceTest {

    private PapelGlobalJpaRepository papelGlobalRepository;
    private UsuarioJpaRepository usuarioRepository;
    private PapelGlobalAuthorizationService service;

    @BeforeEach
    void setUp() {
        papelGlobalRepository = mock(PapelGlobalJpaRepository.class);
        usuarioRepository = mock(UsuarioJpaRepository.class);
        service = new PapelGlobalAuthorizationService(papelGlobalRepository, usuarioRepository);
    }

    @Test
    void deveRetornarTrueParaFielComPapelGlobalAtivo() {
        UUID fielId = UUID.randomUUID();
        PapelGlobalEntity entity = new PapelGlobalEntity();
        entity.setPapel("vigario");
        when(papelGlobalRepository.findByFielIdAndAtivoTrue(fielId)).thenReturn(List.of(entity));

        assertThat(service.isGlobal(fielId)).isTrue();
        assertThat(service.papeisGlobaisDe(fielId)).containsExactly(PapelGlobal.VIGARIO);
    }

    @Test
    void deveRetornarFalseParaFielSemPapelGlobal() {
        UUID fielId = UUID.randomUUID();
        when(papelGlobalRepository.findByFielIdAndAtivoTrue(fielId)).thenReturn(List.of());

        assertThat(service.isGlobal(fielId)).isFalse();
    }

    @Test
    void deveRetornarFalseParaFielIdNulo() {
        assertThat(service.isGlobal(null)).isFalse();
        assertThat(service.papeisGlobaisDe(null)).isEmpty();
    }

    @Test
    void deveResolverFielIdAoConsultarPorUsuarioId() {
        UUID usuarioId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setFielId(fielId);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        PapelGlobalEntity entity = new PapelGlobalEntity();
        entity.setPapel("admin");
        when(papelGlobalRepository.findByFielIdAndAtivoTrue(fielId)).thenReturn(List.of(entity));

        assertThat(service.isGlobalByUsuarioId(usuarioId)).isTrue();
    }

    @Test
    void deveRetornarFalseQuandoUsuarioNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        assertThat(service.isGlobalByUsuarioId(usuarioId)).isFalse();
    }
}
