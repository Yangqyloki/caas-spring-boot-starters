package com.hybris.caas.web.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
public @interface JsonRequestMapping
{
	@AliasFor(annotation = RequestMapping.class) String name() default "";

	@AliasFor(annotation = RequestMapping.class) String[] value() default {};

	@AliasFor(annotation = RequestMapping.class) String[] path() default {};

	@AliasFor(annotation = RequestMapping.class) String[] params() default {};

	@AliasFor(annotation = RequestMapping.class) String[] headers() default {};
}
