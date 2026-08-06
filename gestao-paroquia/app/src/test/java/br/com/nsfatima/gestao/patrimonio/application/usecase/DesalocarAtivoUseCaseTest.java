package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.DesalocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.application.usecase.observacao.CreateNotaAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoDesalocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DesalocarAtivoUseCaseTest {

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private CreateNotaAtivoPatrimonialUseCase createNotaUseCase;

    @Mock
    private PatrimonioAuditPublisher auditPublisher;

    private DesalocarAtivoUseCase useCase;
    private UUID ativoId;
    private UUID grupoParoquialId;

    @BeforeEach
    void setUp() {
        useCase = new DesalocarAtivoUseCase(ativoRepository, alocacaoRepository, actorContextResolver,
                new PatrimonioAuthorizationService(), createNotaUseCase, auditPublisher);
        ativoId = UUID.randomUUID();
        grupoParoquialId = UUID.randomUUID();

        lenientMocks();
    }

    private void lenientMocks() {
        org.mockito.Mockito.lenient().when(ativoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        org.mockito.Mockito.lenient().when(alocacaoRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AtivoPatrimonialEntity ativoAlocado() {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.ALOCADO);
        ativo.setGrupoParoquialId(grupoParoquialId);
        return ativo;
    }

    private AtivoAlocacaoEntity ocupacaoAtiva(AtivoAlocacaoStatus status) {
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setTipo(AtivoAlocacaoTipo.ALOCACAO_EVENTO);
        ocupacao.setDataInicio(LocalDate.now().minusDays(1));
        ocupacao.setDataFim(LocalDate.now());
        ocupacao.setStatus(status);
        return ocupacao;
    }

    @ParameterizedTest
    @EnumSource(value = AtivoAlocacaoStatus.class, names = {"CONFIRMADA", "ATRASADA"})
    void deveDesalocarComSucessoAPartirDeStatusValidos(AtivoAlocacaoStatus status) {
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoAlocado()));
        AtivoAlocacaoEntity ocupacao = ocupacaoAtiva(status);
        when(alocacaoRepository.findByAtivoIdAndStatusIn(ativoId, List.of(AtivoAlocacaoStatus.CONFIRMADA,
                AtivoAlocacaoStatus.ATRASADA))).thenReturn(List.of(ocupacao));

        var response = useCase.execute(ativoId, new DesalocarAtivoRequest("Devolvido após o evento"));

        assertThat(response.status()).isEqualTo(AtivoStatus.DISPONIVEL);
        assertThat(ocupacao.getStatus()).isEqualTo(AtivoAlocacaoStatus.DEVOLVIDA);
        assertThat(ocupacao.getDevolvidoEmUtc()).isNotNull();
    }

    @Test
    void deveRejeitarQuandoNaoHaOcupacaoCorrente() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoAlocado()));
        when(alocacaoRepository.findByAtivoIdAndStatusIn(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(ativoId, null))
                .isInstanceOf(AtivoDesalocacaoInvalidaException.class);
    }

    @Test
    void deveRejeitarQuandoGrupoParoquialDoAtorDivergeDaGrupoParoquialDona() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoAlocado()));
        when(alocacaoRepository.findByAtivoIdAndStatusIn(any(), any()))
                .thenReturn(List.of(ocupacaoAtiva(AtivoAlocacaoStatus.CONFIRMADA)));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("coordenador", "coord-pastoral-local", "PASTORAL",
                        UUID.randomUUID(), UUID.randomUUID(), null));

        assertThatThrownBy(() -> useCase.execute(ativoId, null))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
