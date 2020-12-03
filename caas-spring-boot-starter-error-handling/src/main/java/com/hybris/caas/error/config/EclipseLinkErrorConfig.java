package com.hybris.caas.error.config;

import com.hybris.caas.error.converter.AbstractExceptionConverter;
import com.hybris.caas.error.converter.spring.db.eclipselink.AbstractDatasourceExceptionConverter;
import com.hybris.caas.error.converter.spring.db.eclipselink.EclipseLinkJpaSystemExceptionConverter;
import com.hybris.caas.error.converter.spring.db.eclipselink.EclipseLinkTransactionSystemExceptionConverter;
import com.hybris.caas.error.converter.spring.db.eclipselink.HeuristicCompletionExceptionConverter;
import com.hybris.caas.error.db.ExceptionToMessageMapper;
import com.hybris.caas.error.db.postgresql.PSQLArtifactToMessageMappingsProperties;
import com.hybris.caas.error.db.postgresql.PSQLExceptionToMessageMapper;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.TransactionSystemException;

@Configuration
@ConditionalOnClass(DatabaseException.class)
public class EclipseLinkErrorConfig
{
	private static final String PSQL_EXCEPTION_TO_MESSAGE_MAPPER_BEAN_NAME = "psqlExceptionToMessageMapper";

	@Bean
	public PSQLArtifactToMessageMappingsProperties psqlArtifactToMessageMappingsProperties()
	{
		return new PSQLArtifactToMessageMappingsProperties();
	}

	@Bean
	@ConditionalOnMissingBean(name = PSQL_EXCEPTION_TO_MESSAGE_MAPPER_BEAN_NAME)
	public ExceptionToMessageMapper<PSQLException> psqlExceptionToMessageMapper(
			final PSQLArtifactToMessageMappingsProperties psqlArtifactToMessageMappingsProperties)
	{
		return new PSQLExceptionToMessageMapper(psqlArtifactToMessageMappingsProperties);
	}

	@Bean
	public AbstractExceptionConverter<JpaSystemException> jpaSystemExceptionAbstractExceptionConverter(
			@Qualifier(PSQL_EXCEPTION_TO_MESSAGE_MAPPER_BEAN_NAME) final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		return new EclipseLinkJpaSystemExceptionConverter(exceptionToMessageMapper);
	}

	@Bean
	public AbstractExceptionConverter<TransactionSystemException> transactionSystemExceptionAbstractExceptionConverter(
			@Qualifier(PSQL_EXCEPTION_TO_MESSAGE_MAPPER_BEAN_NAME) final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		return new EclipseLinkTransactionSystemExceptionConverter(exceptionToMessageMapper);
	}

	@Bean
	public AbstractDatasourceExceptionConverter<HeuristicCompletionException> heuristicCompletionExceptionConverter(
			@Qualifier(PSQL_EXCEPTION_TO_MESSAGE_MAPPER_BEAN_NAME) final ExceptionToMessageMapper<PSQLException> exceptionToMessageMapper)
	{
		return new HeuristicCompletionExceptionConverter(exceptionToMessageMapper);
	}
}
