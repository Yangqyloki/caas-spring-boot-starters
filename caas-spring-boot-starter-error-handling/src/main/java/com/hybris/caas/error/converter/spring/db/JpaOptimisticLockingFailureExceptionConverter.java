package com.hybris.caas.error.converter.spring.db;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;

/**
 * Converts {@link JpaOptimisticLockingFailureException} to canonical error message.
 */
public class JpaOptimisticLockingFailureExceptionConverter extends AbstractExceptionConverter<JpaOptimisticLockingFailureException>
{
	private static final String MESSAGE = "The resource has already been modified. Please only modify the latest version.";

	@Override
	protected ErrorMessage convert(final JpaOptimisticLockingFailureException ex)
	{
		return ErrorMessage.builder()
				.withMessage(MESSAGE)
				.withStatus(HttpStatus.CONFLICT.value())
				.withType(ErrorConstants.TYPE_409_CONFLICT_RESOURCE)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
