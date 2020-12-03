package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.assertj.core.util.Arrays;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UnrecognizedPropertyExceptionConverterTest
{
	private final UnrecognizedPropertyExceptionConverter converter = new UnrecognizedPropertyExceptionConverter();

	@Test
	public void should_convert_to_error_message() throws Exception
	{
		final JsonParser parser = new JsonFactory().createParser("Json parser".getBytes());
		final UnrecognizedPropertyException ex = UnrecognizedPropertyException.from(parser, String.class, "propName",
				Arrays.asList(new String[] { "propId" }));
		final ErrorMessage errorMessage = converter.convert(ex);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
	}

}