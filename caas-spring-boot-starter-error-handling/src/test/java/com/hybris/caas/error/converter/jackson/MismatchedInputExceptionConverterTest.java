package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;
import static com.hybris.caas.error.converter.jackson.AbstractJacksonExceptionConverter.JSON_PARSE_EXCEPTION_MORE_INFO;
import static com.hybris.caas.error.converter.jackson.MismatchedInputExceptionConverter.JSON_PARSE_EXCEPTION_MESSAGE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

public class MismatchedInputExceptionConverterTest
{
	private final MismatchedInputExceptionConverter converter = new MismatchedInputExceptionConverter();

	@Test
	public void should_convert_to_error_message() throws Exception
	{

		final JsonParser parser = new JsonFactory().createParser("Json parser".getBytes());
		final JsonLocation location = new JsonLocation("Json location source ref", 100, 2, 1);
		final MismatchedInputException ex = new DummyException(parser, "mismatchProperty", location);
		ex.prependPath(new JsonMappingException.Reference(new Object(), "dummy2"));
		ex.prependPath(new JsonMappingException.Reference(new Object(), "dummy1"));

		final ErrorMessage errorMessage = converter.convert(ex);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));

		assertThat(errorMessage.getDetails(), hasSize(1));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(errorMessage.getDetails().get(0).getField(), equalTo("dummy1.dummy2"));
		assertThat(errorMessage.getDetails().get(0).getMessage(), equalTo(JSON_PARSE_EXCEPTION_MESSAGE));
		assertThat(errorMessage.getDetails().get(0).getMoreInfo(), equalTo(String.format(JSON_PARSE_EXCEPTION_MORE_INFO, 2, 1)));
	}

	private static final class DummyException extends MismatchedInputException
	{
		protected DummyException(final JsonParser p, final String msg, final JsonLocation loc)
		{
			super(p, msg, loc);
		}
	}
}
