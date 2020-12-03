package com.hybris.caas.web.filter;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HeaderModifiableRequestTest
{
	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private HeaderModifiableRequest headerModifiableRequest;

	private final String originalHeader1 = "original-header-1";
	private final String originalHeader2 = "original-header-2";
	private final String originalValue1 = "original-value-2";
	private final String originalValue2 = "original-value-1";
	private final String myHeader1 = "my-header-1";
	private final String myValue1 = "my-value";
	private final String myValue2 = "my-value-2";

	@Before
	public void setUp()
	{
		request.addHeader(originalHeader1, originalValue1);
		request.addHeader(originalHeader2, originalValue2);

		headerModifiableRequest = new HeaderModifiableRequest(request);

		headerModifiableRequest.addHeader(myHeader1, myValue1);
		headerModifiableRequest.addHeader(originalHeader2, myValue2);
	}

	@Test
	public void shouldSetExistingHeader()
	{
		headerModifiableRequest.setHeader(originalHeader1, myValue1);
		assertThat(headerModifiableRequest.getHeader(originalHeader1)).isEqualTo(myValue1);
	}

	@Test
	public void shouldSetNewHeader()
	{
		final String myHeader2 = "my-header-2";
		headerModifiableRequest.setHeader(myHeader2, myValue2);
		assertThat(headerModifiableRequest.getHeader(myHeader2)).isEqualTo(myValue2);
	}

	@Test
	public void shouldGetHeader()
	{
		assertThat(headerModifiableRequest.getHeader(originalHeader1)).isEqualTo(originalValue1);
		assertThat(headerModifiableRequest.getHeader(originalHeader2)).isEqualTo(originalValue2);
		assertThat(headerModifiableRequest.getHeader(myHeader1)).isEqualTo(myValue1);
	}

	@Test
	public void shouldGetHeaders()
	{
		assertThat(Collections.list(headerModifiableRequest.getHeaders(originalHeader1))).contains(originalValue1);
		assertThat(Collections.list(headerModifiableRequest.getHeaders(originalHeader2))).contains(originalValue2, myValue2);
		assertThat(Collections.list(headerModifiableRequest.getHeaders(myHeader1))).contains(myValue1);
	}

	@Test
	public void shouldGetHeaderNames()
	{
		assertThat(Collections.list(headerModifiableRequest.getHeaderNames())).contains(originalHeader1, originalHeader2, myHeader1);
	}
}
