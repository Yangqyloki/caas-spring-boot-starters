package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class InvalidTypeIdExceptionConverterTest
{
	private final InvalidTypeIdExceptionConverter converter = new InvalidTypeIdExceptionConverter();

	@Test
	public void should_convert_to_error_message() throws Exception
	{
		final JsonParser parser = new JsonFactory().createParser("Json parser".getBytes());
		final InvalidTypeIdException ex = InvalidTypeIdException.from(parser, " Missing type id when trying to resolve subtype", null,
				"missingTypeIdProperty");

		final ErrorMessage errorMessage = converter.convert(ex);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));
	}
}
