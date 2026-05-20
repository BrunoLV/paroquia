package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import br.com.nsfatima.gestao.aprovacao.application.usecase.CreateSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.calendario.support.fake.FakeAprovacaoRepository;
import br.com.nsfatima.gestao.calendario.support.fake.FakeEventoActorContextResolver;
import br.com.nsfatima.gestao.calendario.support.fake.FakeLegacyEnumInconsistencyPublisher;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AprovacaoHorarioIntegrationTest {

    @Test
    void shouldCreateApprovalRequest() {
        FakeEventoActorContextResolver resolver = new FakeEventoActorContextResolver();
        FakeAprovacaoRepository repository = new FakeAprovacaoRepository();
        FakeLegacyEnumInconsistencyPublisher publisher = new FakeLegacyEnumInconsistencyPublisher();
        AprovacaoMapper mapper = new AprovacaoMapper(publisher);

        CreateSolicitacaoAprovacaoUseCase useCase = new CreateSolicitacaoAprovacaoUseCase(
                repository,
                resolver,
                null,
                mapper);
        
        assertNotNull(useCase.create(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                TipoSolicitacaoInput.ALTERACAO_HORARIO));
    }
}
