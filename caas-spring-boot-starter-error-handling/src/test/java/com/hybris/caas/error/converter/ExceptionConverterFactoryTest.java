package com.hybris.caas.error.converter;

import com.hybris.caas.error.ErrorMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ValidationException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionConverterFactoryTest
{
	private ExceptionConverterFactory converter;;
	private final AbstractExceptionConverter<?> stubConverter = new StubConverter();
	private final AbstractExceptionConverter<?> defaultConverter = new StubDefaultConverter();
	private final AbstractExceptionConverter<?> rootCauseConverter = new StubRootCauseExceptionConverter();
	private ValidationException exception;

	@Mock
	private ErrorMessage stubErrorMessage;
	@Mock
	private ErrorMessage defaultErrorMessage;

	@Before
	public void setUp()
	{
		final Set<AbstractExceptionConverter<?>> abstractConverters = new HashSet<>();
		abstractConverters.add(stubConverter);
		abstractConverters.add(rootCauseConverter);

		converter = new ExceptionConverterFactory(defaultConverter);
		converter.setAbstractConverters(abstractConverters);
		converter.postConstruct();

		exception = new ValidationException(new IllegalArgumentException("TEST"));

		// Assert that converter map is properly loaded.
		assertThat(converter.getConverters().get(IllegalArgumentException.class), equalTo(stubConverter));
		assertThat(converter.getConverters().get(ValidationException.class), equalTo(rootCauseConverter));
	}

	@Test
	public void should_convert_with_loaded_converter_when_match()
	{
		final ErrorMessage errorMessage = converter.toErrorMessage(new IllegalArgumentException());
		assertThat(errorMessage, equalTo(stubErrorMessage));
	}

	@Test
	public void should_convert_with_default_converter_when_no_match()
	{
		final ErrorMessage errorMessage = converter.toErrorMessage(new IllegalStateException());
		assertThat(errorMessage, equalTo(defaultErrorMessage));
	}

	@Test
	public void should_convert_with_exception_converter_factory_standard_root_cause()
	{
		final ErrorMessage errorMessage = converter.toErrorMessage(exception);
		assertThat(errorMessage, equalTo(stubErrorMessage));
	}

	@Test
	public void should_convert_with_default_converter_no_root_cause()
	{
		exception = new ValidationException();
		final ErrorMessage errorMessage = converter.toErrorMessage(exception);
		assertThat(errorMessage, equalTo(defaultErrorMessage));
	}

	@Test
	public void should_convert_with_default_converter_self_root_cause()
	{
		final ValidationException spyException = Mockito.spy(new ValidationException());

		final ErrorMessage errorMessage = converter.toErrorMessage(spyException);
		assertThat(errorMessage, equalTo(defaultErrorMessage));
	}

	private class StubConverter extends AbstractExceptionConverter<IllegalArgumentException>
	{

		@Override
		protected ErrorMessage convert(IllegalArgumentException ex)
		{
			return stubErrorMessage;
		}
	}

	private class StubDefaultConverter extends AbstractExceptionConverter<Exception>
	{

		@Override
		protected ErrorMessage convert(Exception ex)
		{
			return defaultErrorMessage;
		}
	}

	private static class StubRootCauseExceptionConverter extends AbstractCauseExceptionConverter<ValidationException>
	{
		@Override
		protected boolean useRootCause()
		{
			return false;
		}
	}
}
