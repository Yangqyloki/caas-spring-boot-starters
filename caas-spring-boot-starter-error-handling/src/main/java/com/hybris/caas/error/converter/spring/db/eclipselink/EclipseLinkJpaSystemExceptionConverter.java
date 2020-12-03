package com.hybris.caas.error.converter.spring.db.eclipselink;

import com.hybris.caas.error.db.ExceptionToMessageMapper;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import org.springframework.orm.jpa.JpaSystemException;

/**
 * Converts {@link JpaSystemException} wrapped in a {@link DatabaseException} to canonical error message.
 */
public class EclipseLinkJpaSystemExceptionConverter extends AbstractDatasourceExceptionConverter<JpaSystemException>
{
	public EclipseLinkJpaSystemExceptionConverter(final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		super(exceptionToMessageMapper);
	}
}
