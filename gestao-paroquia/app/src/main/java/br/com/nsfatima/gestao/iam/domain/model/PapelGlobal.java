package br.com.nsfatima.gestao.iam.domain.model;

import java.util.Locale;

/**
 * Papéis de acesso irrestrito ("Roles Globais"), independentes de vínculo com qualquer organização
 * específica — diferente de {@link br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional},
 * que sempre pressupõe um escopo (organização/comunidade). Persistidos em {@code calendario.papel_global}
 * (par {@code fielId}+{@code papel}, sem {@code grupoParoquialId}), consultados via
 * {@link br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService}.
 */
public enum PapelGlobal {
    BISPO_DIOCESANO("bispo-diocesano"),
    PAROCO("paroco"),
    VIGARIO("vigario"),
    ADMIN("admin"),
    SECRETARIA_PAROQUIAL("secretaria-paroquial");

    private final String storedCode;

    PapelGlobal(String storedCode) {
        this.storedCode = storedCode;
    }

    /**
     * Converte um valor persistido/recebido de fora (código armazenado ou nome do enum) para o papel global
     * correspondente, normalizando maiúsculas/minúsculas e o separador ({@code _} vs {@code -}).
     *
     * @param rawValue valor bruto vindo do banco de dados ou de uma requisição externa
     * @return o {@link PapelGlobal} correspondente, ou {@code null} se {@code rawValue} for nulo, em branco
     *     ou não corresponder a nenhum papel global conhecido
     */
    public static PapelGlobal fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");
        for (PapelGlobal papel : values()) {
            if (papel.storedCode.equals(normalized) || papel.name().equalsIgnoreCase(rawValue.trim())) {
                return papel;
            }
        }
        return null;
    }

    /**
     * Retorna o código estável usado para persistir este papel.
     *
     * @return o código de armazenamento deste papel global
     */
    public String storedValue() {
        return storedCode;
    }
}
