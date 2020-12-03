package com.hybris.caas.web.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class GuidDeserializerTest
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
		final Dummy result = mapper.readValue("{\"id\":\"USid-EFER-8383\"}", Dummy.class);
		assertThat(result.getId(), equalTo("usid-efer-8383"));
	}

	private static class Dummy
	{
		@JsonDeserialize(using = GuidDeserializer.class)
		private String id;

		public String getId()
		{
			return id;
		}
	}

}
