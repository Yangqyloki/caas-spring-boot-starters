package com.hybris.caas.web.validator;

import com.hybris.caas.web.WrappedCollection;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * Validate the {@link WrappedCollection} has a valid size.
 */
public class WrappedCollectionSizeValidator implements ConstraintValidator<WrappedCollectionSize, WrappedCollection<?>>
{
	private static final Log LOG = LoggerFactory.make(MethodHandles.lookup());
	static final String PROPERTY_NODE_VALUE = "value";

	private int min;
	private int max;

	@Override
	public void initialize(WrappedCollectionSize parameters)
	{
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	@Override
	public boolean isValid(final WrappedCollection<?> value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value) || Objects.isNull(value.getValue()))
		{
			return true;
		}

		int size = value.getValue().size();
		boolean isValid = size >= min && size <= max;

		if (!isValid)
		{
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
					.addPropertyNode(PROPERTY_NODE_VALUE)
					.addConstraintViolation();
		}
		return isValid;
	}

	private void validateParameters()
	{
		if ( min < 0 )
		{
			throw LOG.getMinCannotBeNegativeException();
		}
		if ( max < 0 )
		{
			throw LOG.getMaxCannotBeNegativeException();
		}
		if ( max < min )
		{
			throw LOG.getLengthCannotBeNegativeException();
		}
	}
}
