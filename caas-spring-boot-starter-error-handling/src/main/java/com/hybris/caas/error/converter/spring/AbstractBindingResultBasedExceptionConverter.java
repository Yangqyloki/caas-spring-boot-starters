package com.hybris.caas.error.converter.spring;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.converter.AbstractExceptionConverter;

/**
 * Abstract base class for all validation errors that use a
 * {@link BindingResult} to gather field errors.
 *
 * The following default values are used:
 * <ul>
 * <li>status: <code>400</code></li>
 * <li>type: <code>validation_violation</code></li>
 * <li>moreInfo: </li>
 * </ul>
 *
 * @param <E>
 *            Exception type handled by this converter.
 */
public abstract class AbstractBindingResultBasedExceptionConverter<E extends Exception>
		extends AbstractExceptionConverter<E>
{

	@Override
	public ErrorMessage convert(E ex)
	{
		final ErrorMessage errorMessage = ErrorMessage.builder()
				.withMessage(ErrorConstants.MESSAGE_400)
				.withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION)
				.withMoreInfo(ErrorConstants.INFO)
				.build();

		errorMessage.setDetails(getBindingResult(ex).getFieldErrors().stream()
				.map(AbstractBindingResultBasedExceptionConverter::toDetail)
				.collect(Collectors.toList()));

		return errorMessage;
	}

	/**
	 * Implementing classes should provide the binding result to the abstract
	 * class.
	 *
	 * @param ex
	 *            - the exception to convert
	 * @return - the spring binding result
	 */
	protected abstract BindingResult getBindingResult(E ex);

	private static ErrorMessageDetail toDetail(FieldError fieldError)
	{
		return ErrorMessageDetail.builder()
				.withField(fieldError.getField())
				.withType(ErrorConstants.SUB_TYPE_400_INVALID_FIELD)
				.withMessage(fieldError.getDefaultMessage())
				.withMoreInfo(ErrorConstants.INFO)
				.build();
	}

}
