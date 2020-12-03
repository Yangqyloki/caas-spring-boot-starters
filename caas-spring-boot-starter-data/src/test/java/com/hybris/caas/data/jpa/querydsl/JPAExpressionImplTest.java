package com.hybris.caas.data.jpa.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JPAExpressionImplTest
{
	JPAExpressionImpl expressionImpl;
	Expression expression;

	@Before
	public void setUp() throws NoSuchMethodException
	{
		expressionImpl = new JPAExpressionImpl();
		expression = Expressions.TRUE;
	}

	@Test
	public void should_select_expression()
	{
		assertThat(expressionImpl.select(expression), is(JPAExpressions.select(expression)));
	}

}