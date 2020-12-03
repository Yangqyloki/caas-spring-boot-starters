package com.hybris.caas.error.converter.spring.db.eclipselink;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.db.ExceptionToMessageMapper;
import org.postgresql.util.PSQLException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.hybris.caas.error.ErrorConstants.MESSAGE_500;

/**
 * Base converter class for converting datasource exception to canonical error message.
 */
public abstract class AbstractDatasourceExceptionConverter<T extends RuntimeException> extends AbstractExceptionConverter<T>
{
	private static final String UNIQUE_CONSTRAINT_MESSAGE = "There is already a resource with the same unique identifier(s).";
	private static final String FOREIGN_CONSTRAINT_MESSAGE = "Resource referenced by identifier(s) not found.";

	private static final String VIOLATES_FOREIGN_KEY_CONSTRAINT = "violates foreign key constraint";
	private static final List<String> UNIQUE_CONSTRAINT_VIOLATION_MESSAGES = Arrays.asList("unique", "duplicate key",
			"primary key violation");

	private final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper;

	public AbstractDatasourceExceptionConverter(final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		this.exceptionToMessageMapper = exceptionToMessageMapper;
	}

	@Override
	protected ErrorMessage convert(final T ex)
	{
		return Stream.of(uniqueConstraint(), foreignKeyConstraint())
				.map(f -> f.apply(ex))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst()
				.orElseGet(() -> buildDefaultErrorMessage(mapExceptionToMessageWithDefault(ex, MESSAGE_500)));
	}

	protected final String mapExceptionToMessageWithDefault(final T ex, final String defaultMessage)
	{
		final Throwable cause = NestedExceptionUtils.getMostSpecificCause(ex);
		if (cause instanceof PSQLException)
		{
			final PSQLException psqlException = (PSQLException) cause;
			return exceptionToMessageMapper.map(psqlException).orElse(defaultMessage);
		}

		return defaultMessage;
	}

	private Function<T, Optional<ErrorMessage>> uniqueConstraint()
	{
		return t -> Optional.ofNullable(NestedExceptionUtils.getRootCause(t))
				.map(Throwable::getMessage)
				.filter(message -> UNIQUE_CONSTRAINT_VIOLATION_MESSAGES.stream().anyMatch(message::contains))
				.map(s -> buildErrorMessageForUniqueConstraint(mapExceptionToMessageWithDefault(t, UNIQUE_CONSTRAINT_MESSAGE)));
	}

	private Function<T, Optional<ErrorMessage>> foreignKeyConstraint()
	{
		return t -> Optional.ofNullable(NestedExceptionUtils.getRootCause(t))
				.map(Throwable::getMessage)
				.filter(message -> message.contains(VIOLATES_FOREIGN_KEY_CONSTRAINT))
				.map(s -> buildErrorMessageForForeignConstraint(mapExceptionToMessageWithDefault(t, FOREIGN_CONSTRAINT_MESSAGE)));
	}

	private static ErrorMessage buildErrorMessageForUniqueConstraint(final String message)
	{
		return ErrorMessage.builder()
				.withMessage(message)
				.withStatus(HttpStatus.CONFLICT.value())
				.withType(ErrorConstants.TYPE_409_CONFLICT_RESOURCE)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}

	private static ErrorMessage buildErrorMessageForForeignConstraint(final String message)
	{
		return ErrorMessage.builder()
				.withMessage(message)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_BUSINESS_ERROR)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}

	private static ErrorMessage buildDefaultErrorMessage(final String message)
	{
		return ErrorMessage.builder()
				.withMessage(message)
				.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.withType(ErrorConstants.TYPE_500_INTERNAL_SERVER_ERROR)
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}
}
