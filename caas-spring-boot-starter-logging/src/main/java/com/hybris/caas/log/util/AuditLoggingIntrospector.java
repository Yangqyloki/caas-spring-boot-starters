package com.hybris.caas.log.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hybris.caas.log.audit.annotation.MaskedAuditField;
import com.hybris.caas.log.audit.annotation.NonAuditableField;
import com.hybris.caas.log.audit.annotation.PartiallyMaskedAuditField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Custom annotation introspector implementation that allows use of {@code MaskedAuditField} and {@code PartiallyMaskedAuditField}
 * annotations to hide or partially hide sensitive entity attributes.
 */
@SuppressWarnings("serial")
public class AuditLoggingIntrospector extends JacksonAnnotationIntrospector
{
	private static final String MASK_CHARACTER = "*";
	private static final Integer NUMBER_OF_CHARS_TO_REVEAL = 4;

	@Override
	public Object findSerializer(final Annotated annotated)
	{
		if (!Objects.isNull(_findAnnotation(annotated, MaskedAuditField.class)))
		{
			return MaskSensitiveDataSerializer.class;
		}
		else if (!Objects.isNull(_findAnnotation(annotated, PartiallyMaskedAuditField.class)))
		{
			return PartialMaskSensitiveDataSerializer.class;
		}
		else
		{
			// Delegate to JacksonAnnotationIntrospector
			return super.findSerializer(annotated);
		}
	}

	@Override
	protected boolean _isIgnorable(final Annotated annotated)
	{
		return Optional.ofNullable(_findAnnotation(annotated, NonAuditableField.class))
				.map(nonAuditableField -> true)
				.orElseGet(() -> super._isIgnorable(annotated));
	}

	/**
	 * Hides sensitive information during serialization by converting the input value to a asterisks(*)
	 */
	public static class MaskSensitiveDataSerializer extends StdSerializer<String>
	{
		public MaskSensitiveDataSerializer()
		{
			super(String.class);
		}

		@Override
		public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{
			final String  maskString = (value.chars().mapToObj( c -> MASK_CHARACTER)).collect(Collectors.joining(""));
			gen.writeString(maskString);
		}
	}

	/**
	 * Hides sensitive information during serialization by partially converting the input value to a asterisks(*)
	 * The last 4 characters are kept while the remaining characters are converted to asterisks (*). If the value to mask is smaller than 4 chars, the whole field is masked
	 */
	public static class PartialMaskSensitiveDataSerializer extends StdSerializer<String>
	{
		public PartialMaskSensitiveDataSerializer()
		{
			super(String.class);
		}

		@Override
		public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException
		{
			final StringBuilder buff = new StringBuilder(value);
			final int maskedStrLength =
					value.length() < NUMBER_OF_CHARS_TO_REVEAL ? value.length() : value.length() - NUMBER_OF_CHARS_TO_REVEAL;
			buff.replace(0, maskedStrLength, StringUtils.repeat(MASK_CHARACTER, maskedStrLength));
			gen.writeString(buff.toString());
		}
	}

}
