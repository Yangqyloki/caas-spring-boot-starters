package com.hybris.caas.data.jpa.querydsl;

import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

/**
 * Imitation of required {@link JPAExpressions} methods to allow mocking.
 */
public interface JPAExpression
{
	/**
	 * Create a new detached JPQLQuery instance with the given projection
	 *
	 * @param expr projection
	 * @param <T>
	 * @return select(expr)
	 */
	<T> JPQLQuery<T> select(Expression<T> expr);
}
