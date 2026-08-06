package br.com.nsfatima.gestao.organizacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

/**
 * Classifica um {@code GrupoParoquial} — qualquer agrupamento de fiéis com função pastoral ou de
 * governança. Restrito aos 4 valores da spec de domínio: {@code PASTORAL}, {@code MOVIMENTO},
 * {@code CONSELHO}, {@code MINISTERIO}. Hierarquia territorial (Clero/Matriz/Comunidade) não é um
 * {@code GrupoParoquial} — ver {@link TipoUnidadeTerritorial}. As regras de qual grupo pode ter qual pai
 * vivem em {@code GrupoParoquialService.validateHierarchy}, não neste enum.
 */
public enum TipoGrupo {
    PASTORAL,
    MOVIMENTO,
    CONSELHO,
    MINISTERIO;

    /**
     * Converte um valor recebido via JSON para a constante correspondente, tratando os aliases legados dos
     * 4 tipos de conselho pré-existentes ({@code CONSELHO_PASTORAL}, {@code CONSELHO_ECONOMICO},
     * {@code CPP}, {@code CPC}) como {@code CONSELHO}, e {@code OUTRO} como {@code MINISTERIO}.
     *
     * @param rawValue valor bruto recebido na requisição
     * @return o {@link TipoGrupo} correspondente
     */
    @JsonCreator
    public static TipoGrupo fromJson(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim().toUpperCase(Locale.ROOT);
        if ("CONSELHO_PASTORAL".equals(normalized) || "CONSELHO_ECONOMICO".equals(normalized)
                || "CPP".equals(normalized) || "CPC".equals(normalized)) {
            return CONSELHO;
        }
        if ("OUTRO".equals(normalized)) {
            return MINISTERIO;
        }
        // CLERO/MATRIZ/COMUNIDADE eram valores territoriais legados (track linguagem_ubiqua_dominio): não
        // são mais um TipoGrupo válido (viraram TipoUnidadeTerritorial). Contextos que ainda produzem esses
        // valores como sentinela (ex.: EventoActorContext de um Pároco/Vigário sem GrupoParoquial) devem
        // tratar null como "sem tipo de grupo aplicável", em vez de falhar aqui.
        if ("CLERO".equals(normalized) || "MATRIZ".equals(normalized) || "COMUNIDADE".equals(normalized)) {
            return null;
        }
        return EnumRequestNormalizer.normalize(rawValue, TipoGrupo.class);
    }

    /**
     * Converte um valor persistido no banco de dados para a constante correspondente, aplicando a mesma
     * normalização e os mesmos aliases legados de {@link #fromJson(String)}.
     *
     * @param rawValue valor bruto armazenado no banco de dados
     * @return o {@link TipoGrupo} correspondente, ou {@code null} se {@code rawValue} for nulo
     */
    public static TipoGrupo fromStoredValue(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        return fromJson(rawValue);
    }
}
