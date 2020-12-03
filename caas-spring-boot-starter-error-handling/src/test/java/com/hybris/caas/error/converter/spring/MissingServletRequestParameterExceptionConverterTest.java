package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class MissingServletRequestParameterExceptionConverterTest
{
	private final MissingServletRequestParameterExceptionConverter converter = new MissingServletRequestParameterExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final MissingServletRequestParameterException exception = new MissingServletRequestParameterException("productId", "query");
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(MissingServletRequestParameterExceptionConverter.EXCEPTION_MESSAGE));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));

		assertThat(errorMessage.getDetails().get(0).getField(), equalTo("productId"));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_MISSING_QUERY_PARAMETER));

	}
}
