package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CancelarAlocacaoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoCancelamentoInvalidoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarAlocacaoAtivoUseCaseTest {

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private AprovacaoJpaRepository aprovacaoRepository;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    private CancelarAlocacaoAtivoUseCase useCase;
    private UUID alocacaoId;
    private UUID organizacaoId;
    private UUID solicitanteId;

    @BeforeEach
    void setUp() {
        useCase = new CancelarAlocacaoAtivoUseCase(alocacaoRepository, aprovacaoRepository, actorContextResolver,
                new PatrimonioAuthorizationService());
        alocacaoId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();
        solicitanteId = UUID.randomUUID();

        lenient().when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(aprovacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AtivoAlocacaoEntity reservaFutura(AtivoAlocacaoStatus status) {
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(alocacaoId);
        ocupacao.setTipo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        ocupacao.setOrganizacaoOrigemId(organizacaoId);
        ocupacao.setDataInicio(LocalDate.now().plusDays(5));
        ocupacao.setDataFim(LocalDate.now().plusDays(7));
        ocupacao.setStatus(status);
        ocupacao.setSolicitanteId(solicitanteId);
        return ocupacao;
    }

    @Test
    void deveCancelarPedidoPendenteQuandoSolicitanteCancelaOProprio() {
        AtivoAlocacaoEntity ocupacao = reservaFutura(AtivoAlocacaoStatus.PENDENTE_APROVACAO);
        UUID aprovacaoId = UUID.randomUUID();
        ocupacao.setAprovacaoId(aprovacaoId);
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);

        when(alocacaoRepository.findById(alocacaoId)).thenReturn(Optional.of(ocupacao));
        when(aprovacaoRepository.findById(aprovacaoId)).thenReturn(Optional.of(aprovacao));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("solicitante", "coord-pastoral-local", "PASTORAL",
                        UUID.randomUUID(), solicitanteId, null));

        useCase.execute(alocacaoId, new CancelarAlocacaoRequest("Não será mais necessário"));

        assertThat(ocupacao.getStatus()).isEqualTo(AtivoAlocacaoStatus.CANCELADA);
        assertThat(ocupacao.getMotivoCancelamento()).isEqualTo("Não será mais necessário");
        assertThat(aprovacao.getStatus()).isEqualTo(AprovacaoStatus.REPROVADA);
    }

    @Test
    void deveCancelarReservaConfirmadaFuturaPeloResponsavelDaOrganizacao() {
        AtivoAlocacaoEntity ocupacao = reservaFutura(AtivoAlocacaoStatus.CONFIRMADA);
        ocupacao.setSolicitanteId(UUID.randomUUID());

        when(alocacaoRepository.findById(alocacaoId)).thenReturn(Optional.of(ocupacao));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));

        useCase.execute(alocacaoId, new CancelarAlocacaoRequest("Reorganização de agenda"));

        assertThat(ocupacao.getStatus()).isEqualTo(AtivoAlocacaoStatus.CANCELADA);
    }

    @Test
    void deveRejeitarCancelamentoDeOcupacaoJaEmCurso() {
        AtivoAlocacaoEntity ocupacao = reservaFutura(AtivoAlocacaoStatus.CONFIRMADA);
        ocupacao.setDataInicio(LocalDate.now().minusDays(1));
        when(alocacaoRepository.findById(alocacaoId)).thenReturn(Optional.of(ocupacao));

        assertThatThrownBy(() -> useCase.execute(alocacaoId, new CancelarAlocacaoRequest("Motivo qualquer válido")))
                .isInstanceOf(AtivoAlocacaoCancelamentoInvalidoException.class);
    }

    @Test
    void deveRejeitarUsuarioSemPermissaoNemDonoDoPedido() {
        AtivoAlocacaoEntity ocupacao = reservaFutura(AtivoAlocacaoStatus.PENDENTE_APROVACAO);
        when(alocacaoRepository.findById(alocacaoId)).thenReturn(Optional.of(ocupacao));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("outro", "voluntario-pastoral-local", "PASTORAL",
                        UUID.randomUUID(), UUID.randomUUID(), null));

        assertThatThrownBy(() -> useCase.execute(alocacaoId, new CancelarAlocacaoRequest("Motivo qualquer válido")))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
