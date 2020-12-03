package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;


public class HttpMessageConversionExceptionConverterTest
{
	private final HttpMessageConversionExceptionConverter converter = new HttpMessageConversionExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final HttpMessageConversionException exception = new HttpMessageConversionException("Required request body is missing: ...");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(HttpMessageNotReadableExceptionConverter.EXCEPTION_MESSAGE));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}