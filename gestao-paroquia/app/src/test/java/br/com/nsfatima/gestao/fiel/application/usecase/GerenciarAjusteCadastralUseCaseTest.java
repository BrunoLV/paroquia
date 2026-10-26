package br.com.nsfatima.gestao.fiel.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.nsfatima.gestao.fiel.api.v1.dto.EnderecoDto;
import br.com.nsfatima.gestao.fiel.api.v1.dto.SolicitarAjusteCadastralRequest;
import br.com.nsfatima.gestao.fiel.infrastructure.observability.FielAuditPublisher;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.SolicitacaoAjusteCadastralEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.SolicitacaoAjusteCadastralJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GerenciarAjusteCadastralUseCaseTest {

    @Mock private UsuarioJpaRepository usuarios;
    @Mock private FielJpaRepository fieis;
    @Mock private SolicitacaoAjusteCadastralJpaRepository ajustes;
    @Mock private GerenciarNotificacaoUseCase notificacoes;
    @Mock private FielAuditPublisher auditoria;
    @Captor private ArgumentCaptor<SolicitacaoAjusteCadastralEntity> solicitacaoCaptor;
    @Captor private ArgumentCaptor<FielEntity> fielCaptor;

    private final UUID fielId = UUID.randomUUID();
    private final UUID unidadeId = UUID.randomUUID();

    private GerenciarAjusteCadastralUseCase useCase() {
        return new GerenciarAjusteCadastralUseCase(usuarios, fieis, ajustes, notificacoes, auditoria);
    }

    private FielEntity fiel() {
        FielEntity fiel = new FielEntity();
        fiel.setId(fielId);
        fiel.setUnidadeTerritorialId(unidadeId);
        return fiel;
    }

    private UsuarioEntity usuario(UUID usuarioId) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        usuario.setFielId(fielId);
        return usuario;
    }

    private SolicitarAjusteCadastralRequest request() {
        return new SolicitarAjusteCadastralRequest(
                "novo@email.com", "11999990000", "1133330000",
                new EnderecoDto("01000-000", "Rua Nova", "10", "Ap 1", "Centro", "São Paulo", "SP"));
    }

    @Test
    void solicitar_deveCriarSolicitacaoPendenteEPublicarAuditoria() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId)));
        when(fieis.findById(fielId)).thenReturn(Optional.of(fiel()));
        when(ajustes.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        UUID solicitacaoId = useCase().solicitar(usuarioId, request());

        assertThat(solicitacaoId).isNotNull();
        verify(ajustes).save(solicitacaoCaptor.capture());
        SolicitacaoAjusteCadastralEntity salva = solicitacaoCaptor.getValue();
        assertThat(salva.getStatus()).isEqualTo("PENDENTE");
        assertThat(salva.getFielId()).isEqualTo(fielId);
        assertThat(salva.getUsuarioSolicitanteId()).isEqualTo(usuarioId);
        assertThat(salva.getEmail()).isEqualTo("novo@email.com");
        assertThat(salva.getEnderecoLogradouro()).isEqualTo("Rua Nova");
        verify(auditoria).publicarSolicitacaoAjuste(eq(salva.getId()), eq(fielId), eq(usuarioId), eq(unidadeId));
    }

    @Test
    void solicitar_deveLancarQuandoUsuarioNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarios.findById(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().solicitar(usuarioId, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuário não encontrado.");
    }

    @Test
    void solicitar_deveLancarQuandoFielNaoEncontrado() {
        UUID usuarioId = UUID.randomUUID();
        when(usuarios.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId)));
        when(fieis.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().solicitar(usuarioId, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fiel não encontrado.");
    }

    @Test
    void pendentes_deveMapearSolicitacoesPendentesOrdenadas() {
        SolicitacaoAjusteCadastralEntity entity = solicitacaoPendente();
        when(ajustes.findByStatusOrderByCriadoEmUtcAsc("PENDENTE")).thenReturn(List.of(entity));

        var resultado = useCase().pendentes();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(entity.getId());
        assertThat(resultado.get(0).fielId()).isEqualTo(fielId);
        assertThat(resultado.get(0).endereco().logradouro()).isEqualTo("Rua Nova");
    }

    @Test
    void pendentesDoFiel_deveMapearSolicitacoesDoFielEspecifico() {
        SolicitacaoAjusteCadastralEntity entity = solicitacaoPendente();
        when(ajustes.findByFielIdAndStatusOrderByCriadoEmUtcDesc(fielId, "PENDENTE")).thenReturn(List.of(entity));

        var resultado = useCase().pendentesDoFiel(fielId);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).fielId()).isEqualTo(fielId);
    }

    @Test
    void decidir_aprovar_deveAtualizarFielEPublicarNotificacaoAprovada() {
        UUID analista = UUID.randomUUID();
        SolicitacaoAjusteCadastralEntity solicitacao = solicitacaoPendente();
        FielEntity fielExistente = fiel();
        when(ajustes.findById(solicitacao.getId())).thenReturn(Optional.of(solicitacao));
        when(fieis.findById(fielId)).thenReturn(Optional.of(fielExistente));

        useCase().decidir(solicitacao.getId(), analista, true, "Dados conferem");

        verify(fieis).save(fielCaptor.capture());
        assertThat(fielCaptor.getValue().getEmail()).isEqualTo("novo@email.com");
        assertThat(fielCaptor.getValue().getEnderecoLogradouro()).isEqualTo("Rua Nova");
        verify(ajustes).save(solicitacaoCaptor.capture());
        assertThat(solicitacaoCaptor.getValue().getStatus()).isEqualTo("APROVADO");
        assertThat(solicitacaoCaptor.getValue().getJustificativaAnalise()).isEqualTo("Dados conferem");
        assertThat(solicitacaoCaptor.getValue().getAnalisadoPorUsuarioId()).isEqualTo(analista);
        verify(auditoria).publicarDecisaoAjuste(
                eq(solicitacao.getId()), eq(fielId), eq(analista), eq(unidadeId), eq(true));
        verify(notificacoes).publicar(eq(solicitacao.getUsuarioSolicitanteId()), anyString());
    }

    @Test
    void decidir_reprovar_naoDeveAlterarDadosDoFiel() {
        UUID analista = UUID.randomUUID();
        SolicitacaoAjusteCadastralEntity solicitacao = solicitacaoPendente();
        when(ajustes.findById(solicitacao.getId())).thenReturn(Optional.of(solicitacao));
        when(fieis.findById(fielId)).thenReturn(Optional.of(fiel()));

        useCase().decidir(solicitacao.getId(), analista, false, "Documentação insuficiente");

        verify(fieis, never()).save(any());
        verify(ajustes).save(solicitacaoCaptor.capture());
        assertThat(solicitacaoCaptor.getValue().getStatus()).isEqualTo("REJEITADO");
        verify(auditoria).publicarDecisaoAjuste(
                eq(solicitacao.getId()), eq(fielId), eq(analista), eq(unidadeId), eq(false));
        verify(notificacoes, times(1)).publicar(any(), anyString());
    }

    @Test
    void decidir_deveLancarQuandoSolicitacaoNaoEncontrada() {
        UUID id = UUID.randomUUID();
        when(ajustes.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().decidir(id, UUID.randomUUID(), true, "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solicitação não encontrada.");
    }

    @Test
    void decidir_deveLancarQuandoSolicitacaoJaAnalisada() {
        SolicitacaoAjusteCadastralEntity solicitacao = solicitacaoPendente();
        solicitacao.setStatus("APROVADO");
        when(ajustes.findById(solicitacao.getId())).thenReturn(Optional.of(solicitacao));

        assertThatThrownBy(() -> useCase().decidir(solicitacao.getId(), UUID.randomUUID(), true, "x"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Solicitação já analisada.");
    }

    @Test
    void decidir_deveLancarQuandoFielNaoEncontrado() {
        SolicitacaoAjusteCadastralEntity solicitacao = solicitacaoPendente();
        when(ajustes.findById(solicitacao.getId())).thenReturn(Optional.of(solicitacao));
        when(fieis.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().decidir(solicitacao.getId(), UUID.randomUUID(), true, "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fiel não encontrado.");
    }

    private SolicitacaoAjusteCadastralEntity solicitacaoPendente() {
        SolicitacaoAjusteCadastralEntity entity = new SolicitacaoAjusteCadastralEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setUsuarioSolicitanteId(UUID.randomUUID());
        entity.setEmail("novo@email.com");
        entity.setCelular("11999990000");
        entity.setTelefoneFixo("1133330000");
        entity.setEnderecoLogradouro("Rua Nova");
        entity.setEnderecoNumero("10");
        entity.setEnderecoComplemento("Ap 1");
        entity.setEnderecoBairro("Centro");
        entity.setEnderecoCep("01000-000");
        entity.setEnderecoCidade("São Paulo");
        entity.setEnderecoEstado("SP");
        entity.setStatus("PENDENTE");
        return entity;
    }
}
