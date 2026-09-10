package br.com.nsfatima.gestao.organizacao.domain.model;

import java.util.Locale;

/**
 * Enumera os papéis que um usuário pode exercer dentro de uma organização da paróquia (clero, coordenações
 * pastorais/de movimento, mesas de conselho, voluntariado etc.). O papel de um usuário numa organização
 * específica, combinado com o {@code TipoOrganizacao} dessa organização e seu escopo (comunidade/pastoral),
 * é a base das checagens de autorização em todo o sistema — ver
 * {@code EscopoUsuarioAuthorizationService} para as regras de escopo derivadas de cada papel.
 */
public enum PapelOrganizacional {
    ADMIN("admin", "Administrador do Sistema"),
    BISPO_DIOCESANO("bispo-diocesano", "Bispo Diocesano"),
    PAROCO("paroco", "Pároco"),
    VIGARIO("vigario", "Vigário Paroquial"),
    DIACONO("diacono", "Diácono"),
    PADRE("padre", "Padre / Sacerdote"),
    MESA_CPP("mesa-cpp", "Mesa Diretora do CPP"),
    MEMBRO_CPP("membro-cpp", "Membro do CPP"),
    MEMBRO_CAE("membro-cae", "Membro do CAE"),
    PRESIDENTE_CPC("presidente-cpc", "Presidente do CPC"),
    MEMBRO_CPC("membro-cpc", "Membro do CPC"),
    COORD_PASTORAL_GLOBAL("coord-pastoral-global", "Coordenador Pastoral Global"),
    COORD_PASTORAL_LOCAL("coord-pastoral-local", "Coordenador Pastoral Local"),
    COORD_PASTORAL("coord-pastoral", "Coordenador Pastoral"),
    COORD_MOVIMENTO("coord-movimento", "Coordenador de Movimento"),
    VOLUNTARIO_PASTORAL_LOCAL("voluntario-pastoral-local", "Voluntário Pastoral Local"),
    MEMBRO_PASTORAL("membro-pastoral", "Membro de Pastoral"),
    MEMBRO_MOVIMENTO("membro-movimento", "Membro de Movimento"),
    SECRETARIO("secretario", "Secretário");

    private final String storedCode;
    private final String descricao;

    PapelOrganizacional(String storedCode, String descricao) {
        this.storedCode = storedCode;
        this.descricao = descricao;
    }

    /**
     * Converte um valor persistido/recebido de fora (código armazenado, nome do enum, ou um alias legado como
     * {@code mesa-diretora-cpp}) para o papel correspondente, normalizando maiúsculas/minúsculas e o separador
     * ({@code _} vs {@code -}). Os aliases legados existem para manter compatibilidade com dados gravados antes
     * de uma renomeação de papel; novos códigos não devem ser adicionados aqui sem necessidade de migração.
     *
     * @param rawValue valor bruto vindo do banco de dados ou de uma requisição externa
     * @return o {@link PapelOrganizacional} correspondente, ou {@code null} se {@code rawValue} for nulo,
     *     em branco ou não corresponder a nenhum papel conhecido
     */
    public static PapelOrganizacional fromStoredValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT).replace("_", "-");

        // Legado / Aliases de compatibilidade
        if ("mesa-diretora-cpp".equals(normalized) || "mesa_diretora_cpp".equalsIgnoreCase(rawValue.trim())) {
            return MESA_CPP;
        }
        if ("coordenador-pastoral-global".equals(normalized)
                || "coordenador_pastoral_global".equalsIgnoreCase(rawValue.trim())) {
            return COORD_PASTORAL_GLOBAL;
        }
        if ("coordenador-pastoral-local".equals(normalized)
                || "coordenador_pastoral_local".equalsIgnoreCase(rawValue.trim())) {
            return COORD_PASTORAL_LOCAL;
        }
        if ("coordenador-pastoral".equals(normalized) || "coordenador_pastoral".equalsIgnoreCase(rawValue.trim())) {
            return COORD_PASTORAL;
        }
        if ("coordenador-movimento".equals(normalized) || "coordenador_movimento".equalsIgnoreCase(rawValue.trim())) {
            return COORD_MOVIMENTO;
        }

        for (PapelOrganizacional role : values()) {
            if (role.storedCode.equals(normalized) || role.name().equalsIgnoreCase(rawValue.trim())) {
                return role;
            }
        }
        return null;
    }

    /**
     * Retorna o código estável usado para persistir este papel (independente do nome do enum em código),
     * permitindo renomear a constante Java sem quebrar dados já gravados.
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
     * Papel de coordenação/liderança único por organização (contexto de igreja). Usado para
     * sincronizar o campo de liderança da {@code GrupoParoquial} e para rebaixar o coordenador
     * anterior. Consolida a lista antes duplicada em {@code LiderancaSyncSupport}.
     */
    public boolean isCoordenacao() {
        return switch (this) {
            case COORD_PASTORAL_GLOBAL, COORD_PASTORAL_LOCAL, COORD_PASTORAL, COORD_MOVIMENTO,
                 PRESIDENTE_CPC, MESA_CPP -> true;
            default -> false;
        };
    }

    /**
     * Papel global (concedido só via {@code AtribuirPapelGlobalUseCase}, nunca como vínculo de
     * {@code MembroGrupo}).
     */
    public boolean isGlobal() {
        return this == PAROCO || this == VIGARIO || this == ADMIN;
    }
}
