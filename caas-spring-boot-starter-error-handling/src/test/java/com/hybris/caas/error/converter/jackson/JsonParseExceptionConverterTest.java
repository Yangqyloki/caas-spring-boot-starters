package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;
import static com.hybris.caas.error.converter.jackson.JsonParseExceptionConverter.JSON_PARSE_EXCEPTION_MESSAGE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class JsonParseExceptionConverterTest
{
	private final JsonParseExceptionConverter converter = new JsonParseExceptionConverter();

	@Test
	public void should_convert_to_error_message() throws Exception
	{
		final JsonParser parser = new JsonFactory().createParser("Json parser".getBytes());
		final JsonLocation location = new JsonLocation("Json location source ref", 100, 2, 1);
		final JsonParseException ex = new JsonParseException(parser, "Failed to parse request body", location);
		final ErrorMessage errorMessage = converter.convert(ex);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));

		assertThat(errorMessage.getDetails(), hasSize(1));
		assertThat(errorMessage.getDetails().get(0).getMessage(), equalTo(String.format(JSON_PARSE_EXCEPTION_MESSAGE, 2, 1)));
		assertThat(errorMessage.getDetails().get(0).getType(), nullValue());
		assertThat(errorMessage.getDetails().get(0).getField(), nullValue());
	}
}
