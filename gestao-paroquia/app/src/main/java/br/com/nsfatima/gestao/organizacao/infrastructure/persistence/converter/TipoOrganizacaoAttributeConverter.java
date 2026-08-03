package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.converter;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TipoOrganizacaoAttributeConverter implements AttributeConverter<TipoOrganizacao, String> {

    @Override
    public String convertToDatabaseColumn(TipoOrganizacao attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public TipoOrganizacao convertToEntityAttribute(String dbData) {
        return TipoOrganizacao.fromStoredValue(dbData);
    }
}
