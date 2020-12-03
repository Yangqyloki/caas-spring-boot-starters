package com.hybris.caas.error.converter.spring.db;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class EmptyResultDataAccessExceptionConverterTest
{
	private final EmptyResultDataAccessExceptionConverter converter = new EmptyResultDataAccessExceptionConverter();

	@Test
	public void should_convert_exception_to_error_message() throws Exception
	{
		final EmptyResultDataAccessException exception = new EmptyResultDataAccessException(
				"No class com.hybris.caas.persistence.entity.MyItem entity "
						+ "with id com.hybris.caas.persistence.entity.MyItemPk exists!", 1);

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(notNullValue()));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.NOT_FOUND.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));

	}

}
