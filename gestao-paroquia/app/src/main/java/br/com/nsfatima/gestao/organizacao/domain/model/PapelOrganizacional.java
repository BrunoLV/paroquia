package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

/**
 * Enumera os 7 papéis organizacionais canônicos (Camadas B e C) que um fiel pode exercer dentro de um
 * conselho paroquial ({@code CONSELHO}) ou grupo pastoral/movimento ({@code PASTORAL}, {@code MOVIMENTO}).
 * Papéis transversais/clericais pertencem exclusivamente a {@link br.com.nsfatima.gestao.iam.domain.model.PapelGlobal}.
 */
public enum PapelOrganizacional {
    SECRETARIO_CONSELHO("secretario-conselho", "Secretário de Conselho"),
    MEMBRO_CONSELHO_PASTORAL("membro-conselho-pastoral", "Membro do Conselho Pastoral"),
    MEMBRO_CONSELHO_ECONOMICO("membro-conselho-economico", "Membro do Conselho Econômico"),
    COORDENADOR("coordenador", "Coordenador"),
    SECRETARIO_GRUPO("secretario-grupo", "Secretário de Grupo"),
    MEMBRO_ATIVO("membro-ativo", "Membro Ativo"),
    VOLUNTARIO("voluntario", "Voluntário");

    private final String storedCode;
    private final String descricao;

    PapelOrganizacional(String storedCode, String descricao) {
        this.storedCode = storedCode;
        this.descricao = descricao;
    }

    /**
     * Converte o código persistido em kebab-case ou o nome da constante enum para o
     * {@link PapelOrganizacional} correspondente, sem aliases legados.
     *
     * @param rawValue valor bruto vindo do banco de dados ou de uma requisição externa
     * @return o {@link PapelOrganizacional} correspondente, ou {@code null} se não reconhecido
     */
    public static PapelOrganizacional fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");
        for (PapelOrganizacional role : values()) {
            if (role.storedCode.equals(normalized) || role.name().equalsIgnoreCase(rawValue.trim())) {
                return role;
            }
        }
        return null;
    }

    /**
     * Retorna o código estável em kebab-case usado para persistir este papel.
     *
     * @return o código de armazenamento deste papel
     */
    public String storedValue() {
        return storedCode;
    }

    /**
     * Retorna o rótulo em português apresentado ao usuário final para este papel.
     *
     * @return a descrição legível deste papel
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Indica se este papel representa a coordenação formal de uma pastoral ou movimento.
     *
     * @return {@code true} exclusivamente quando for {@link #COORDENADOR}
     */
    public boolean isCoordenacao() {
        return this == COORDENADOR;
    }
}
