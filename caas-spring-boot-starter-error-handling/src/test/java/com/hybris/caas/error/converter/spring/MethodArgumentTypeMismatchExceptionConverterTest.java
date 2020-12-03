package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class MethodArgumentTypeMismatchExceptionConverterTest
{
	private final MethodArgumentTypeMismatchExceptionConverter converter = new MethodArgumentTypeMismatchExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(null, null, "pageSize", null, null);
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(MethodArgumentTypeMismatchExceptionConverter.EXCEPTION_MESSAGE));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));

		assertThat(errorMessage.getDetails().get(0).getField(), equalTo("pageSize"));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_QUERY_PARAMETER));
	}
}
