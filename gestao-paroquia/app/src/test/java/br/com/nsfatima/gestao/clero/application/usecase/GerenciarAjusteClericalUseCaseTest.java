package br.com.nsfatima.gestao.clero.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitarAjusteClericalRequest;
import br.com.nsfatima.gestao.clero.infrastructure.observability.ClerigoAuditPublisher;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.SolicitacaoAjusteClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.SolicitacaoAjusteClericalJpaRepository;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GerenciarAjusteClericalUseCaseTest {

    @Mock private UsuarioJpaRepository usuarios;
    @Mock private ClerigoJpaRepository clerigos;
    @Mock private SolicitacaoAjusteClericalJpaRepository ajustes;
    @Mock private GerenciarNotificacaoUseCase notificacoes;
    @Mock private ClerigoAuditPublisher auditoria;

    @Test
    void deveAplicarSolicitacaoAprovadaESemExporValoresNaAuditoria() {
        UUID usuarioId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setClerigoId(clerigoId);
        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setId(clerigoId);
        SolicitacaoAjusteClericalEntity ajuste = new SolicitacaoAjusteClericalEntity();
        ajuste.setId(UUID.randomUUID());
        ajuste.setClerigoId(clerigoId);
        ajuste.setUsuarioSolicitanteId(usuarioId);
        ajuste.setStatus("PENDENTE");
        ajuste.setEmail("novo@paroquia.test");
        ajuste.setCelular("11999998888");
        ajuste.setTelefoneFixo("1133334444");
        ajuste.setEndereco("Casa Paroquial");
        when(ajustes.findById(ajuste.getId())).thenReturn(Optional.of(ajuste));
        when(clerigos.findById(clerigoId)).thenReturn(Optional.of(clerigo));


        GerenciarAjusteClericalUseCase useCase = new GerenciarAjusteClericalUseCase(
                usuarios, clerigos, ajustes, notificacoes, auditoria);

        useCase.decidir(ajuste.getId(), UUID.randomUUID(), true, null);

        assertEquals("novo@paroquia.test", clerigo.getEmail());
        assertEquals("11999998888", clerigo.getCelular());
        assertFalse(ajuste.getStatus().equals("PENDENTE"));
        verify(auditoria).publicarDecisaoAjuste(any(), any(), any(), anyBoolean());
        verify(notificacoes).publicar(usuarioId, "Sua solicitação de ajuste cadastral foi aprovada.");
    }

    @Test
    void deveRejeitarSolicitacaoComJustificativa() {
        UUID usuarioId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();
        SolicitacaoAjusteClericalEntity ajuste = new SolicitacaoAjusteClericalEntity();
        ajuste.setId(UUID.randomUUID());
        ajuste.setClerigoId(clerigoId);
        ajuste.setUsuarioSolicitanteId(usuarioId);
        ajuste.setStatus("PENDENTE");
        when(ajustes.findById(ajuste.getId())).thenReturn(Optional.of(ajuste));

        GerenciarAjusteClericalUseCase useCase = new GerenciarAjusteClericalUseCase(
                usuarios, clerigos, ajustes, notificacoes, auditoria);

        useCase.decidir(ajuste.getId(), UUID.randomUUID(), false, "Documento inconsistente");

        assertEquals("REJEITADA", ajuste.getStatus());
        assertEquals("Documento inconsistente", ajuste.getJustificativaAnalise());
        verify(notificacoes).publicar(usuarioId, "Sua solicitação de ajuste cadastral foi rejeitada.");
    }

    @Test
    void deveListarSolicitacoesPendentes() {
        SolicitacaoAjusteClericalEntity ajuste = new SolicitacaoAjusteClericalEntity();
        ajuste.setId(UUID.randomUUID());
        ajuste.setClerigoId(UUID.randomUUID());
        ajuste.setStatus("PENDENTE");
        ajuste.setEmail("email@test.com");
        when(ajustes.findByStatusOrderByCriadoEmUtcAsc("PENDENTE")).thenReturn(List.of(ajuste));

        GerenciarAjusteClericalUseCase useCase = new GerenciarAjusteClericalUseCase(
                usuarios, clerigos, ajustes, notificacoes, auditoria);

        var lista = useCase.listarPendentes();
        assertEquals(1, lista.size());
        assertEquals("email@test.com", lista.get(0).email());
    }

    @Test
    void deveCriarSolicitacaoParaOClerigoDoUsuarioAutenticado() {
        UUID usuarioId = UUID.randomUUID();
        UUID clerigoId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setClerigoId(clerigoId);
        ClerigoEntity clerigo = new ClerigoEntity();
        clerigo.setId(clerigoId);
        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(clerigos.findById(clerigoId)).thenReturn(Optional.of(clerigo));
        when(ajustes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        GerenciarAjusteClericalUseCase useCase = new GerenciarAjusteClericalUseCase(
                usuarios, clerigos, ajustes, notificacoes, auditoria);
        useCase.solicitar(usuarioId, new SolicitarAjusteClericalRequest(
                "novo@paroquia.test", "(11) 99999-8888", null, "Casa Paroquial"));

        ArgumentCaptor<SolicitacaoAjusteClericalEntity> captor =
                ArgumentCaptor.forClass(SolicitacaoAjusteClericalEntity.class);
        verify(ajustes).save(captor.capture());
        assertEquals(clerigoId, captor.getValue().getClerigoId());
        assertEquals("11999998888", captor.getValue().getCelular());
        verify(auditoria).publicarSolicitacaoAjuste(any(), any(), any());
    }
}
