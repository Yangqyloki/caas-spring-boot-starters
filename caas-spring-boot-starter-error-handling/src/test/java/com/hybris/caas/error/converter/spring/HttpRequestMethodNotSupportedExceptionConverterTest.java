package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

public class HttpRequestMethodNotSupportedExceptionConverterTest
{
	private final HttpRequestMethodNotSupportedExceptionConverter converter = new HttpRequestMethodNotSupportedExceptionConverter();

	@Test
	public void should_convert_to_error_message_without_supported_HttpMethods()
	{
		final HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("GET");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("Request method 'GET' not supported"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.METHOD_NOT_ALLOWED.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_405_UNSUPPORTED_METHOD));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getResponseHeaders().getAllow(), empty());
	}

	@Test
	public void should_convert_to_error_message_with_supported_HttpMethods()
	{
		final HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("GET", new String[]{"POST"});
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("Request method 'GET' not supported"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.METHOD_NOT_ALLOWED.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_405_UNSUPPORTED_METHOD));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getResponseHeaders().getAllow(), contains(HttpMethod.POST));
	}
}
