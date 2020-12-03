package com.hybris.caas.error.config;

import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.converter.spring.db.EmptyResultDataAccessExceptionConverter;
import com.hybris.caas.error.converter.spring.db.JpaOptimisticLockingFailureExceptionConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.orm.jpa.JpaOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;

@Configuration
@ConditionalOnClass(EntityManager.class)
public class JpaErrorConfig
{
	@Bean
	@ConditionalOnClass(EntityManager.class)
	public AbstractExceptionConverter<JpaOptimisticLockingFailureException> jpaOptimisticLockingFailureExceptionAbstractExceptionConverter()
	{
		return new JpaOptimisticLockingFailureExceptionConverter();
	}

	@Bean
	@ConditionalOnClass(PlatformTransactionManager.class)
	public AbstractExceptionConverter<EmptyResultDataAccessException> emptyResultDataAccessExceptionAbstractExceptionConverter()
	{
		return new EmptyResultDataAccessExceptionConverter();
	}
}
