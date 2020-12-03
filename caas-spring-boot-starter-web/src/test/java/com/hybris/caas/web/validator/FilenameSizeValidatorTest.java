package com.hybris.caas.web.validator;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

public class FilenameSizeValidatorTest
{
	private final FilenameSizeValidator validator = new FilenameSizeValidator();
	private final MultipartFile file = Mockito.mock(MultipartFile.class);
	private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
	private final FilenameSize filenameSize = Mockito.mock(FilenameSize.class);

	@Before
	public void setUp()
	{
		when(file.getOriginalFilename()).thenReturn("test");
		when(filenameSize.min()).thenReturn(2);
		when(filenameSize.max()).thenReturn(5);

		validator.initialize(filenameSize);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_min_less_than_0()
	{
		when(filenameSize.min()).thenReturn(-1);
		validator.initialize(filenameSize);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_max_less_than_0()
	{
		when(filenameSize.max()).thenReturn(-1);
		validator.initialize(filenameSize);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_max_less_than_min()
	{
		when(filenameSize.min()).thenReturn(10);
		validator.initialize(filenameSize);
		fail();
	}

	@Test
	public void should_validate_null_file()
	{
		final boolean valid = validator.isValid(null, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_validate_min_max_file()
	{
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_fail_validate_null_filename()
	{
		when(file.getOriginalFilename()).thenReturn(null);
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}

	@Test
	public void should_fail_validate_less_than_min()
	{
		when(file.getOriginalFilename()).thenReturn("t");
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}

	@Test
	public void should_fail_validate_more_than_max()
	{
		when(file.getOriginalFilename()).thenReturn("test-test");
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}
}
