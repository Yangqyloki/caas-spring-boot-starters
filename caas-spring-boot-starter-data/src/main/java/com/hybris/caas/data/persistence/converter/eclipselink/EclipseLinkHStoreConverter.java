package com.hybris.caas.data.persistence.converter.eclipselink;

import org.postgresql.util.HStoreConverter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Converter
public class EclipseLinkHStoreConverter implements AttributeConverter<Map<String, String>, Object>
{
	@Override
	public Object convertToDatabaseColumn(final Map<String, String> object)
	{
		if (Objects.nonNull(object))
		{
			return HStoreConverter.toString(object);
		}
		return null;
	}

	@Override
	public Map<String, String> convertToEntityAttribute(final Object object)
	{
		if (object instanceof Map)
		{
			return (Map<String, String>) object;
		}
		return Collections.emptyMap();
	}
}