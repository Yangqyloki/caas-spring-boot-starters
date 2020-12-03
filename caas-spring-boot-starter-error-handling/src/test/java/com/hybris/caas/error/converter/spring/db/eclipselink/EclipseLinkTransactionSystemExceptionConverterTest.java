package com.hybris.caas.error.converter.spring.db.eclipselink;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.db.postgresql.PSQLArtifactToMessageMappingsProperties;
import com.hybris.caas.error.db.postgresql.PSQLExceptionToMessageMapper;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.junit.Test;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class EclipseLinkTransactionSystemExceptionConverterTest
{
	private static final String TEST_MESSAGE = "test message";

	private final EclipseLinkTransactionSystemExceptionConverter converter = new EclipseLinkTransactionSystemExceptionConverter(
			new PSQLExceptionToMessageMapper(new PSQLArtifactToMessageMappingsProperties()));

	@Test
	public void should_convert_exception_to_internal_server_error_message()
	{
		final TransactionSystemException exception = new TransactionSystemException(TEST_MESSAGE);

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_500));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));
	}

	@Test
	public void should_convert_exception_to_internal_server_error_message_for_generic_database_exception()
	{
		final TransactionSystemException exception = new TransactionSystemException(TEST_MESSAGE,
				DatabaseException.sqlException(new PSQLException(null, null)));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_500));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));
	}

	@Test
	public void should_convert_exception_to_conflict_error_message_for_unique_constraint_violation()
	{
		final TransactionSystemException exception = new TransactionSystemException(TEST_MESSAGE, DatabaseException.sqlException(
				new PSQLException("duplicate key value violates unique constraint \"item_pkey\"", null)));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(notNullValue()));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.CONFLICT.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_409_CONFLICT_RESOURCE));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));
	}

	@Test
	public void should_convert_exception_to_foreign_key_error_message_for_foreign_key_constraint_violation()
	{
		final TransactionSystemException exception = new TransactionSystemException(TEST_MESSAGE, DatabaseException.sqlException(
				new PSQLException("ERROR: insert or update on table \"my_table\" violates foreign key constraint \"my_table_fkey\"",
						null)));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(notNullValue()));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.BAD_REQUEST.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_400_BUSINESS_ERROR));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));
	}

}
