package br.com.nsfatima.gestao.organizacao.domain.exception;

import java.util.UUID;

public class OrganizationBusinessException extends RuntimeException {
    public OrganizationBusinessException(String message) {
        super(message);
    }

    public static OrganizationBusinessException notFound(UUID id) {
        return new OrganizationBusinessException("Organization not found with ID: " + id);
    }

    public static OrganizationBusinessException inUse(UUID id) {
        return new OrganizationBusinessException("Cannot delete organization " + id + " because it has associated members or events.");
    }
}
