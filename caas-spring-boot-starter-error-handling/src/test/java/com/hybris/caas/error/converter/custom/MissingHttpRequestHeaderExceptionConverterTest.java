package com.hybris.caas.error.converter.custom;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.custom.MissingHttpRequestHeaderExceptionConverter;
import com.hybris.caas.error.exception.MissingHttpRequestHeaderException;

public class MissingHttpRequestHeaderExceptionConverterTest
{
	private final MissingHttpRequestHeaderExceptionConverter converter = new MissingHttpRequestHeaderExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final MissingHttpRequestHeaderException exception = new MissingHttpRequestHeaderException("test header");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("Missing HTTP request header 'test header'."));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
