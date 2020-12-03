package com.hybris.caas.web.validator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintValidatorContext;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileExtensionValidatorTest
{
	private static final String VALID_EXTENSION = "test.zip";
	private static final String CASE_INSENSITIVE_EXTENSION = "test.ZIp";
	private static final String INVALID_EXTENSION = "test.txt";

	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;
	@Spy
	private FileExtensionValidator validator;
	@Mock
	private FileExtension extension;

	private final MultipartFile file = Mockito.mock(MultipartFile.class);
	private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

	@Before
	public void setUp()
	{
		when(context.getDefaultConstraintMessageTemplate()).thenReturn("dummy-message %s");
		when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(constraintViolationBuilder);
		when(extension.name()).thenReturn("zip");
		validator.initialize(extension);
	}

	@Test
	public void should_validate_null_file()
	{
		final boolean valid = validator.isValid(null, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_validate_valid_zip_file()
	{
		when(file.getOriginalFilename()).thenReturn(VALID_EXTENSION);
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_fail_validation_of_zip_file_with_invalid_extension()
	{
		when(file.getOriginalFilename()).thenReturn(INVALID_EXTENSION);
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}

	@Test
	public void should_validate_case_insensitively_a_valid_zip_file()
	{

		when(file.getOriginalFilename()).thenReturn(CASE_INSENSITIVE_EXTENSION);
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_fail_validation_of_a_null_zip_filename()
	{

		when(file.getOriginalFilename()).thenReturn(null);
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}
}
