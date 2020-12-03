package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.firewall.RequestRejectedException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class RequestRejectedExceptionConverterTest
{
	private final RequestRejectedExceptionConverter converter = new RequestRejectedExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final RequestRejectedException exception = new RequestRejectedException("Message");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("Message"));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}
}
