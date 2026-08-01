package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AtivoVinculacaoPolicyTest {

    @Mock
    private AtivoAlocacaoConflitoPolicy conflitoPolicy;

    private AtivoVinculacaoPolicy policy;
    private final UUID organizacaoId = UUID.randomUUID();
    private AtivoPatrimonialEntity ativo;
    private final LocalDate dataInicio = LocalDate.now().plusDays(1);
    private final LocalDate dataFim = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        policy = new AtivoVinculacaoPolicy(conflitoPolicy);
        ativo = new AtivoPatrimonialEntity();
        ativo.setId(UUID.randomUUID());
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.DISPONIVEL);
        ativo.setOrganizacaoId(organizacaoId);
        lenient().doNothing().when(conflitoPolicy).assertPeriodoDisponivel(any(), any(), any());
    }

    @Test
    void deveRejeitarQuandoAtivoEstaBaixado() {
        ativo.setStatus(AtivoStatus.BAIXADO);

        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, "Justificativa válida com 10+"))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("BAIXADO");
    }

    @Test
    void deveRejeitarDataFimAnteriorADataInicio() {
        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, dataFim, dataInicio, "Justificativa válida com 10+"))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("data final");
    }

    @Test
    void deveRejeitarJustificativaNula() {
        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, null))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("nota explicativa");
    }

    @Test
    void deveRejeitarJustificativaComMenosDeDezCaracteres() {
        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, "curta"))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("nota explicativa");
    }

    @Test
    void devePermitirAlocacaoQuandoTodasAsRegrasSaoAtendidas() {
        assertThatCode(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, "Justificativa válida com 10+"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveIgnorarFlagReservavelNaValidacao() {
        ativo.setReservavel(true);
        assertThat(ativo.isReservavel()).isTrue();

        assertThatCode(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, "Justificativa válida com 10+"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarQuandoPeriodoConflitaComOutraOcupacao() {
        doThrow(new AtivoPeriodoConflitanteException("conflito"))
                .when(conflitoPolicy).assertPeriodoDisponivel(ativo.getId(), dataInicio, dataFim);

        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, "Justificativa válida com 10+"))
                .isInstanceOf(AtivoPeriodoConflitanteException.class);
    }

    @Test
    void devePermitirAlocacaoParaOrganizacaoDiferenteDoAtivoPoisEmprestimoEDecididoPeloUseCase() {
        assertThatCode(() -> policy.assertCanAlocar(ativo, dataInicio, dataFim, "Justificativa válida com 10+"))
                .doesNotThrowAnyException();
    }
}
