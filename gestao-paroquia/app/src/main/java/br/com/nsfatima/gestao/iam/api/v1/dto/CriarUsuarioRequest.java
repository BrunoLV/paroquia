package br.com.nsfatima.gestao.iam.api.v1.dto;

import br.com.nsfatima.gestao.iam.api.v1.validation.SenhaValida;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Dados para criação administrativa de um usuário (via {@code POST /api/v1/usuarios}, restrito a
 * {@code ROLE_ADMIN}) — exige {@code fielId} ou {@code clerigoId} (exclusivos).
 */
public record CriarUsuarioRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @SenhaValida String password,
        UUID fielId,
        UUID clerigoId
) {
    public CriarUsuarioRequest(String username, String password, UUID fielId) {
        this(username, password, fielId, null);
    }

    @AssertTrue(message = "Deve ser informado exatamente um vínculo exclusivo: fielId ou clerigoId")
    public boolean isPessoaValida() {
        return (fielId != null && clerigoId == null) || (fielId == null && clerigoId != null);
    }
}
