package com.hybris.caas.security.encode;

import org.hamcrest.Matchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class PropertiesEncoderTest
{
	@Test
	public void should_encode_tab()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\t"), Matchers.equalTo("\\t"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\t"), Matchers.equalTo("\\t"));
	}

	@Test
	public void should_encode_newline()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\n"), Matchers.equalTo("\\n"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\n"), Matchers.equalTo("\\n"));
	}

	@Test
	public void should_encode_return()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\r"), Matchers.equalTo("\\r"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\r"), Matchers.equalTo("\\r"));
	}

	@Test
	public void should_encode_formfeed()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\f"), Matchers.equalTo("\\f"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\f"), Matchers.equalTo("\\f"));
	}

	@Test
	public void should_encode_equals()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("="), Matchers.equalTo("\\="));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("="), Matchers.equalTo("\\="));
	}

	@Test
	public void should_encode_exclamation()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("!"), Matchers.equalTo("\\!"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("!"), Matchers.equalTo("\\!"));
	}

	@Test
	public void should_encode_colon()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey(":"), Matchers.equalTo("\\:"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue(":"), Matchers.equalTo("\\:"));
	}

	@Test
	public void should_encode_hash()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("#"), Matchers.equalTo("\\#"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("#"), Matchers.equalTo("\\#"));
	}

	@Test
	public void should_encode_escaped_backslash()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\\"), Matchers.equalTo("\\\\"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\\"), Matchers.equalTo("\\\\"));
	}

	@Test
	public void should_encode_space_head_char()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey(" "), Matchers.equalTo("\\ "));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue(" "), Matchers.equalTo("\\ "));
	}

	@Test
	public void should_encode_space_tail_char()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("a "), Matchers.equalTo("a\\ "));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("a "), Matchers.equalTo("a "));
	}

	@Test
	public void should_encode_unicode_001F()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\u001F"), Matchers.equalTo("\\u001F"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\u001F"), Matchers.equalTo("\\u001F"));
	}

	@Test
	public void should_encode_unicode_0020_space_head()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\u0020"), Matchers.equalTo("\\ "));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\u0020"), Matchers.equalTo("\\ "));
	}

	@Test
	public void should_encode_unicode_0020_space_tail()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("a\u0020"), Matchers.equalTo("a\\ "));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("a\u0020"), Matchers.equalTo("a "));
	}

	@Test
	public void should_not_encode_unicode_007E()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\u007E"), Matchers.equalTo("~"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\u007E"), Matchers.equalTo("~"));
	}

	@Test
	public void should_encode_unicode_007F()
	{
		MatcherAssert.assertThat(PropertiesEncoder.encodeKey("\u007F"), Matchers.equalTo("\\u007F"));
		MatcherAssert.assertThat(PropertiesEncoder.encodeValue("\u007F"), Matchers.equalTo("\\u007F"));
	}
}
