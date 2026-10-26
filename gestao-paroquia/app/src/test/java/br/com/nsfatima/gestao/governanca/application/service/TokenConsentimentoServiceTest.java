package br.com.nsfatima.gestao.governanca.application.service;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenConsentimentoServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-trinta-e-dois-caracteres";
    private static final String OUTRO_SEGREDO = "outro-segredo-qualquer-com-mais-de-trinta-e-dois-chars";

    @Test
    @DisplayName("Deve falhar o boot quando o segredo do token de consentimento estiver ausente")
    void deveRejeitarSegredoAusente() {
        assertThatThrownBy(() -> new TokenConsentimentoService(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new TokenConsentimentoService(" ")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deve falhar o boot quando o segredo for curto demais")
    void deveRejeitarSegredoCurto() {
        assertThatThrownBy(() -> new TokenConsentimentoService("curto"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_CONSENT_SECRET");
    }

    @Test
    @DisplayName("Deve rejeitar token assinado com outro segredo e aceitar o assinado com o segredo configurado")
    void deveRejeitarTokenForjadoComOutroSegredo() {
        UUID titular = UUID.randomUUID();
        UUID jti = UUID.randomUUID();
        TipoTitularConsentimento tipo = TipoTitularConsentimento.values()[0];
        String forjado = new TokenConsentimentoService(OUTRO_SEGREDO).gerarToken(tipo, titular, jti);
        TokenConsentimentoService servico = new TokenConsentimentoService(SEGREDO);

        assertThatThrownBy(() -> servico.validarToken(forjado)).isInstanceOf(IllegalArgumentException.class);
        TokenConsentimentoService.TokenPayload payload = servico.validarToken(servico.gerarToken(tipo, titular, jti));
        assertThat(payload.titularId()).isEqualTo(titular);
        assertThat(payload.jti()).isEqualTo(jti);
    }
}
