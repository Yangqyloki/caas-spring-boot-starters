package com.hybris.caas.web.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hybris.caas.web.annotation.Guid;

import java.io.IOException;
import java.util.Locale;

/**
 * Jackson custom deserializer permitting to convert guid to lowercase.
 * The jackson property must be annotated with {@link Guid}.
 */
public class GuidDeserializer extends StdDeserializer<String>
{
	public GuidDeserializer()
	{
		super(String.class);
	}

	@Override
	public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		return _parseString(p, ctxt).toLowerCase(Locale.getDefault());
	}
}

