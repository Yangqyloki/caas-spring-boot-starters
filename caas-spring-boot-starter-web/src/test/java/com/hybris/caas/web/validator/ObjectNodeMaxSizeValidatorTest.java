package com.hybris.caas.web.validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectNodeMaxSizeValidatorTest
{
	@Mock
	private ConstraintValidatorContext context;
	@Mock
	private ObjectNodeMaxSize validObjectNodeMaxSize;
	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;

	private ObjectNodeMaxSizeValidator validator = new ObjectNodeMaxSizeValidator();
	private ObjectNode node = new ObjectMapper().createObjectNode();

	@Test
	public void testIsValid() throws Exception
	{
		when(context.getDefaultConstraintMessageTemplate()).thenReturn("dummy-message %s");
		when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(constraintViolationBuilder);
		when(validObjectNodeMaxSize.max()).thenReturn(15);
		validator.initialize(validObjectNodeMaxSize);
		node.put("test", "abc");

		assertThat("the object is null", validator.isValid(null, context), is(true));
		assertThat("the object size is valid", validator.isValid(node, context), is(true));

		node.put("adding", "gt 15 char");
		assertThat("the object size is too large", validator.isValid(node, context), is(false));
	}
}
