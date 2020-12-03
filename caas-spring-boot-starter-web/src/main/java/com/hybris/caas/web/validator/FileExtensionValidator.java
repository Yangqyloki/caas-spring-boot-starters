package com.hybris.caas.web.validator;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Check that the extension of a multipart filename matches the regex pattern (a concatenation of the prefix, value of the name property, and the suffix). The validation is case-insensitive.
 */
public class FileExtensionValidator implements ConstraintValidator<FileExtension, MultipartFile>
{
	private static final String REGEX_PREFIX = ".+\\.";
	private static final String REGEX_SUFFIX = "$";
	private Pattern pattern;
	private String name;

	@Override
	public void initialize(FileExtension parameters)
	{
		pattern = Pattern.compile(REGEX_PREFIX + parameters.name() + REGEX_SUFFIX, Pattern.CASE_INSENSITIVE);
		name = parameters.name();
	}

	/**
	 * Checks the file extension of the specified file.
	 *
	 * @param file                       The multipart file containing the filename to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 * @return Returns {@code true} if the file extension is the one specified in the name property, returns {@code false} otherwise.
	 */
	@Override
	public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext)
	{
		if (Objects.isNull(file))
		{
			return true;
		}

		if (Objects.isNull(file.getOriginalFilename()) || !pattern.matcher(file.getOriginalFilename()).matches())
		{
			final String message = String.format(constraintValidatorContext.getDefaultConstraintMessageTemplate(),
					name.toLowerCase(Locale.ENGLISH));
			constraintValidatorContext.disableDefaultConstraintViolation();
			constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();

			return false;
		}

		return true;
	}
}
