package com.hybris.caas.log.config;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import com.hybris.caas.log.audit.service.DataChangeObjectProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.hybris.caas.log.audit.service.AuditLogger;
import com.hybris.caas.log.context.UserProvider;
import com.sap.xs.audit.api.exception.AuditLogException;
import com.sap.xs.env.VcapServices;

@RunWith(MockitoJUnitRunner.class)
public class AuditLogConfigTest
{
	private static final String VCAP_SERVICES = "{\"auditlog\": [{\"credentials\": {\"password\": \"password\",\"url\": \"https://audit.com\",\"user\": \"user\",\"vendor\": \"SAP\"},"
			+ "\"plan\": \"standard\",\"label\": \"auditlog\",\"name\": \"caas2-auditlog\",\"tags\": [\"auditlog\"]}]}";

	@Spy
	private AuditLogConfig auditLogConfig;
	@Mock
	private DataChangeObjectProcessor dataChangeObjectProcessor;
	@Mock
	private UserProvider userProvider;

	private VcapServices vcapServices;
	private AuditLogProperties properties;

	@Before
	public void setUp()
	{
		properties = new AuditLogProperties();
		properties.setEnabled(true);
	}

	@Test(expected = AuditLogException.class)
	public void should_throw_AuditLogException_when_service_enabled_but_not_bound() throws AuditLogException
	{
		vcapServices = new VcapServices();
		when(auditLogConfig.getVcapServicesFromEnvironment()).thenReturn(vcapServices);

		auditLogConfig.auditLogger(properties, userProvider, dataChangeObjectProcessor);
	}

	@Test
	public void should_create_AuditLogger_when_service_enabled_and_bound() throws AuditLogException
	{
		vcapServices = VcapServices.from(VCAP_SERVICES);
		when(auditLogConfig.getVcapServicesFromEnvironment()).thenReturn(vcapServices);

		final AuditLogger auditLogger = auditLogConfig.auditLogger(properties, userProvider, dataChangeObjectProcessor);
		assertThat(auditLogger, notNullValue());
	}

	@Test
	public void should_create_AuditLogger_when_service_disabled() throws AuditLogException
	{
		properties.setEnabled(false);

		final AuditLogger auditLogger = auditLogConfig.auditLogger(properties, userProvider, dataChangeObjectProcessor);
		assertThat(auditLogger, notNullValue());
	}
}
