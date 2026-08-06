package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.TransferirAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoTransferenciaBloqueadaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoGrupoParoquialPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferirAtivoPatrimonialUseCaseTest {

    @Mock
    private AtivoPatrimonialJpaRepository repository;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private PatrimonioAuditPublisher auditPublisher;

    @Mock
    private AtivoGrupoParoquialPolicy grupoParoquialPolicy;

    private TransferirAtivoPatrimonialUseCase useCase;
    private UUID ativoId;
    private UUID grupoParoquialOrigemId;

    @BeforeEach
    void setUp() {
        useCase = new TransferirAtivoPatrimonialUseCase(repository, actorContextResolver,
                new PatrimonioAuthorizationService(), grupoParoquialPolicy, auditPublisher);
        ativoId = UUID.randomUUID();
        grupoParoquialOrigemId = UUID.randomUUID();

        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));
    }

    @Test
    void deveLancarExcecaoQuandoAtivoNaoExiste() {
        when(repository.findById(ativoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new TransferirAtivoRequest(UUID.randomUUID(), "Justificativa da transferência")))
                .isInstanceOf(AtivoPatrimonialNotFoundException.class);
    }

    @ParameterizedTest
    @EnumSource(value = AtivoStatus.class, names = {"EM_MANUTENCAO", "ALOCADO"})
    void deveBloquearTransferenciaParaStatusIncompativel(AtivoStatus status) {
        AtivoPatrimonialEntity ativo = criarAtivo(status);
        when(repository.findById(ativoId)).thenReturn(Optional.of(ativo));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new TransferirAtivoRequest(UUID.randomUUID(), "Justificativa da transferência")))
                .isInstanceOf(AtivoTransferenciaBloqueadaException.class);
    }

    @Test
    void deveTransferirAtivoDisponivelComSucesso() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL);
        UUID novaGrupoParoquialId = UUID.randomUUID();
        when(repository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(ativoId, new TransferirAtivoRequest(novaGrupoParoquialId, "Justificativa válida"));

        assertThat(response.grupoParoquialId()).isEqualTo(novaGrupoParoquialId);
    }

    @Test
    void deveRejeitarTransferenciaParaGrupoParoquialDeTipoInvalido() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL);
        UUID novaGrupoParoquialId = UUID.randomUUID();
        when(repository.findById(ativoId)).thenReturn(Optional.of(ativo));
        doThrow(new AtivoGrupoParoquialInvalidaException("tipo inválido"))
                .when(grupoParoquialPolicy).assertGrupoParoquialProprietariaPermitida(novaGrupoParoquialId);

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new TransferirAtivoRequest(novaGrupoParoquialId, "Justificativa válida")))
                .isInstanceOf(AtivoGrupoParoquialInvalidaException.class);
    }

    private AtivoPatrimonialEntity criarAtivo(AtivoStatus status) {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(status);
        ativo.setGrupoParoquialId(grupoParoquialOrigemId);
        return ativo;
    }
}
