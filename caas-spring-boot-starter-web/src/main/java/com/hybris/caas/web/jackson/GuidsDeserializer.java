package com.hybris.caas.web.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hybris.caas.web.annotation.Guids;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Jackson custom deserializer permitting to convert a list of guid to lowercase.
 * The jackson property must be annotated with {@link Guids}.
 */
public class GuidsDeserializer extends StdDeserializer<List>
{
	public GuidsDeserializer()
	{
		super(List.class);
	}

	@Override
	public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		final List<String> input = mapper.readValue(p, new TypeReference<List<String>>(){});
		input.replaceAll(str -> str.toLowerCase(Locale.ENGLISH));
		return input;
	}
}

