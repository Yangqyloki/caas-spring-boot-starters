package com.hybris.caas.multitenant.jpa.annotation;

import com.hybris.caas.multitenant.jpa.aspect.TransactionTenantSetterAspect;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that the annotated method should trigger the setting of the tenant for the current transaction.
 * This annotation is intended to be used along with {@link Transactional} annotation that makes sure that the transaction
 * is started while allowing customization of the transactional properties. Besides this annotation being applied, there
 * are additional requirements for the method and the class providing the annotated method.
 * Please see {@link TransactionTenantSetterAspect} for more details about the additional requirements.
 */

@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface TenantSetter
{
}
