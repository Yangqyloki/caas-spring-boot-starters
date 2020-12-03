package com.hybris.caas.web.converter;

import com.hybris.caas.web.annotation.KeyMultiValues;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Spring custom converter permitting to disable the conversion from comma delimited string to collection.
 * The controller parameter must be annotated with {@link KeyMultiValues}.
 */
public class KeyMultiValuesConverter implements ConditionalGenericConverter
{
	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return Objects.nonNull(targetType.getAnnotation(KeyMultiValues.class));
	}

	@Override
	@Nullable
	public Object convert(final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType)
	{
		return Objects.isNull(source) ? null : Arrays.asList(source);
	}
}
