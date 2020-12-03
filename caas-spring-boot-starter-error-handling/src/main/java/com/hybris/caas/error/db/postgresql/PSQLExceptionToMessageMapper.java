package com.hybris.caas.error.db.postgresql;

import com.hybris.caas.error.db.ExceptionToMessageMapper;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * PostgreSQL based implementation of {@link ExceptionToMessageMapper}.
 */
public class PSQLExceptionToMessageMapper implements ExceptionToMessageMapper<PSQLException>
{
	private final Map<String, String> constraintToMessage;

	public PSQLExceptionToMessageMapper(final PSQLArtifactToMessageMappingsProperties psqlArtifactToMessageMappingsProperties)
	{
		this.constraintToMessage = psqlArtifactToMessageMappingsProperties.getConstraintToMessageMappings();
	}

	@Override
	public Optional<String> map(final PSQLException exception)
	{
		if (PSQLStateExtra.UNIQUE_VIOLATION.getState().equals(exception.getSQLState()) ||
			PSQLStateExtra.FOREIGN_KEY_VIOLATION.getState().equals(exception.getSQLState()))
		{
			final ServerErrorMessage serverErrorMessage = exception.getServerErrorMessage();

			if (Objects.nonNull(serverErrorMessage))
			{
				return Optional.ofNullable(constraintToMessage.get(serverErrorMessage.getConstraint()));
			}
		}

		return Optional.empty();
	}
}
