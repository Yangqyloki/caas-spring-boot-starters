package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.exception.InvalidEnumValueException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class InvalidEnumValueExceptionConverterTest
{
	private static final String DUMMY = "dummy";
	private static final String INVALID_VALUE = "INVALID-VALUE";

	private InvalidEnumValueExceptionConverter converter = new InvalidEnumValueExceptionConverter();

	@Test
	public void should_convert_exception_to_error_message()
	{
		final InvalidEnumValueException exception = new InvalidEnumValueException(DUMMY, Collections.singleton(DUMMY), INVALID_VALUE);

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(ErrorConstants.MESSAGE_400));
		assertThat(errorMessage.getStatus(), is(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), is(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getMoreInfo(), is(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), hasSize(1));

		final ErrorMessageDetail detail = errorMessage.getDetails().get(0);
		assertThat(detail.getType(), is(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(detail.getMoreInfo(), is(ErrorConstants.INFO));
		assertThat(detail.getField(), is(DUMMY));
		assertThat(detail.getMessage(), containsString(INVALID_VALUE));
	}
}
