package com.hybris.caas.kafka.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a custom annotation for {@link ValidCaasKafkaProperties}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CaasKafkaPropertiesValidator.class)
public @interface ValidCaasKafkaProperties
{
	String message() default "Invalid caas kafka properties.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
