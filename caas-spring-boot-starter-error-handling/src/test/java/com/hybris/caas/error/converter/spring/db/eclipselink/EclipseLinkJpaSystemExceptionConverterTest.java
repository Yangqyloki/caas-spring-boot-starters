package com.hybris.caas.error.converter.spring.db.eclipselink;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.db.postgresql.PSQLArtifactToMessageMappingsProperties;
import com.hybris.caas.error.db.postgresql.PSQLExceptionToMessageMapper;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class EclipseLinkJpaSystemExceptionConverterTest
{
	private PSQLExceptionToMessageMapper spyExceptionToMessageMapper;
	private EclipseLinkJpaSystemExceptionConverter converter;

	@Before
	public void setUp()
	{
		spyExceptionToMessageMapper = Mockito.spy(new PSQLExceptionToMessageMapper(new PSQLArtifactToMessageMappingsProperties()));
		converter = new EclipseLinkJpaSystemExceptionConverter(spyExceptionToMessageMapper);
	}

	@Test
	public void should_convert_exception_to_internal_server_error_message_for_generic_database_exception()
	{
		final JpaSystemException exception = new JpaSystemException(DatabaseException.sqlException(new PSQLException(null, null)));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_500));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));

		verify(spyExceptionToMessageMapper).map(any(PSQLException.class));
	}

	@Test
	public void should_convert_exception_to_conflict_error_message_for_unique_constraint_violation()
	{
		final JpaSystemException exception = new JpaSystemException(DatabaseException.sqlException(
				new PSQLException("duplicate key value violates unique constraint \"item_pkey\"", null)));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(notNullValue()));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.CONFLICT.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_409_CONFLICT_RESOURCE));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));
		assertThat(errorMessage.getDetails(), is(empty()));

		verify(spyExceptionToMessageMapper).map(any(PSQLException.class));
	}

	@Test
	public void should_convert_exception_and_not_invoke_psql_mapper()
	{
		final JpaSystemException exception = new JpaSystemException(new IllegalArgumentException("dymmy"));

		final ErrorMessage errorMessage = converter.convert(exception);

		assertThat(errorMessage.getMessage(), is(notNullValue()));
		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_500));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.INTERNAL_SERVER_ERROR.value()));

		verifyNoInteractions(spyExceptionToMessageMapper);
	}
}
