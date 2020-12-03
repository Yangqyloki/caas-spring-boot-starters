package com.hybris.caas.web.validator;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class ZipFileValidatorTest
{
	@Spy
	private ZipFileValidator validator;

	@Mock
	private ZipFile zipFile;
	private final MultipartFile file = Mockito.mock(MultipartFile.class);
	private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

	@Before
	public void setUp() throws IOException, URISyntaxException
	{
		final Path path = Paths.get(getClass().getClassLoader().getResource("valid.zip").toURI());
		when(file.getBytes()).thenReturn(Files.readAllBytes(path));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_validation_IOException_when_getBytes() throws IOException
	{
		when(file.getBytes()).thenThrow(IOException.class);
		validator.isValid(file, context);
		fail();
	}

	@Test
	public void should_validate_null_file()
	{
		final boolean valid = validator.isValid(null, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_validate_valid_zip_file() throws URISyntaxException, IOException
	{
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_fail_validate_null_bytes() throws IOException
	{
		when(file.getBytes()).thenReturn(null);
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}

	@Test
	public void should_fail_validate_empty_byte_array() throws IOException
	{
		when(file.getBytes()).thenReturn(new byte[] {});
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}

	@Test
	public void should_fail_validate_ZipException() throws IOException, URISyntaxException
	{
		final Path path = Paths.get(getClass().getClassLoader().getResource("invalid.zip").toURI());
		when(file.getBytes()).thenReturn(Files.readAllBytes(path));
		final boolean valid = validator.isValid(file, context);
		assertThat(valid, is(FALSE));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_validation_IOException_when_new_ZipFile() throws IOException
	{
		Mockito.doThrow(IOException.class).when(validator).createZipFile(Mockito.any());
		validator.isValid(file, context);
		fail();
	}


}
