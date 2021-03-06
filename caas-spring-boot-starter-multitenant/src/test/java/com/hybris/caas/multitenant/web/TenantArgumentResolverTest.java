package com.hybris.caas.multitenant.web;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.hybris.caas.multitenant.service.TenantService;

@RunWith(MockitoJUnitRunner.class)
public class TenantArgumentResolverTest
{
	private static final String TENANT = "tenant";

	private TenantArgumentResolver argumentResolver;

	@Mock
	private TenantService tenantService;
	@Mock
	private MethodParameter methodParameter;
	@Mock
	private ModelAndViewContainer modelAndViewContainer;
	@Mock
	private NativeWebRequest nativeWebRequest;
	@Mock
	private HttpServletRequest httpServletRequest;
	@Mock
	private WebDataBinderFactory webDataBinderFactory;

	@Before
	public void setup() throws IOException
	{
		argumentResolver = new TenantArgumentResolver(tenantService);

		doReturn(String.class).when(methodParameter).getParameterType();
		when(methodParameter.hasParameterAnnotation(Tenant.class)).thenReturn(Boolean.TRUE);
		when(tenantService.getTenant(httpServletRequest)).thenReturn(TENANT);
		when(nativeWebRequest.getNativeRequest(HttpServletRequest.class)).thenReturn(httpServletRequest);
	}

	@Test
	public void should_support_parameter()
	{
		final boolean supported = argumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.TRUE));
	}

	@Test
	public void should_not_support_non_String()
	{
		doReturn(Integer.class).when(methodParameter).getParameterType();
		final boolean supported = argumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.FALSE));
	}

	@Test
	public void should_not_support_missing_annotation()
	{
		when(methodParameter.hasParameterAnnotation(Tenant.class)).thenReturn(Boolean.FALSE);
		final boolean supported = argumentResolver.supportsParameter(methodParameter);
		assertThat(supported, is(Boolean.FALSE));
	}

	@Test
	public void should_resolve_argument() throws Exception
	{
		final String tenant = (String) argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		assertThat(tenant, equalTo(TENANT));
	}

	@Test
	public void should_call_tenant_service() throws Exception
	{
		argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
		verify(tenantService).getTenant(httpServletRequest);
	}

}
