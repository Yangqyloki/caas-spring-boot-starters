package com.hybris.caas.test.integration;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"tenant.multiTenantSessionProperty=eclipselink.tenant-id", "spring.kafka.listener.missing-topics-fatal=false",
		"spring.application.name=dummy-service"})
@AutoConfigureWireMock(port = 0)
public abstract class AbstractIntegrationTest
{
	// Nothing to add here
}
