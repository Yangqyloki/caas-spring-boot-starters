package com.hybris.caas.web.validator;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * Check that the length of a multipart filename is between min and max.
 */
public class FilenameSizeValidator implements ConstraintValidator<FilenameSize, MultipartFile>
{
	private static final Log LOG = LoggerFactory.make(MethodHandles.lookup());

	private int min;
	private int max;

	@Override
	public void initialize(FilenameSize parameters)
	{
		min = parameters.min();
		max = parameters.max();
		validateParameters();
	}

	/**
	 * Checks the length of the specified file's filename.
	 *
	 * @param file The multipart file containing the filename to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns {@code true} if the file is {@code null} or the length of {@link MultipartFile#getOriginalFilename()} is between the specified
	 *         {@code min} and {@code max} values (inclusive), {@code false} otherwise.
	 */
	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext)
	{
		if (Objects.isNull(file))
		{
			return true;
		}
		if (Objects.isNull(file.getOriginalFilename()))
		{
			return false;
		}
		final String fileName = file.getOriginalFilename();
		if (fileName == null) {
			throw new IllegalArgumentException();
		}
		final int length = fileName.length();
		return length >= min && length <= max;
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
