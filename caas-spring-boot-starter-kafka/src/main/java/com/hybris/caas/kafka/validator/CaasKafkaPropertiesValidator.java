package com.hybris.caas.kafka.validator;

import com.hybris.caas.kafka.config.CaasKafkaProperties;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Validates {@link CaasKafkaProperties} object by:
 * - ensuring that "transactionIdPrefix" property is not set for the retryable consumer listener when no retryable consumers are configured
 * - ensuring that "transactionIdPrefix" property is set for the retryable consumer listener when retryable consumers are configured
 * <p>
 * Important: The "transactionIdPrefix" property for a retryable consumer listener along with the same property available for Kafka producer
 * are used to ensure that the required beans are created when retryable consumer is used or not created when retryable consumers are not used.
 */
public class CaasKafkaPropertiesValidator implements ConstraintValidator<ValidCaasKafkaProperties, CaasKafkaProperties>
{
	static final String RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_PATH = "listener.retryable-consumer.transaction-id-prefix";
	static final String RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_REQUIRED = "retryable-consumer.transaction-id-prefix is required when using retryable consumer";
	static final String RETRYABLE_CONSUMER_TRANSACTION_ID_SET = "retryable-consumer.transaction-id-prefix should not be set when not using retryable consumer";

	@Override
	public void initialize(ValidCaasKafkaProperties constraintAnnotation)
	{
		// empty
	}

	@Override
	public boolean isValid(final CaasKafkaProperties value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value))
		{
			return true;
		}

		if (value.getRetryableConsumer().isEmpty())
		{
			if (nonNull(value.getListener().getRetryableConsumer().getTransactionIdPrefix()))
			{
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(RETRYABLE_CONSUMER_TRANSACTION_ID_SET)
						.addPropertyNode(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_PATH)
						.addConstraintViolation();

				return false;
			}
		}
		else
		{
			if (isEmpty(value.getListener().getRetryableConsumer().getTransactionIdPrefix()))
			{
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_REQUIRED)
						.addPropertyNode(RETRYABLE_CONSUMER_TRANSACTION_ID_PREFIX_PATH)
						.addConstraintViolation();

				return false;
			}
		}

		return true;
	}
}
