package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtivoVinculacaoPolicyTest {

    private final AtivoVinculacaoPolicy policy = new AtivoVinculacaoPolicy();
    private final UUID organizacaoId = UUID.randomUUID();
    private AtivoPatrimonialEntity ativo;

    @BeforeEach
    void setUp() {
        ativo = new AtivoPatrimonialEntity();
        ativo.setId(UUID.randomUUID());
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.DISPONIVEL);
        ativo.setOrganizacaoId(organizacaoId);
    }

    @Test
    void deveRejeitarQuandoAtivoNaoEstaDisponivel() {
        ativo.setStatus(AtivoStatus.EM_MANUTENCAO);

        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, organizacaoId, "Justificativa válida com 10+ chars"))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("DISPONIVEL");
    }

    @Test
    void deveRejeitarQuandoOrganizacaoDoLocalDivergeDaOrganizacaoDoAtivo() {
        UUID outraOrganizacaoId = UUID.randomUUID();

        assertThatThrownBy(() ->
                policy.assertCanAlocar(ativo, outraOrganizacaoId, "Justificativa válida com 10+ chars"))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("outras comunidades");
    }

    @Test
    void deveRejeitarJustificativaNula() {
        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, organizacaoId, null))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("nota explicativa");
    }

    @Test
    void deveRejeitarJustificativaComMenosDeDezCaracteres() {
        assertThatThrownBy(() -> policy.assertCanAlocar(ativo, organizacaoId, "curta"))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class)
                .hasMessageContaining("nota explicativa");
    }

    @Test
    void devePermitirAlocacaoQuandoTodasAsRegrasSaoAtendidas() {
        assertThatCode(() -> policy.assertCanAlocar(ativo, organizacaoId, "Justificativa válida com 10+ chars"))
                .doesNotThrowAnyException();
    }

    @Test
    void deveIgnorarFlagReservavelNaValidacao() {
        ativo.setReservavel(true);
        assertThat(ativo.isReservavel()).isTrue();

        assertThatCode(() -> policy.assertCanAlocar(ativo, organizacaoId, "Justificativa válida com 10+ chars"))
                .doesNotThrowAnyException();
    }
}
