package com.hybris.caas.data.persistence.converter.hibernate;

import org.postgresql.util.HStoreConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Converter
public class HibernateHStoreConverter implements AttributeConverter<Map<String, String>, String>
{
	@Override
	public String convertToDatabaseColumn(final Map<String, String> attribute)
	{
		if (Objects.nonNull(attribute))
		{
			return HStoreConverter.toString(attribute);
		}
		return null;
	}

	@Override
	public Map<String, String> convertToEntityAttribute(final String dbData)
	{
		if (Objects.nonNull(dbData))
		{
			return HStoreConverter.fromString(dbData);
		}
		return Collections.emptyMap();
	}
}
