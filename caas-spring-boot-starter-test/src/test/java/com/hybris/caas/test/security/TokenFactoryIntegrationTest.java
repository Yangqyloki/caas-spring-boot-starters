package com.hybris.caas.test.security;

import com.hybris.caas.test.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class TokenFactoryIntegrationTest
{
	@Autowired
	private TokenFactory tokenFactory;

	@Test
	public void should_populate_token_registry()
	{
		final CaasJwtToken developerToken = tokenFactory.getTokenObject("developer");
		assertThat(developerToken.getAccountId(), equalTo(CaasJwtToken.ACCOUNT_ID));
		assertThat(developerToken.getEmail(), is(nullValue()));
		assertThat(developerToken.getTenant(), startsWith("sap_"));
		assertThat(developerToken.getTenant().length(), greaterThanOrEqualTo(17));
		assertThat(developerToken.getPrivateKey(), equalTo(CaasJwtToken.PRIVATE_KEY));
		assertThat(developerToken.getScopes(), containsInAnyOrder("develop", "test"));

		final CaasJwtToken testUserToken = tokenFactory.getTokenObject("test-user");
		assertThat(testUserToken.getAccountId(), startsWith("Alice Test "));
		assertThat(testUserToken.getAccountId().length(), equalTo(47));
		assertThat(testUserToken.getEmail(), equalTo("some-test-users-email@test.com"));
		assertThat(testUserToken.getTenant(), equalTo("test"));
		assertThat(testUserToken.getPrivateKey(), equalTo(CaasJwtToken.PRIVATE_KEY));
		assertThat(testUserToken.getScopes(), containsInAnyOrder("read_only"));
	}

	@Test
	public void tokens_should_be_immutable()
	{
		final CaasJwtToken developerToken1 = tokenFactory.getTokenObject("developer");
		final CaasJwtToken developerToken2 = tokenFactory.getTokenObject("developer");
		assertFalse(developerToken1 == developerToken2);
	}
}
