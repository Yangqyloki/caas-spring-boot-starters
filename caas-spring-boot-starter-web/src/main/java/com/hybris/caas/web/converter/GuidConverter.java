package com.hybris.caas.web.converter;

import com.google.common.collect.ImmutableSet;
import com.hybris.caas.web.annotation.Guid;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Spring custom converter permitting to convert guid to lowercase.
 * The controller parameter must be annotated with {@link Guid}.
 */
public class GuidConverter implements GenericConverter
{
	@Override
	public Set<ConvertiblePair> getConvertibleTypes()
	{
		final ConvertiblePair[] pairs = new ConvertiblePair[] { new ConvertiblePair(String.class, String.class) };

		return ImmutableSet.copyOf(pairs);
	}

	@Override
	public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType)
	{
		return Objects.isNull(targetType.getAnnotation(Guid.class)) ? source : ((String) source).toLowerCase(Locale.ENGLISH);
	}
}
