package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.converter;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converte {@link TipoGrupo} entre a coluna de banco de dados e o enum Java, delegando a normalização
 * (incluindo aliases legados como {@code CONSELHO}) para {@link TipoGrupo#fromStoredValue(String)} em
 * vez de depender apenas do mapeamento padrão {@code @Enumerated(STRING)}.
 */
@Converter
public class TipoGrupoAttributeConverter implements AttributeConverter<TipoGrupo, String> {

    /**
     * Converte o enum para o valor gravado na coluna do banco de dados.
     *
     * @param attribute valor do atributo em memória
     * @return o nome do enum, ou {@code null} se o atributo for nulo
     */
    @Override
    public String convertToDatabaseColumn(TipoGrupo attribute) {
        return attribute == null ? null : attribute.name();
    }

    /**
     * Converte o valor gravado no banco de dados de volta para o enum, aplicando a normalização e os aliases
     * legados de {@link TipoGrupo#fromStoredValue(String)}.
     *
     * @param dbData valor bruto armazenado na coluna
     * @return o {@link TipoGrupo} correspondente
     */
    @Override
    public TipoGrupo convertToEntityAttribute(String dbData) {
        return TipoGrupo.fromStoredValue(dbData);
    }
}
