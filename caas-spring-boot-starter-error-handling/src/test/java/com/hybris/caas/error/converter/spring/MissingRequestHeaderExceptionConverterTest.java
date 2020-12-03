package com.hybris.caas.error.converter.spring;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;

import static com.hybris.caas.error.converter.spring.MissingRequestHeaderExceptionConverter.EXCEPTION_MESSAGE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MissingRequestHeaderExceptionConverterTest
{
	private final MissingRequestHeaderExceptionConverter converter = new MissingRequestHeaderExceptionConverter();

	@Mock
	private MissingRequestHeaderException missingRequestHeaderException;

	@Test
	public void should_convert_to_error_message()
	{
		final ErrorMessage errorMessage = converter.convert(missingRequestHeaderException);

		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.PRECONDITION_FAILED.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_412_PRECONDITION_FAILED));
		assertThat(errorMessage.getMessage(), containsString(EXCEPTION_MESSAGE));
	}
}
