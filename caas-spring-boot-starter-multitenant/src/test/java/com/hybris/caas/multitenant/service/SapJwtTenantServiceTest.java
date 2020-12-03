package com.hybris.caas.multitenant.service;

import com.hybris.caas.multitenant.service.config.TenantProperties;
import com.hybris.caas.multitenant.service.exception.MissingTenantException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

import static com.hybris.caas.multitenant.Constants.TENANT_ATTRIBUTE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SapJwtTenantServiceTest
{
	@Mock
	private HttpServletRequest request;
	@Mock
	private TenantProperties tenantProperties;

	private SapJwtTenantService tenantService;

	private final String tenantStored = "tenant";

	@Before
	public void setUp()
	{
		tenantService = new SapJwtTenantService(tenantProperties) {
			@Override
			HttpServletRequest getRequest()
			{
				return request;
			}
		};
	}

	@Test
	public void shouldGetTenant()
	{
		when(request.getAttribute(TENANT_ATTRIBUTE_NAME)).thenReturn(tenantStored);

		final String tenant = tenantService.getTenant();

		assertThat(tenant).isEqualTo(tenantStored);
	}

	@Test
	public void shouldThrowMissingTenantExceptionWitPublicAccessType()
	{
		final MissingTenantException ex = assertThrows(MissingTenantException.class, () -> tenantService.getTenant());
		assertThat(ex.getAccessType()).isEqualTo(MissingTenantException.AccessType.PUBLIC);
	}

	@Test
	public void shouldThrowMissingTenantExceptionWitProtectedAccessType()
	{
		when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("some auth string");

		final MissingTenantException ex = assertThrows(MissingTenantException.class, () -> tenantService.getTenant());
		assertThat(ex.getAccessType()).isEqualTo(MissingTenantException.AccessType.PROTECTED);
	}

	@Test
	public void shouldIndicatePaasTenant()
	{
		when(tenantProperties.getPaasName()).thenReturn(tenantStored);
		when(request.getAttribute(TENANT_ATTRIBUTE_NAME)).thenReturn(tenantStored);

		assertThat(tenantService.isPaasTenant()).isTrue();
	}

	@Test
	public void shouldIndicateNotAPaasTenant()
	{
		when(tenantProperties.getPaasName()).thenReturn("different paas tenant");
		when(request.getAttribute(TENANT_ATTRIBUTE_NAME)).thenReturn(tenantStored);

		assertThat(tenantService.isPaasTenant()).isFalse();
	}

}
