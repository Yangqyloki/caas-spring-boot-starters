package com.hybris.caas.multitenant.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.hybris.caas.multitenant.service.TenantService;

/**
 * Spring MVC argument resolver permitting to inject a tenant string
 * into a controller. The controller parameter must be of type {@link String}
 * and must be annotated with {@link Tenant}.
 */
public class TenantArgumentResolver implements HandlerMethodArgumentResolver
{
	private final TenantService tenantService;

	public TenantArgumentResolver(final TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return String.class.equals(parameter.getParameterType()) && parameter.hasParameterAnnotation(Tenant.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest nativeWebRequest,
			WebDataBinderFactory binderFactory) throws Exception
	{
		final HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
		return tenantService.getTenant(request);
	}

}
