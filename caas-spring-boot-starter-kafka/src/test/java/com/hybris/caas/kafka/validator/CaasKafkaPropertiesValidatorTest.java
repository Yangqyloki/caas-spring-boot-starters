package com.hybris.caas.kafka.validator;

import com.hybris.caas.kafka.config.CaasKafkaProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.ConstraintValidatorContext;
import java.util.Collections;

import static com.hybris.caas.kafka.validator.CaasKafkaPropertiesValidator.RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_PATH;
import static com.hybris.caas.kafka.validator.CaasKafkaPropertiesValidator.RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_REQUIRED;
import static com.hybris.caas.kafka.validator.CaasKafkaPropertiesValidator.RETRYABLE_CONSUMER_TRANSACTION_ID_SET;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaasKafkaPropertiesValidatorTest
{
	@Mock
	private ConstraintValidatorContext context;
	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder constraintViolationBuilder;
	@Mock
	private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilderCustomizableContext;

	private CaasKafkaPropertiesValidator validator = new CaasKafkaPropertiesValidator();

	@Before
	public void setUp()
	{
		when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(constraintViolationBuilder);
		when(constraintViolationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
	}

	@Test
	public void should_pass_when_object_is_null()
	{
		final boolean valid = validator.isValid(null, context);

		assertThat(valid, is(true));
	}

	@Test
	public void should_pass_when_retryable_consumer_is_not_used_and_transactionIdPrefix_is_not_set()
	{
		final boolean valid = validator.isValid(new CaasKafkaProperties(), context);

		assertThat(valid, is(true));
	}

	@Test
	public void should_fail_when_using_retryable_consumer_and_transactionIdPrefix_is_not_set()
	{
		final CaasKafkaProperties properties = new CaasKafkaProperties();
		properties.setRetryableConsumerMap(Collections.singletonMap("dummy", new CaasKafkaProperties.RetryableConsumer()));

		final boolean valid = validator.isValid(properties, context);

		assertThat(valid, is(false));

		verify(context).disableDefaultConstraintViolation();
		verify(context).buildConstraintViolationWithTemplate(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_REQUIRED);
		verify(constraintViolationBuilder).addPropertyNode(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_PATH);
		verify(nodeBuilderCustomizableContext).addConstraintViolation();
	}

	@Test
	public void should_pass_when_using_retryable_consumer_and_transactionIdPrefix_is_set()
	{
		final CaasKafkaProperties properties = new CaasKafkaProperties();
		properties.setRetryableConsumerMap(Collections.singletonMap("dummy", new CaasKafkaProperties.RetryableConsumer()));
		properties.getListener().getRetryableConsumer().setTransactionIdPrefix("");

		final boolean valid = validator.isValid(new CaasKafkaProperties(), context);

		assertThat(valid, is(true));
	}

	@Test
	public void should_fail_when_not_using_retryable_consumer_and_transactionIdPrefix_is_set()
	{
		final CaasKafkaProperties properties = new CaasKafkaProperties();
		properties.getListener().getRetryableConsumer().setTransactionIdPrefix("");

		final boolean valid = validator.isValid(properties, context);

		assertThat(valid, is(false));

		verify(context).disableDefaultConstraintViolation();
		verify(context).buildConstraintViolationWithTemplate(RETRYABLE_CONSUMER_TRANSACTION_ID_SET);
		verify(constraintViolationBuilder).addPropertyNode(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_PATH);
		verify(nodeBuilderCustomizableContext).addConstraintViolation();
	}
}
