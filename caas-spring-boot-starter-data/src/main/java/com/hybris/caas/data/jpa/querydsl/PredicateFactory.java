package com.hybris.caas.data.jpa.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

/**
 * Creates instance of type {@link BooleanBuilder}.
 */
public interface PredicateFactory
{
	/**
	 * Creates a {@link BooleanBuilder} which is an implementation of {@link Predicate}
	 *
	 * @return a {@link BooleanBuilder} instance
	 */
	BooleanBuilder create();

	/**
	 * Creates a {@link BooleanBuilder} which is an implementation of {@link Predicate} with the initial
	 * {@link Predicate}
	 *
	 * @param initial the initial {@link Predicate}
	 * @return a {@link BooleanBuilder} instance
	 */
	BooleanBuilder create(Predicate initial);
}
