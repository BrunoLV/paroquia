package br.com.nsfatima.gestao.organizacao.domain.exception;

import java.util.UUID;

public class OrganizacaoNegocioException extends RuntimeException {
    public OrganizacaoNegocioException(String message) {
        super(message);
    }

    public static OrganizacaoNegocioException notFound(UUID id) {
        return new OrganizacaoNegocioException("Organization not found with ID: " + id);
    }

    public static OrganizacaoNegocioException inUse(UUID id) {
        return new OrganizacaoNegocioException(
                "Cannot delete organization " + id + " because it has associated members or events.");
    }
}
