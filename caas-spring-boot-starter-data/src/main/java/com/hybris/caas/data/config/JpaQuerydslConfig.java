package com.hybris.caas.data.config;

import com.hybris.caas.data.jpa.querydsl.JPAExpressionImpl;
import com.hybris.caas.data.jpa.querydsl.PredicateFactoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(com.querydsl.core.Query.class)
public class JpaQuerydslConfig
{

	@Bean
	public JPAExpressionImpl jpaExpressionImpl()
	{
		return new JPAExpressionImpl();
	}

	@Bean
	public PredicateFactoryImpl predicateFactoryImpl()
	{
		return new PredicateFactoryImpl();
	}

}
