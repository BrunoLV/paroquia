package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ConflitoAgendaEclesialException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica a hierarquia da agenda sem banco, Spring ou entidades de persistência. */
@DisplayName("Policy: hierarquia de prioridade da agenda")
class HierarquiaPrioridadeAgendaPolicyTest {

    private final HierarquiaPrioridadeAgendaPolicy policy = new HierarquiaPrioridadeAgendaPolicy();

    /** RN-CAL-PRI-01: ausência de local e igreja não representa um espaço compartilhado. */
    @Test
    @DisplayName("não considera dois identificadores ausentes como o mesmo espaço")
    void naoConsideraIdentificadoresAusentesComoMesmoEspaco() {
        PrioridadeAgendaPolicy.EventoSobreposto novo = evento(3, null, null);
        PrioridadeAgendaPolicy.EventoSobreposto existente = evento(3, null, null);

        assertThatCode(() -> policy.resolver(novo, List.of(existente))).doesNotThrowAnyException();
    }

    /** RN-CAL-PRI-02: movimentos não sobrepõem liturgia no mesmo local. */
    @Test
    @DisplayName("rejeita movimento que sobrepoe liturgia no mesmo local")
    void rejeitaMovimentoQueSobrepoeLiturgiaNoMesmoLocal() {
        UUID localId = UUID.randomUUID();

        assertThatThrownBy(() -> policy.resolver(evento(4, localId, null), List.of(evento(2, localId, null))))
                .isInstanceOf(ConflitoAgendaEclesialException.class);
    }

    private PrioridadeAgendaPolicy.EventoSobreposto evento(int prioridade, UUID localId, UUID igrejaId) {
        return new PrioridadeAgendaPolicy.EventoSobreposto(UUID.randomUUID(), prioridade, localId, igrejaId);
    }
}
