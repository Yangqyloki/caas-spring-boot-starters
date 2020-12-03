package com.hybris.caas.data.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JpaQuerydslConfigTest
{
	private JpaQuerydslConfig config;

	@Before
	public void setUp() throws NoSuchMethodException
	{
		config = new JpaQuerydslConfig();
	}

	@Test
	public void should_return_JPA_Expresison()
	{
		assertThat(config.jpaExpressionImpl(), is(notNullValue()));
	}

	@Test
	public void should_return_Predicate_Factory()
	{
		assertThat(config.predicateFactoryImpl(), is(notNullValue()));
	}
}
