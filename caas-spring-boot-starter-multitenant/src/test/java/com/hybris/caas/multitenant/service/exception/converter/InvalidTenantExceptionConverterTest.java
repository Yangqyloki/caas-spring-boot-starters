package com.hybris.caas.multitenant.service.exception.converter;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.multitenant.service.exception.InvalidTenantException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.multitenant.Constants.TENANT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InvalidTenantExceptionConverterTest
{
	private InvalidTenantExceptionConverter converter = new InvalidTenantExceptionConverter();

	@Test
	public void should_convert_exception()
	{
		final InvalidTenantException exception = new InvalidTenantException();

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(ErrorConstants.MESSAGE_400));
		assertThat(errorMessage.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), is(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));
		assertThat(errorMessage.getDetails(), hasSize(1));
		assertThat(errorMessage.getDetails().get(0).getType(), is(ErrorConstants.SUB_TYPE_400_INVALID_HEADER));
		assertThat(errorMessage.getDetails().get(0).getField(), is(TENANT));
		assertThat(errorMessage.getDetails().get(0).getMessage(), is(InvalidTenantException.MESSAGE));
	}

}
