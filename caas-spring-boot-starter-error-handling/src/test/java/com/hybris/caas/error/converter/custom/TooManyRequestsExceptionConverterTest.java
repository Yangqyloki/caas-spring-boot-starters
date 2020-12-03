package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.exception.TooManyRequestsException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class TooManyRequestsExceptionConverterTest
{
	private final TooManyRequestsExceptionConverter converter = new TooManyRequestsExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final TooManyRequestsException exception = new TooManyRequestsException(100L, "hour", 3600L);
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo("The service only allows 100 requests per hour to this endpoint. A Retry-After response header will indicate how long to wait before making a new request."));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.TOO_MANY_REQUESTS.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_429_TOO_MANY_REQUESTS));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getResponseHeaders().get("Retry-After").toString(), equalTo("[3600]"));
	}
}
