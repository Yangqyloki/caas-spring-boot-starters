package com.hybris.caas.error.converter.jackson;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import static com.hybris.caas.error.converter.jackson.AbstractJacksonExceptionConverter.JSON_PARSE_EXCEPTION_MORE_INFO;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InvalidDefinitionExceptionConverterTest
{
	private static final JsonLocation LOCATION = new JsonLocation("Json location source ref", 100, 2, 1);

	private final InvalidDefinitionExceptionConverter converter = new InvalidDefinitionExceptionConverter();

	@Mock
	private InvalidDefinitionException invalidDefinitionException;
	@Mock
	private BeanPropertyDefinition beanPropertyDefinition;

	@Before
	public void setUp()
	{
		when(invalidDefinitionException.getType()).thenReturn(TypeFactory.defaultInstance().unknownType());
		when(invalidDefinitionException.getLocation()).thenReturn(LOCATION);
		when(beanPropertyDefinition.getName()).thenReturn("dummy");
		when(invalidDefinitionException.getProperty()).thenReturn(beanPropertyDefinition);
	}

	@Test
	public void should_convert_to_error_message() throws Exception
	{
		final ErrorMessage errorMessage = converter.convert(invalidDefinitionException);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_400));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_VALIDATION_VIOLATION));
		assertThat(errorMessage.getDetails().get(0).getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_FIELD));
		assertThat(errorMessage.getDetails().get(0).getMoreInfo(), equalTo(String.format(JSON_PARSE_EXCEPTION_MORE_INFO, 2, 1)));
	}
}
