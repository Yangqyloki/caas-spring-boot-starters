package com.hybris.caas.web.pagination;

import com.hybris.caas.web.sort.util.CommaSeparatedParameterWebRequest;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Objects;

/**
 * Spring MVC argument resolver permitting to inject a spring data {@link Pageable} param
 * into a controller. The controller parameter must be of type {@link Pageable}.
 * This implementation will delegate the resolving to its super class and then apply the {@link MaxPageSize} limit if available.
 */
public class CaasPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver
{
	public CaasPageableHandlerMethodArgumentResolver(SortArgumentResolver sortResolver)
	{
		super(sortResolver);
	}

	@Override
	public Pageable resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest nativeWebRequest,
			WebDataBinderFactory binderFactory)
	{
		Pageable pageable = resolveFromSuper(parameter, mavContainer, nativeWebRequest, binderFactory);

		// If the MaxPageSize annotation is present, then ensure that the page size provided is under or equal to this upper bound.
		final MaxPageSize maxPageSize = parameter.getParameterAnnotation(MaxPageSize.class);
		if (Objects.nonNull(maxPageSize) && Objects.nonNull(pageable) && pageable.getPageSize() > maxPageSize.value())
		{
			pageable = PageRequest.of(pageable.getPageNumber(), maxPageSize.value(), pageable.getSort());
		}
		return pageable;
	}

	Pageable resolveFromSuper(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest nativeWebRequest,
			WebDataBinderFactory binderFactory)
	{
		return super.resolveArgument(parameter, mavContainer, new CommaSeparatedParameterWebRequest(nativeWebRequest), binderFactory);
	}
}