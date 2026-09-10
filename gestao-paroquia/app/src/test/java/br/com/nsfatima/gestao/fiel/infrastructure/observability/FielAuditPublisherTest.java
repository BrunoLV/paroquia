package br.com.nsfatima.gestao.fiel.infrastructure.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FielAuditPublisherTest {

    @Test
    void devePublicarDecisaoSemValoresCadastrais() {
        AuditoriaPersistenceService auditoria = org.mockito.Mockito.mock(AuditoriaPersistenceService.class);
        FielAuditPublisher publisher = new FielAuditPublisher(auditoria);
        UUID solicitacaoId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();
        UUID atorId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        publisher.publicarDecisaoAjuste(solicitacaoId, fielId, atorId, unidadeId, false);

        ArgumentCaptor<Map<String, Object>> metadata = ArgumentCaptor.captor();
        verify(auditoria).log(eq(atorId.toString()), eq("aprovacao-decisao"),
                eq(solicitacaoId.toString()), eq("rejected"), metadata.capture());
        assertThat(metadata.getValue())
                .containsEntry("resourceType", "SOLICITACAO_AJUSTE_CADASTRAL")
                .containsEntry("status", "REJECTED")
                .doesNotContainKeys("email", "celular", "telefoneFixo", "endereco", "justificativa");
    }

    @Test
    void devePublicarInativacaoComoSoftDelete() {
        AuditoriaPersistenceService auditoria = org.mockito.Mockito.mock(AuditoriaPersistenceService.class);
        FielAuditPublisher publisher = new FielAuditPublisher(auditoria);
        UUID parId = UUID.randomUUID();
        UUID fielId = UUID.randomUUID();
        UUID atorId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        publisher.publicarInativacaoRelacionamento(parId, fielId, atorId, unidadeId);

        ArgumentCaptor<Map<String, Object>> metadata = ArgumentCaptor.captor();
        verify(auditoria).log(eq(atorId.toString()), eq("cancelar"), eq(parId.toString()),
                eq("success"), metadata.capture());
        assertThat(metadata.getValue())
                .containsEntry("resourceType", "RELACIONAMENTO_FAMILIAR")
                .containsEntry("modo", "SOFT_DELETE")
                .doesNotContainKeys("nome", "cpf", "email", "celular", "endereco");
    }
}
