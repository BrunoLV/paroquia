package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Classifica cada nó da hierarquia organizacional da paróquia. A hierarquia canônica esperada é
 * {@code CLERO} (raiz) → {@code MATRIZ}/{@code COMUNIDADE} → {@code CONSELHO_PASTORAL}/{@code CONSELHO_ECONOMICO}
 * → {@code PASTORAL}/{@code MOVIMENTO}; {@code CPP} e {@code CPC} são variantes de conselho com sua própria
 * mesa/presidência. As regras que impõem qual tipo pode ter qual organização-mãe vivem em
 * {@code OrganizacaoService.validateHierarchy} — este enum não valida nada por si só.
 */
public enum TipoOrganizacao {
    MATRIZ,
    COMUNIDADE,
    PASTORAL,
    MOVIMENTO,
    CLERO,
    CONSELHO_PASTORAL,
    CONSELHO_ECONOMICO,
    CPP,
    CPC,
    OUTRO;

    /**
     * Converte um valor recebido via JSON para a constante correspondente, tratando o alias legado
     * {@code CONSELHO} (usado antes da distinção entre conselho pastoral e econômico) como
     * {@code CONSELHO_PASTORAL}.
     *
     * @param rawValue valor bruto recebido na requisição
     * @return o {@link TipoOrganizacao} correspondente
     */
    @JsonCreator
    public static TipoOrganizacao fromJson(String rawValue) {
        if ("CONSELHO".equalsIgnoreCase(rawValue)) {
            return CONSELHO_PASTORAL;
        }
        return EnumRequestNormalizer.normalize(rawValue, TipoOrganizacao.class);
    }

    /**
     * Converte um valor persistido no banco de dados para a constante correspondente, aplicando a mesma
     * normalização e os mesmos aliases legados de {@link #fromJson(String)}.
     *
     * @param rawValue valor bruto armazenado no banco de dados
     * @return o {@link TipoOrganizacao} correspondente, ou {@code null} se {@code rawValue} for nulo
     */
    public static TipoOrganizacao fromStoredValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        return fromJson(rawValue);
    }
}
