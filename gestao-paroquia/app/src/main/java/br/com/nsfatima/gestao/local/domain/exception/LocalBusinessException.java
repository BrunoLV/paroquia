package br.com.nsfatima.gestao.local.domain.exception;

import java.util.UUID;

public class LocalBusinessException extends RuntimeException {
    public LocalBusinessException(String message) {
        super(message);
    }

    public static LocalBusinessException notFound(UUID id) {
        return new LocalBusinessException("Location not found with ID: " + id);
    }

    public static LocalBusinessException inUse(UUID id) {
        return new LocalBusinessException("Cannot delete location " + id + " because it has associated events.");
    }
}
