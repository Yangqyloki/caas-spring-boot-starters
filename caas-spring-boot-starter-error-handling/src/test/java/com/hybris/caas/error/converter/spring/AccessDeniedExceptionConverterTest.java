package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class AccessDeniedExceptionConverterTest
{
	private final AccessDeniedExceptionConverter converter = new AccessDeniedExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final AccessDeniedException exception = new AccessDeniedException("message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_403));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.FORBIDDEN.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_403_INSUFFICIENT_PERMISSIONS));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
