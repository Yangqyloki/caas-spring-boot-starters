package com.hybris.caas.security.serialize;

import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;

@RunWith(MockitoJUnitRunner.class)
public class HtmlSanitizationSerializerTest
{
	private static final String UNSANITIZED_HTML = "<div><p>DUMMY</p></div>";
	private static final String SANITIZED_HTML = "<p>DUMMY</p>";

	private final StubSerializer serializer = new StubSerializer();

	@Mock
	private JsonGenerator jgen;
	@Mock
	private SerializerProvider provider;

	@Test
	public void should_serialize_with_sanitization_policy() throws JsonProcessingException, IOException
	{
		serializer.serialize(UNSANITIZED_HTML, jgen, provider);

		verify(jgen).writeString(SANITIZED_HTML);
	}

	private class StubSerializer extends HtmlSanitizationSerializer
	{
		private static final long serialVersionUID = 2904360122178414471L;

		@Override
		public PolicyFactory getPolicy()
		{
			return new HtmlPolicyBuilder().allowElements("p").toFactory();
		}

	}
}
