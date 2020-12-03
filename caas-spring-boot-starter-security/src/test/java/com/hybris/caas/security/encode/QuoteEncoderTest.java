package com.hybris.caas.security.encode;

import org.hamcrest.Matchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class QuoteEncoderTest
{
	@Test
	public void should_encode_tab()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\t"), Matchers.equalTo("\\t"));
	}

	@Test
	public void should_encode_newline()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\n"), Matchers.equalTo("\\n"));
	}

	@Test
	public void should_encode_return()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\r"), Matchers.equalTo("\\r"));
	}

	@Test
	public void should_encode_formfeed()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\f"), Matchers.equalTo("\\f"));
	}

	@Test
	public void should_encode_escaped_backslash()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\\"), Matchers.equalTo("\\\\"));
	}

	@Test
	public void should_encode_single_quote()
	{
		final String encoded = QuoteEncoder.encode("'");
		MatcherAssert.assertThat(encoded, Matchers.equalTo("\\\'"));
	}

	@Test
	public void should_encode_double_quote()
	{
		final String encoded = QuoteEncoder.encode("\"");
		MatcherAssert.assertThat(encoded, Matchers.equalTo("\\\""));
	}

	@Test
	public void should_encode_single_quote_unicode()
	{
		final String encoded = QuoteEncoder.encode("\u0027");
		MatcherAssert.assertThat(encoded, Matchers.equalTo("\\\'"));
	}

	@Test
	public void should_encode_double_quote_unicode()
	{
		final String encoded = QuoteEncoder.encode("\\u0022");
		MatcherAssert.assertThat(encoded, Matchers.equalTo("\\\\u0022"));
	}

	@Test
	public void should_encode_unicode_001F()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\u001F"), Matchers.equalTo("\\u001F"));
	}

	@Test
	public void should_not_encode_unicode_007E()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\u007E"), Matchers.equalTo("~"));
	}

	@Test
	public void should_encode_unicode_007F()
	{
		MatcherAssert.assertThat(QuoteEncoder.encode("\u007F"), Matchers.equalTo("\\u007F"));
	}

	@Test
	public void should_support_URIs()
	{
		final String uri = "https://www.example.org/my-redirect-uri?with=parameter";
		MatcherAssert.assertThat(QuoteEncoder.encode(uri), Matchers.equalTo(uri));
	}

}
