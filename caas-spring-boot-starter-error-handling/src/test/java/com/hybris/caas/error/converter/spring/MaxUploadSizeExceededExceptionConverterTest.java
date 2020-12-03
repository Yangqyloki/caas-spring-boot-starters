package com.hybris.caas.error.converter.spring;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;

public class MaxUploadSizeExceededExceptionConverterTest
{
	private final MaxUploadSizeExceededExceptionConverter converter = new MaxUploadSizeExceededExceptionConverter(DataSize.ofMegabytes(1));

	@Test
	public void should_convert_to_error_message()
	{
		final MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(5000l);
		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(String.format(MaxUploadSizeExceededExceptionConverter.MESSAGE, "1048576B")));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
	}
}
