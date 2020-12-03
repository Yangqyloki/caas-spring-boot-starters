package com.hybris.caas.error.converter.spring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpMediaTypeNotAcceptableException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;

public class HttpMediaTypeNotAcceptableExceptionConverterTest
{
	private final HttpMediaTypeNotAcceptableExceptionConverter converter = new HttpMediaTypeNotAcceptableExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final HttpMediaTypeNotAcceptableException exception = new HttpMediaTypeNotAcceptableException("test message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("test message"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.NOT_ACCEPTABLE.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_406_UNSUPPORTED_RESPONSE_CONTENT_TYPE));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
