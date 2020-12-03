package com.hybris.caas.error.converter;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.annotation.WebException;

public class DefaultExceptionConverterTest
{
	private final DefaultExceptionConverter converter = new DefaultExceptionConverter();

	@Test
	public void should_use_default_values_when_no_annotation()
	{
		final ErrorMessage error = converter.toErrorMessage(new Exception("test message"));

		assertThat(error.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		assertThat(error.getType(), equalTo(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR));
		assertThat(error.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(error.getMessage(), equalTo(ErrorConstants.MESSAGE_500));
		assertThat(error.getDetails(), empty());
	}

	@Test
	public void should_set_annotation_values_when_present()
	{
		final ErrorMessage error = converter.toErrorMessage(new StubException("stub message"));

		assertThat(error.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(error.getType(), equalTo("type"));
		assertThat(error.getMoreInfo(), equalTo("info"));
		assertThat(error.getMessage(), equalTo("stub message"));
		assertThat(error.getDetails(), empty());
	}

	@WebException(status = HttpStatus.BAD_REQUEST, type = "type", info = "info")
	private static class StubException extends Exception
	{
		private static final long serialVersionUID = -6209262216083835345L;

		StubException(String msg)
		{
			super(msg);
		}
	}
}
