package com.hybris.caas.data.jpa.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

public class JPAExpressionImpl implements JPAExpression
{
	@Override
	public <T> JPQLQuery<T> select(final Expression<T> expr)
	{
		return JPAExpressions.select(expr);
	}
}
