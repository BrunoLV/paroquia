package br.com.nsfatima.gestao.patrimonio.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.application.usecase.AlocarAtivoParaEventoUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlocacaoAtivoExecutionStrategyTest {

    @Mock
    private AlocarAtivoParaEventoUseCase alocarAtivoParaEventoUseCase;

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    private AlocacaoAtivoExecutionStrategy strategy;
    private UUID ativoId;
    private UUID grupoParoquialDonaId;

    @BeforeEach
    void setUp() {
        strategy = new AlocacaoAtivoExecutionStrategy(alocarAtivoParaEventoUseCase, ativoRepository,
                alocacaoRepository, new AprovacaoAcaoPayloadMapper(new ObjectMapper()));
        ativoId = UUID.randomUUID();
        grupoParoquialDonaId = UUID.randomUUID();
    }

    private AtivoPatrimonialEntity ativoDaGrupoParoquial(UUID grupoParoquialId) {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.DISPONIVEL);
        ativo.setGrupoParoquialId(grupoParoquialId);
        return ativo;
    }

    private AprovacaoEntity aprovacaoComAtivo() {
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(UUID.randomUUID());
        aprovacao.setActionPayloadJson("""
                {"ativoId":"%s"}
                """.formatted(ativoId));
        return aprovacao;
    }

    @Test
    void deveRejeitarCoordenadorDeConselhoDeGrupoParoquialErrada() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDaGrupoParoquial(grupoParoquialDonaId)));
        EventoActorContext actorContext = new EventoActorContext("coordenador", "presidente-cpc", "CPC",
                UUID.randomUUID(), UUID.randomUUID(), null);

        assertThatThrownBy(() -> strategy.execute(aprovacaoComAtivo(), actorContext))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void devePermitirCoordenadorDeConselhoDaGrupoParoquialCorreta() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDaGrupoParoquial(grupoParoquialDonaId)));
        when(alocarAtivoParaEventoUseCase.executeApprovedAlocacao(any(), any(), any()))
                .thenReturn(respostaAlocado());
        EventoActorContext actorContext = new EventoActorContext("coordenador", "presidente-cpc", "CPC",
                grupoParoquialDonaId, UUID.randomUUID(), null);

        var execution = strategy.execute(aprovacaoComAtivo(), actorContext);

        assertThat(execution.outcome()).isEqualTo("EXECUTED");
    }

    @Test
    void devePermitirParocoDeQualquerGrupoParoquial() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDaGrupoParoquial(grupoParoquialDonaId)));
        when(alocarAtivoParaEventoUseCase.executeApprovedAlocacao(any(), any(), any()))
                .thenReturn(respostaAlocado());
        EventoActorContext actorContext = new EventoActorContext("paroco", "paroco", "CLERO",
                UUID.randomUUID(), UUID.randomUUID(), null);

        var execution = strategy.execute(aprovacaoComAtivo(), actorContext);

        assertThat(execution.outcome()).isEqualTo("EXECUTED");
    }

    @Test
    void devePermitirAdminDeQualquerGrupoParoquial() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDaGrupoParoquial(grupoParoquialDonaId)));
        when(alocarAtivoParaEventoUseCase.executeApprovedAlocacao(any(), any(), any()))
                .thenReturn(respostaAlocado());
        EventoActorContext actorContext = new EventoActorContext("admin", "admin", "CLERO",
                UUID.randomUUID(), UUID.randomUUID(), null);

        var execution = strategy.execute(aprovacaoComAtivo(), actorContext);

        assertThat(execution.outcome()).isEqualTo("EXECUTED");
    }

    @Test
    void deveRejeitarPapelSemPermissaoAlguma() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDaGrupoParoquial(grupoParoquialDonaId)));
        EventoActorContext actorContext = new EventoActorContext("voluntario", "voluntario-pastoral-local", "PASTORAL",
                grupoParoquialDonaId, UUID.randomUUID(), null);

        assertThatThrownBy(() -> strategy.execute(aprovacaoComAtivo(), actorContext))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void onRejectedDeveMarcarOcupacaoPendenteComoCancelada() {
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setStatus(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus.PENDENTE_APROVACAO);
        AprovacaoEntity aprovacao = aprovacaoComAtivo();
        when(alocacaoRepository.findByAprovacaoId(aprovacao.getId())).thenReturn(Optional.of(ocupacao));

        strategy.onRejected(aprovacao);

        assertThat(ocupacao.getStatus())
                .isEqualTo(br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus.CANCELADA);
    }

    @Test
    void supportsDeveAceitarApenasAlocacaoAtivoEmprestimo() {
        assertThat(strategy.supports(br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput
                .ALOCACAO_ATIVO_EMPRESTIMO)).isTrue();
        assertThat(strategy.supports(br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput
                .CANCELAMENTO)).isFalse();
    }

    private AtivoPatrimonialResponse respostaAlocado() {
        return new AtivoPatrimonialResponse(ativoId, "Projetor", "PAT-2026-0001", AtivoCategoria.ELETRONICO,
                AtivoStatus.ALOCADO, true, grupoParoquialDonaId, null, null, Instant.now());
    }
}
