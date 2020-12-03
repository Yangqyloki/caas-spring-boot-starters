package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class InsufficientAuthenticationExceptionConverterTest
{
	private final InsufficientAuthenticationExceptionConverter converter = new InsufficientAuthenticationExceptionConverter();

	@Test
	public void should_convert_to_error_message() {
		final InsufficientAuthenticationException exception = new InsufficientAuthenticationException("test message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_401));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.UNAUTHORIZED.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_401_INSUFFICIENT_CREDENTIALS));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
