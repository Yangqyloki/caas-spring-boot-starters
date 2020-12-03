package com.hybris.caas.data.persistence.converter;

import com.hybris.caas.error.exception.AttributeConversionException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;
import java.util.UUID;

/**
 * Converts between UUID String java object and PostgreSQL UUID type.
 * For serialization to and deserialization from the persistence storage.
 * NOTE: do not @Converter(autoApply = true), it will cause all columns to go through this converter, because of String and Object.
 * Apply per column basis: {@code @Convert(converter = UUIDConverter.class)}
 */
@Converter
public class UUIDConverter implements AttributeConverter<String, Object>
{

	@Override
	public Object convertToDatabaseColumn(final String attribute)
	{
		return Objects.nonNull(attribute) ? UUID.fromString(attribute) : null;
	}

	@Override
	public String convertToEntityAttribute(final Object dbData)
	{
		if (Objects.isNull(dbData))
		{
			return null;
		}

		if (dbData instanceof String)
		{
			return ((String) dbData).toLowerCase();
		}

		if (dbData instanceof UUID)
		{
			return dbData.toString().toLowerCase();
		}

		throw new AttributeConversionException("Unable to convert PostgreSQL UUID, unexpected type: " + dbData.getClass());
	}
}
