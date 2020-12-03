package com.hybris.caas.error.converter.spring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;

public class NoHandlerFoundExceptionConverterTest
{
	private final NoHandlerFoundExceptionConverter converter = new NoHandlerFoundExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final NoHandlerFoundException exception = new NoHandlerFoundException("test method", "test url", new HttpHeaders());
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("No handler found for test method test url"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.NOT_FOUND.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
