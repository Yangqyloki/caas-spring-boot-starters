package com.hybris.caas.web.converter;

import com.google.common.collect.ImmutableSet;
import com.hybris.caas.web.annotation.Guids;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.List;

/**
 * Spring custom converter permitting to convert a list of guid to lowercase.
 * The controller parameter must be annotated with {@link Guids}.
 */
public class GuidsConverter implements GenericConverter
{
	@Override
	public Set<ConvertiblePair> getConvertibleTypes()
	{
		final ConvertiblePair[] pairs = new ConvertiblePair[] { new ConvertiblePair(List.class, List.class) };

		return ImmutableSet.copyOf(pairs);
	}

	@Override
	public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType)
	{

		if (Objects.nonNull(targetType.getAnnotation(Guids.class)))
		{
			if(!(source instanceof List))
			{
				return source;
			}
			final List<String> list = ((List<String>)source);
			list.replaceAll(str -> str.toLowerCase(Locale.ENGLISH));
			return list;
		}
		else
		{
			return source;
		}

	}
}
