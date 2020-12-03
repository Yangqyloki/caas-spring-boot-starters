package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static com.hybris.caas.error.ErrorConstants.EXCEPTION_MESSAGE_BODY_INVALID;
import static com.hybris.caas.error.converter.jackson.AbstractJacksonExceptionConverter.JSON_PARSE_EXCEPTION_MORE_INFO;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InvalidFormatExceptionConverterTest
{
	private static final JsonLocation LOCATION = new JsonLocation("Json location source ref", 100, 2, 1);

	private final InvalidFormatExceptionConverter converter = new InvalidFormatExceptionConverter();

	@Mock
	private InvalidFormatException invalidFormatException;

	@Before
	public void setUp()
	{
		doReturn(String.class).when(invalidFormatException).getTargetType();
		when(invalidFormatException.getLocation()).thenReturn(LOCATION);
	}

	@Test
	public void should_convert_to_error_message()
	{
		final ErrorMessage errorMessage = converter.convert(invalidFormatException);

		assertThat(errorMessage.getMessage(), equalTo(EXCEPTION_MESSAGE_BODY_INVALID));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
	}

	@Test
	public void should_retrieve_invalid_field_name()
	{
		final List<JsonMappingException.Reference> path = new ArrayList<>();
		path.add(new JsonMappingException.Reference("dummy-object", "ref1"));
		path.add(new JsonMappingException.Reference("dummy-object", "ref2"));
		path.add(new JsonMappingException.Reference("dummy-object", "ref3"));

		when(invalidFormatException.getPath()).thenReturn(path);

		final String invalidField = converter.retrieveFieldName(invalidFormatException);
		assertThat(invalidField, equalTo("ref1.ref2.ref3"));
	}

	@Test
	public void should_build_error_location_details()
	{
		final String locationMessage = converter.buildLocationMessage(LOCATION);
		assertThat(locationMessage, equalTo(String.format(JSON_PARSE_EXCEPTION_MORE_INFO, 2, 1)));
	}
}
