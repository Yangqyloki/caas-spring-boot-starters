package com.hybris.caas.web.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class GuidsDeserializerTest
{
	private ObjectMapper mapper;

	@Before
	public void setup()
	{
		mapper = new ObjectMapper();
	}

	@Test
	public void should_return_guid_in_lowercase() throws IOException
	{
		List<String> results = new ArrayList<>();
		results.add("guid");
		results.add("guids");

		String json = "{\"ids\":[\"GUID\",\"GUIDS\"]}";

		final Dummy result = mapper.readValue(json, Dummy.class);
		assertThat(result.getIds(), equalTo(results));
	}

	private static class Dummy
	{
		@JsonDeserialize(using = GuidsDeserializer.class)
		private List<String> ids;

		public List<String> getIds()
		{

			return ids;

		}
	}

}
