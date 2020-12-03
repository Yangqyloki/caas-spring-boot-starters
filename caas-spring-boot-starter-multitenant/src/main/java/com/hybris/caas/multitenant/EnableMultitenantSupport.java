package com.hybris.caas.multitenant;

import com.hybris.caas.multitenant.jpa.config.JpaMultitenantConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ JpaMultitenantConfig.class })
@Documented
public @interface EnableMultitenantSupport
{
}
