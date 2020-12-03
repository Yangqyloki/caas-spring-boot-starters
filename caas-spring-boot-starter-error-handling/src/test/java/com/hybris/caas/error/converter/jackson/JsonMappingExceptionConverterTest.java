package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.exception.InvalidEnumValueException;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.Collections;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

public class JsonMappingExceptionConverterTest
{
	private final JsonMappingExceptionConverter converter = new JsonMappingExceptionConverter();

	@Test
	public void should_convert_to_error_message()
	{
		final JsonLocation location = new JsonLocation("Json location source ref", 100, 2, 1);
		final JsonMappingException ex = new JsonMappingException(null, "Unrecognized token 'value'", location);
		final ErrorMessage errorMessage = converter.convert(ex);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));

		assertThat(errorMessage.getDetails(), hasSize(1));
		assertThat(errorMessage.getDetails().get(0).getMessage(), equalTo(JsonMappingExceptionConverter.JSON_PARSE_EXCEPTION_MESSAGE));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(errorMessage.getDetails().get(0).getField(), notNullValue());
		assertThat(errorMessage.getDetails().get(0).getMoreInfo(), notNullValue());
	}

	@Test
	public void should_convert_to_error_message_with_nested_cause()
	{
		final Exception e = new InvalidEnumValueException("EnumFieldName", Collections.singleton("VALID_ENUM_VALUE"),
				"PROVIDED_VALUE");
		final JsonMappingException ex = new JsonMappingException(null, "Message ...", e);
		final ErrorMessage errorMessage = converter.convert(ex);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BAD_PAYLOAD_SYNTAX));

		assertThat(errorMessage.getDetails(), hasSize(1));
		assertThat(errorMessage.getDetails().get(0).getMessage(), containsString("Invalid enum value"));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(errorMessage.getDetails().get(0).getField(), containsString("EnumFieldName"));
		assertThat(errorMessage.getDetails().get(0).getMoreInfo(), nullValue());
	}
}
