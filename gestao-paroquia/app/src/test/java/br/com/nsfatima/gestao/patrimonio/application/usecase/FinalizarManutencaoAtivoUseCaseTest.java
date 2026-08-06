package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizarManutencaoAtivoUseCaseTest {

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    private FinalizarManutencaoAtivoUseCase useCase;
    private UUID ativoId;
    private UUID grupoParoquialId;

    @BeforeEach
    void setUp() {
        useCase = new FinalizarManutencaoAtivoUseCase(ativoRepository, alocacaoRepository, actorContextResolver,
                new PatrimonioAuthorizationService());
        ativoId = UUID.randomUUID();
        grupoParoquialId = UUID.randomUUID();

        lenient().when(ativoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AtivoPatrimonialEntity ativoEmManutencao() {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.EM_MANUTENCAO);
        ativo.setGrupoParoquialId(grupoParoquialId);
        return ativo;
    }

    private AtivoAlocacaoEntity manutencao(AtivoAlocacaoStatus status) {
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setTipo(AtivoAlocacaoTipo.MANUTENCAO);
        ocupacao.setDataInicio(LocalDate.now().minusDays(5));
        ocupacao.setDataFim(LocalDate.now().minusDays(1));
        ocupacao.setStatus(status);
        return ocupacao;
    }

    @ParameterizedTest
    @EnumSource(value = AtivoAlocacaoStatus.class, names = {"CONFIRMADA", "ATRASADA"})
    void deveFinalizarManutencaoComSucesso(AtivoAlocacaoStatus status) {
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoEmManutencao()));
        AtivoAlocacaoEntity manutencao = manutencao(status);
        when(alocacaoRepository.findByAtivoIdAndStatusIn(ativoId, List.of(AtivoAlocacaoStatus.CONFIRMADA,
                AtivoAlocacaoStatus.ATRASADA))).thenReturn(List.of(manutencao));

        var response = useCase.execute(ativoId);

        assertThat(response.status()).isEqualTo(AtivoStatus.DISPONIVEL);
        assertThat(manutencao.getStatus()).isEqualTo(AtivoAlocacaoStatus.DEVOLVIDA);
        assertThat(manutencao.getDevolvidoEmUtc()).isNotNull();
    }

    @Test
    void deveRejeitarQuandoNaoHaManutencaoCorrente() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoEmManutencao()));
        when(alocacaoRepository.findByAtivoIdAndStatusIn(any(), any())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(ativoId))
                .isInstanceOf(AtivoManutencaoInvalidaException.class);
    }

    @Test
    void deveRejeitarQuandoGrupoParoquialDoAtorDivergeDaGrupoParoquialDona() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoEmManutencao()));
        when(alocacaoRepository.findByAtivoIdAndStatusIn(any(), any()))
                .thenReturn(List.of(manutencao(AtivoAlocacaoStatus.CONFIRMADA)));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("coordenador", "coord-pastoral-local", "PASTORAL",
                        UUID.randomUUID(), UUID.randomUUID(), null));

        assertThatThrownBy(() -> useCase.execute(ativoId))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
