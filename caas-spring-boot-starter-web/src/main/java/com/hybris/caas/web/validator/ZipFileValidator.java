package com.hybris.caas.web.validator;

import java.io.IOException;
import java.util.Objects;
import java.util.zip.ZipException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.springframework.web.multipart.MultipartFile;

/**
 * Check that the content of a multipart file is a valid zip file.
 */
public class ZipFileValidator implements ConstraintValidator<com.hybris.caas.web.validator.ZipFile, MultipartFile>
{
	@Override
	public void initialize(com.hybris.caas.web.validator.ZipFile parameters)
	{
		// Nothing to do here
	}

	/**
	 * Checks that the contents of the multipart file can be opened as a zip archive.
	 *
	 * @param file The multipart file containing the bytes to validate.
	 * @param constraintValidatorContext context in which the constraint is evaluated.
	 *
	 * @return Returns {@code true} if the file is {@code null} or the length of {@link MultipartFile#getOriginalFilename()} is between the specified
	 *         {@code min} and {@code max} values (inclusive), {@code false} otherwise.
	 */
	@Override
	@SuppressWarnings("squid:S1166")
	public boolean isValid(MultipartFile file, ConstraintValidatorContext constraintValidatorContext)
	{
		// Allow null, since this is not a null check validator.
		if (Objects.isNull(file))
		{
			return true;
		}

		// Get bytes from file.
		final byte[] bytes;
		try
		{
			bytes = file.getBytes();
		}
		catch (final IOException e)
		{
			throw new IllegalArgumentException("Unable to read zip file.", e);
		}

		// If there are no bytes, then this is not a valid zip file.
		if (Objects.isNull(bytes) || bytes.length <= 0)
		{
			return false;
		}

		/* If the bytes can be read into a zip file successfully, then this is a valid zip file. Is a ZipException occurs, then
		 * this is an invalid zip file. If an IOException occurs, then a runtime exception is thrown.
		 */
		try (final SeekableInMemoryByteChannel inMemoryByteChannel = new SeekableInMemoryByteChannel(bytes);
				final ZipFile zipFile = createZipFile(inMemoryByteChannel))
		{
			return true;
		}
		catch (final ZipException e)
		{
			return false;
		}
		catch (final IOException e)
		{
			throw new IllegalArgumentException("Unable to read zip file.", e);
		}
	}

	protected ZipFile createZipFile(final SeekableInMemoryByteChannel inMemoryByteChannel) throws IOException
	{
		return new ZipFile(inMemoryByteChannel);
	}

}
