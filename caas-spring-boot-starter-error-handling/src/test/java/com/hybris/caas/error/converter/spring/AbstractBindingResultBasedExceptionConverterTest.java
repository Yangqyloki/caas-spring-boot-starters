package com.hybris.caas.error.converter.spring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;

@RunWith(MockitoJUnitRunner.class)
public class AbstractBindingResultBasedExceptionConverterTest
{
	@Mock
	private BindingResult bindingResult;
	private AbstractBindingResultBasedExceptionConverter<IllegalArgumentException> converter;

	@Before
	public void setUp()
	{
		final FieldError field1 = new FieldError("test object", "test field 1", "message 1");
		final FieldError field2 = new FieldError("test object", "test field 2", "message 2");
		final List<FieldError> fieldErrors = Arrays.asList(field1, field2);

		when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
		converter = new StubExceptionConverter(bindingResult);
	}

	@Test
	public void should_convert_to_error_message()
	{
		final IllegalArgumentException exception = new IllegalArgumentException("test message");
		final ErrorMessage errorMessage = converter.convert(exception);
		final ErrorMessageDetail errorMessageDetail1 = errorMessage.getDetails().get(0);
		final ErrorMessageDetail errorMessageDetail2 = errorMessage.getDetails().get(1);

		assertThat(errorMessage.getDetails(), iterableWithSize(2));
		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_400));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));

		assertThat(errorMessageDetail1.getMessage(), equalTo("message 1"));
		assertThat(errorMessageDetail1.getField(), equalTo("test field 1"));
		assertThat(errorMessageDetail1.getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(errorMessageDetail1.getMoreInfo(), equalTo(ErrorConstants.INFO));

		assertThat(errorMessageDetail2.getMessage(), equalTo("message 2"));
		assertThat(errorMessageDetail2.getField(), equalTo("test field 2"));
		assertThat(errorMessageDetail2.getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(errorMessageDetail2.getMoreInfo(), equalTo(ErrorConstants.INFO));
	}

	/**
	 * Stub implementation of the abstract class under test.
	 */
	private static class StubExceptionConverter
			extends AbstractBindingResultBasedExceptionConverter<IllegalArgumentException>
	{
		private final BindingResult bindingResult;

		public StubExceptionConverter(final BindingResult bindingResult)
		{
			this.bindingResult = bindingResult;
		}

		@Override
		protected BindingResult getBindingResult(IllegalArgumentException ex)
		{
			return bindingResult;
		}

	}
}
