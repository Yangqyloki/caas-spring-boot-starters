package com.hybris.caas.error.converter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ValidationException;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCauseExceptionConverterTest
{
	private final AbstractExceptionConverter<ValidationException> converter = new StubRootCauseExceptionConverter();

	@Test(expected = UnsupportedOperationException.class)
	public void should_throw_exception_cant_convert_from_this_converter()
	{
		converter.convert(new ValidationException());
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
