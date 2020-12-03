package com.hybris.caas.data.jpa.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class PredicateFactoryImpl implements PredicateFactory
{
	@Override
	public BooleanBuilder create()
	{
		return new BooleanBuilder();
	}

	@Override
	public BooleanBuilder create(final Predicate initial)
	{
		return new BooleanBuilder(initial);
	}
}
