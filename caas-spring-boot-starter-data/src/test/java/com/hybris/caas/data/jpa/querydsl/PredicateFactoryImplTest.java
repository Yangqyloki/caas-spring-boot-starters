package com.hybris.caas.data.jpa.querydsl;

import com.querydsl.core.types.dsl.Expressions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PredicateFactoryImplTest
{
	PredicateFactoryImpl predicateFactory;

	@Before
	public void setUp() throws NoSuchMethodException
	{
		predicateFactory = new PredicateFactoryImpl();
	}

	@Test
	public void should_create_boolean_builder()
	{
		assertThat(predicateFactory.create(), is(notNullValue()));
	}

	@Test
	public void should_create_boolean_builder_with_predicate()
	{
		assert(predicateFactory.create(Expressions.TRUE).hasValue());
	}

}
