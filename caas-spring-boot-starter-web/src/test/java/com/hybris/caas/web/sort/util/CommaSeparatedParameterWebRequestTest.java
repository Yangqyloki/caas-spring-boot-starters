package com.hybris.caas.web.sort.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.request.NativeWebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommaSeparatedParameterWebRequestTest
{
	private static final String PARAMETER_NAME = "parameter-name";

	@Mock
	private NativeWebRequest delegate;
	private NativeWebRequest nativeWebRequest;

	@Before
	public void setUp()
	{
		nativeWebRequest = new CommaSeparatedParameterWebRequest(delegate);
	}

	@Test
	public void should_getParameterValues_from_single_comma_separated_value_no_spaces()
	{
		when(delegate.getParameter(PARAMETER_NAME)).thenReturn("foo,bar,baz");
		final String[] values = nativeWebRequest.getParameterValues(PARAMETER_NAME);
		assertThat(values).containsExactly("foo", "bar", "baz");
	}

	@Test
	public void should_getParameterValues_from_single_comma_separated_value_with_spaces()
	{
		when(delegate.getParameter(PARAMETER_NAME)).thenReturn("foo, bar, baz");
		final String[] values = nativeWebRequest.getParameterValues(PARAMETER_NAME);
		assertThat(values).containsExactly("foo", "bar", "baz");
	}

	@Test
	public void should_getParameterValues_from_single_comma_separated_value_empty_tokens()
	{
		when(delegate.getParameter(PARAMETER_NAME)).thenReturn(", ,  ,");
		final String[] values = nativeWebRequest.getParameterValues(PARAMETER_NAME);
		assertThat(values).isEmpty();
	}

	@Test
	public void should_getParameterValues_from_single_comma_separated_value_no_comma()
	{
		when(delegate.getParameter(PARAMETER_NAME)).thenReturn("foo:bar;baz");
		final String[] values = nativeWebRequest.getParameterValues(PARAMETER_NAME);
		assertThat(values).containsExactly("foo:bar;baz");
	}

	@Test
	public void should_getParameterValues_from_single_comma_separated_value_no_value()
	{
		when(delegate.getParameter(PARAMETER_NAME)).thenReturn(null);
		final String[] values = nativeWebRequest.getParameterValues(PARAMETER_NAME);
		assertThat(values).isNull();
	}
}
