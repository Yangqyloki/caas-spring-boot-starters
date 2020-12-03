package com.hybris.caas.test.security;

import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.text.ParseException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CaasJwtTokenTest
{
	private static final String EXPECTED_TOKEN = "{\"ext_attr\":{\"zdn\":\"caas\"},\"zid\":\"caas-subaccount-id\",\"grant_type\":\"authorization_code\",\"xs.user.attributes\":{\"accountId\":[\"caas-test\"]},\"azp\":\"caas!t1\",\"scope\":[],\"iss\":\"http:\\/\\/caas.localhost:8080\\/uaa\\/oauth\\/token\",\"exp\":2147483647,\"iat\":-2147483648,\"client_id\":\"caas!t1\",\"cid\":\"caas!t1\"}";

	@Test
	public void should_create_encoded_token() throws ParseException
	{
		final String token = CaasJwtToken.DEFAULT_TOKEN;

		final SignedJWT jwt = SignedJWT.parse(token);
		assertThat(jwt.getJWTClaimsSet().toString(), equalTo(EXPECTED_TOKEN));
	}

	@Test
	public void builder_should_populate_default_values() throws ParseException
	{
		final String token = CaasJwtToken.builder().build().encode();

		final SignedJWT jwt = SignedJWT.parse(token);
		assertThat(jwt.getJWTClaimsSet().toString(), equalTo(EXPECTED_TOKEN));
	}
}
