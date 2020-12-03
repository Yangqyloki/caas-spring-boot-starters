package com.hybris.caas.web.validator;

import com.hybris.caas.web.WrappedCollection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;

import static com.hybris.caas.web.validator.WrappedCollectionSizeValidator.PROPERTY_NODE_VALUE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class WrappedCollectionSizeValidatorTest
{
	private final WrappedCollectionSizeValidator validator = new WrappedCollectionSizeValidator();
	private final ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);
	private final ConstraintViolationBuilder builder = Mockito.mock(ConstraintViolationBuilder.class);
	private final NodeBuilderCustomizableContext nodeBuilder = Mockito.mock(NodeBuilderCustomizableContext.class);
	private final WrappedCollectionSize wrappedCollectionSize = Mockito.mock(WrappedCollectionSize.class);
	private WrappedCollection<String> wrappedCollection;

	@Before
	public void setUp()
	{
		wrappedCollection = WrappedCollection.of("foo", "bar", "baz");
		when(wrappedCollectionSize.min()).thenReturn(2);
		when(wrappedCollectionSize.max()).thenReturn(5);

		when(context.buildConstraintViolationWithTemplate(null)).thenReturn(builder);
		when(builder.addPropertyNode(PROPERTY_NODE_VALUE)).thenReturn(nodeBuilder);

		validator.initialize(wrappedCollectionSize);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_min_less_than_0()
	{
		when(wrappedCollectionSize.min()).thenReturn(-1);
		validator.initialize(wrappedCollectionSize);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_max_less_than_0()
	{
		when(wrappedCollectionSize.max()).thenReturn(-1);
		validator.initialize(wrappedCollectionSize);
		fail();
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_max_less_than_min()
	{
		when(wrappedCollectionSize.min()).thenReturn(10);
		validator.initialize(wrappedCollectionSize);
		fail();
	}

	@Test
	public void should_validate_null_wrappedCollection()
	{
		final boolean valid = validator.isValid(null, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_validate_null_wrappedCollectionValue()
	{
		wrappedCollection = new WrappedCollection<>();
		wrappedCollection.setValue(null);

		final boolean valid = validator.isValid(wrappedCollection, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_validate_min_max_file()
	{
		final boolean valid = validator.isValid(wrappedCollection, context);
		assertThat(valid, is(TRUE));
	}

	@Test
	public void should_fail_validate_less_than_min()
	{
		wrappedCollection = WrappedCollection.of("foo");
		final boolean valid = validator.isValid(wrappedCollection, context);
		assertThat(valid, is(FALSE));
	}

	@Test
	public void should_fail_validate_more_than_max()
	{
		wrappedCollection = WrappedCollection.of("foo", "bar", "baz", "baq", "bax", "bat");
		final boolean valid = validator.isValid(wrappedCollection, context);
		assertThat(valid, is(FALSE));
	}
}
