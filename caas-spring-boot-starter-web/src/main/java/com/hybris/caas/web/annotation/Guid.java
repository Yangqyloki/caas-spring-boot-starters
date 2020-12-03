package com.hybris.caas.web.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hybris.caas.web.jackson.GuidDeserializer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define the resource id and convert the value of annotated parameter/field to lowercase.
 * The annotation can be used on PathVariable, RequestParameter and any property in the RequestBody in a Spring controller.
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonDeserialize(using = GuidDeserializer.class)
public @interface Guid
{
	// nothing to add
}
