package com.hybris.caas.multitenant.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.hybris.caas.multitenant.TenantHolder;
import com.hybris.caas.multitenant.service.TenantService;

/**
 * Spring MVC argument resolver permitting to inject a {@link TenantHolder} into a controller.
 */
public class TenantHolderArgumentResolver implements HandlerMethodArgumentResolver
{
	private final TenantService tenantService;

	public TenantHolderArgumentResolver(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return TenantHolder.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest nativeWebRequest,
			WebDataBinderFactory binderFactory) throws Exception
	{
		final HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
		return TenantHolder.of(tenantService.getTenant(request));
	}

}
