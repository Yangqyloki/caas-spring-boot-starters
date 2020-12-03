package com.hybris.caas.data.persistence.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Converts between {@code List<String>} java object and {@code String[]} data types.
 * For serialization to and deserialization from the persistence storage.
 * NOTE: do not @Converter(autoApply = true), it will cause all columns to go through this converter, because of PGObject
 * Apply per column basis: @Convert(converter = StringArrayConverter.class)
 */
@Converter
public class StringArrayConverter implements AttributeConverter<List<String>, String[]>
{
	@Override
	public String[] convertToDatabaseColumn(final List<String> stringList)
	{
		return Optional.ofNullable(stringList).map(list -> list.toArray(new String[0])).orElse(null);
	}

	@Override
	public List<String> convertToEntityAttribute(final String[] stringArray)
	{
		// previous implementation with @Array returned an empty list for null
		return Optional.ofNullable(stringArray).map(Arrays::asList).orElse(new ArrayList<>());
	}
}
