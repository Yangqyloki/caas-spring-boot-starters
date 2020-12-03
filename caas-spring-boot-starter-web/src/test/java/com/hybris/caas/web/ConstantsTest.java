package com.hybris.caas.web;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertTrue;

public class ConstantsTest
{
	@Test
	public void should_test_audited_object_constructor_is_private() throws NoSuchMethodException
	{
		Constructor constructor = Constants.class.getDeclaredConstructor();
		assertTrue(Modifier.isPrivate(constructor.getModifiers()));
	}
}
