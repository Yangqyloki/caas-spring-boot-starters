package com.hybris.caas.web.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hybris.caas.data.utils.DateUtils;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CustomOffsetDateTimeSerializer extends StdSerializer<OffsetDateTime>
{
	@SuppressWarnings(value = {"squid:S1948", "squid:S3437"})
	private final DateTimeFormatter format = DateTimeFormatter.ofPattern(DateUtils.ISO8601_DATE_PATTERN);

	public CustomOffsetDateTimeSerializer()
	{
		super(OffsetDateTime.class);
	}

	@Override
	public void serialize(final OffsetDateTime value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException
	{
		gen.writeString(value.withOffsetSameInstant(ZoneOffset.UTC).format(format));
	}
}
