package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

public class HttpMediaTypeNotSupportedExceptionConverterTest
{
	private final HttpMediaTypeNotSupportedExceptionConverter converter = new HttpMediaTypeNotSupportedExceptionConverter();

	@Test
	public void should_convert_to_error_message_without_supported_MediaTypes()
	{
		final HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException("test message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("test message"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_415_UNSUPPORTED_REQUEST_CONTENT_TYPE));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getResponseHeaders().getAccept(), empty());
	}

	@Test
	public void should_convert_to_error_message_with_supported_MediaTypes()
	{
		final HttpMediaTypeNotSupportedException exception = new HttpMediaTypeNotSupportedException(MediaType.APPLICATION_XML,
				Arrays.asList(MediaType.APPLICATION_JSON), "test message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("test message"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_415_UNSUPPORTED_REQUEST_CONTENT_TYPE));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getResponseHeaders().getAccept(), equalTo(Arrays.asList(MediaType.APPLICATION_JSON)));
	}
}
