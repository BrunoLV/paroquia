package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.IniciarManutencaoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoAlocacaoConflitoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IniciarManutencaoAtivoUseCaseTest {

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private AtivoAlocacaoConflitoPolicy conflitoPolicy;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    private IniciarManutencaoAtivoUseCase useCase;
    private UUID ativoId;
    private UUID grupoParoquialId;

    @BeforeEach
    void setUp() {
        useCase = new IniciarManutencaoAtivoUseCase(ativoRepository, alocacaoRepository, conflitoPolicy,
                actorContextResolver, new PatrimonioAuthorizationService());
        ativoId = UUID.randomUUID();
        grupoParoquialId = UUID.randomUUID();

        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));
        lenient().when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(ativoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AtivoPatrimonialEntity ativoDisponivel() {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.DISPONIVEL);
        ativo.setGrupoParoquialId(grupoParoquialId);
        return ativo;
    }

    @Test
    void deveIniciarManutencaoComSucessoQuandoPeriodoComecaHoje() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDisponivel()));

        var response = useCase.execute(ativoId,
                new IniciarManutencaoRequest(LocalDate.now(), LocalDate.now().plusDays(5), "Troca da lâmpada"));

        assertThat(response.status()).isEqualTo(AtivoStatus.EM_MANUTENCAO);

        ArgumentCaptor<AtivoAlocacaoEntity> captor = ArgumentCaptor.forClass(AtivoAlocacaoEntity.class);
        org.mockito.Mockito.verify(alocacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(AtivoAlocacaoTipo.MANUTENCAO);
        assertThat(captor.getValue().getStatus()).isEqualTo(AtivoAlocacaoStatus.CONFIRMADA);
    }

    @Test
    void naoDeveMudarStatusDoAtivoQuandoManutencaoComecaNoFuturo() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDisponivel()));

        var response = useCase.execute(ativoId,
                new IniciarManutencaoRequest(LocalDate.now().plusDays(10), LocalDate.now().plusDays(15),
                        "Revisão programada"));

        assertThat(response.status()).isEqualTo(AtivoStatus.DISPONIVEL);
    }

    @Test
    void deveRejeitarConflitoDeAgenda() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDisponivel()));
        doThrow(new AtivoPeriodoConflitanteException("conflito"))
                .when(conflitoPolicy).assertPeriodoDisponivel(any(), any(), any());

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new IniciarManutencaoRequest(LocalDate.now(), LocalDate.now().plusDays(2), "Troca da lâmpada")))
                .isInstanceOf(AtivoPeriodoConflitanteException.class);
    }

    @Test
    void deveRejeitarAtivoBaixado() {
        AtivoPatrimonialEntity ativo = ativoDisponivel();
        ativo.setStatus(AtivoStatus.BAIXADO);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new IniciarManutencaoRequest(LocalDate.now(), LocalDate.now().plusDays(2), "Troca da lâmpada")))
                .isInstanceOf(AtivoManutencaoInvalidaException.class);
    }

    @Test
    void deveRejeitarQuandoGrupoParoquialDoAtorDivergeDaGrupoParoquialDona() {
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativoDisponivel()));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("coordenador", "coord-pastoral-local", "PASTORAL",
                        UUID.randomUUID(), UUID.randomUUID(), null));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new IniciarManutencaoRequest(LocalDate.now(), LocalDate.now().plusDays(2), "Troca da lâmpada")))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
