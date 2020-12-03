package com.hybris.caas.error.converter.spring.db;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;

import javax.persistence.OptimisticLockException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class JpaOptimisticLockingFailureExceptionConverterTest
{
	private final JpaOptimisticLockingFailureExceptionConverter converter = new JpaOptimisticLockingFailureExceptionConverter();

	@Test
	public void should_convert_exception_to_error_message()
	{
		final JpaOptimisticLockingFailureException exception = new JpaOptimisticLockingFailureException(
				new OptimisticLockException("test message"));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(notNullValue()));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.CONFLICT.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_409_CONFLICT_RESOURCE));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));
	}
}
