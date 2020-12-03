package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AuthenticationExceptionConverterTest
{
	private final AuthenticationExceptionConverter converter = new AuthenticationExceptionConverter();

	@Test
	public void should_convert_to_error_message() {
		final AuthenticationException exception = new InsufficientAuthenticationException("test message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_401));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.UNAUTHORIZED.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_401_INSUFFICIENT_CREDENTIALS));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
