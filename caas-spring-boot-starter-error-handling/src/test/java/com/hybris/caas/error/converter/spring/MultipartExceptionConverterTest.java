package com.hybris.caas.error.converter.spring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;

public class MultipartExceptionConverterTest
{
	private final MultipartExceptionConverter converter = new MultipartExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final MultipartException exception = new MultipartException("some message goes here");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(MultipartExceptionConverter.MESSAGE));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_MULTIPART_RESOLUTION_ERROR));
	}
}
