package com.hybris.caas.data.persistence.converter;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class StringArrayConverterTest
{
	private final StringArrayConverter converter = new StringArrayConverter();

	@Test
	public void shouldConvertToDatabaseColumn()
	{
		assertThat(converter.convertToDatabaseColumn(null)).isNull();
		assertThat(converter.convertToDatabaseColumn(Collections.emptyList())).isNotNull().isEmpty();
		assertThat(converter.convertToDatabaseColumn(Arrays.asList("a", "b", "c"))).isNotNull().containsExactly("a", "b", "c");
	}

	@Test
	public void shouldConvertToEntityAttribute()
	{
		assertThat(converter.convertToEntityAttribute(null)).isNotNull().isEmpty();
		assertThat(converter.convertToEntityAttribute(new String[] {})).isNotNull().isEmpty();
		assertThat(converter.convertToEntityAttribute(new String[] { "a", "b", "c" })).isNotNull().containsExactly("a", "b", "c");
	}
}
