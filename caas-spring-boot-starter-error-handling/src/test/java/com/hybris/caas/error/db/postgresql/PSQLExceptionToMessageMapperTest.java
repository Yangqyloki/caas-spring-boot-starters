package com.hybris.caas.error.db.postgresql;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.postgresql.util.ServerErrorMessage;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PSQLExceptionToMessageMapperTest
{
	private static final String DUMMY_CONSTRAINT = "dummy_constraint";
	private static final String DUMMY_MESSAGE = "dummy_message";

	@Mock
	private ServerErrorMessage serverErrorMessage;

	private final PSQLArtifactToMessageMappingsProperties properties = new PSQLArtifactToMessageMappingsProperties();

	@Before
	public void setUp()
	{
		properties.setConstraintToMessageMappings(Collections.singletonMap(DUMMY_CONSTRAINT, DUMMY_MESSAGE));

		when(serverErrorMessage.toString()).thenReturn("dummy");
		when(serverErrorMessage.getSQLState()).thenReturn(PSQLStateExtra.UNIQUE_VIOLATION.getState());
		when(serverErrorMessage.getConstraint()).thenReturn(DUMMY_CONSTRAINT);
	}

	private void foreignSetup()
	{
		when(serverErrorMessage.getSQLState()).thenReturn(PSQLStateExtra.FOREIGN_KEY_VIOLATION.getState());
	}

	private void assertConstraintMessage()
	{
		final PSQLExceptionToMessageMapper mapper = new PSQLExceptionToMessageMapper(properties);
		Optional<String> message = mapper.map(new PSQLException(serverErrorMessage));

		assertTrue(message.isPresent());
		assertThat(message.get(), equalTo(DUMMY_MESSAGE));
	}

	@Test
	public void should_map_constraint_to_message()
	{
		assertConstraintMessage();
	}


	@Test
	public void should_map_foreign_constraint_to_message()
	{
		foreignSetup();
		assertConstraintMessage();
	}


	@Test
	public void should_not_map_constraint_to_message_when_mapping_not_defined()
	{
		properties.setConstraintToMessageMappings(Collections.emptyMap());

		final PSQLExceptionToMessageMapper mapper = new PSQLExceptionToMessageMapper(properties);
		Optional<String> message = mapper.map(new PSQLException(serverErrorMessage));

		assertFalse(message.isPresent());
	}

	@Test
	public void should_not_map_constraint_to_message_when_not_unique_violation_exception()
	{
		when(serverErrorMessage.getSQLState()).thenReturn(PSQLState.TOO_MANY_RESULTS.getState());

		final PSQLExceptionToMessageMapper mapper = new PSQLExceptionToMessageMapper(properties);
		Optional<String> message = mapper.map(new PSQLException(serverErrorMessage));

		assertFalse(message.isPresent());
	}
}
