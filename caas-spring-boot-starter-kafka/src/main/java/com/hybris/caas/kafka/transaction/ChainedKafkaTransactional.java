package com.hybris.caas.kafka.transaction;

import org.springframework.core.annotation.AliasFor;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that the annotated method requires chained kafka transaction manager.<br>
 * see {@link ChainedKafkaTransactionManager}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional("chainedKafkaTxManager")
public @interface ChainedKafkaTransactional
{
	@AliasFor(annotation = Transactional.class) Class<? extends Throwable>[] rollbackFor() default {};
}
