package com.hybris.caas.test.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class CaasJwtToken
{
	private static final String NULL_EMAIL = null;

	static final String ACCOUNT_ID = "caas-test";
	static final String IDENTITY_ZONE = "caas";
	static final String IDENTITY_ZONE_ID = "caas-subaccount-id";
	static final String ISS = "http://%s.localhost:8080/uaa/oauth/token";
	static final String PRIVATE_KEY = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC03Q1oKC+n/kE3HBAPTI6VvJocfEb+5hA+8UH5Ho9MsiYqTS6BiZLr4Nj7MeLejbbbeDPhAjpngSwsSW8YofGWYq44fMyn5JizZmw9XNbPfQwkZWzfKNzesLG/+95ko/Fj7qkumN9L0doLlleIvBEcyDUIqGKVGciyZtKCDA0u8k8Gw55TPqY/GfawtBBnlczn10JAJVKvvvhGD9JBJEffbLXqEMNm/o/FGVPhjc7RS2RsNmSmOu7b7exIPFrSjuTJY9T8D6kfGkij9bfrIRP57ksDJhM1C5R7dJRuEWeX1X4gPjdw0P550pdIcS+bH2JZWfrJkMUWkjFYkBTqDvvnAgMBAAECggEAfQgcDpdJar9wYiK6T3lDUOyTNYIMqoZtULYEP64KrqCxlajJr69lrD9CgVUZW3VopgxRAQpDSe6YlP0nGkZJ9wa9HjvYV3DGx67UwQ7/SW1PCZI8U4GyPxizqN77/WMAq27/8kCojYfDTyNd3B73TYhycUvG6wBqKDrA6DpgmsSqgSi7Emw6fLv0r5hDZI7iu/v/OUuS/rXfdF3koTikaexaw48R+HyMdQe3lXhvajofxZqtCyLKzWYNLWLWUwg5iPMZ7K3Bqhpxx7e8rQDaTXa5otLpZkOMuKC7vBtvdYlKNjVHvjhmZAza25EZwyOh8GcG7TjPX22nJc5wTMsgQQKBgQDevo4z7pbMWvEUeM+cOz06qJ77qZjFb9jmILltPIt8thmmoyD2KeYZ+7ed00accUIGcefL0fBy0rCO6jRYeYHteGbr9wvftYoEVNB5XJedbLxL/K76qbZh3LNWEk4qINcF0+/6gsH1R9QciZ7pH96sq8F3+Hr6/Qr+cc9oZIjRVwKBgQDP3cfiuvQazGFKNkhE9AsNBtL0wDA+febq4pNEX+PPrbjJakxb53RDd0ptgiV+DcHSJyKh+Qf9fLgW7olFVyvta+Fr4rfgYGukI+B/onl3A1oenSj4pXpQ05b8w1oH53qw8PDByVdY3UOReByYEqS6ZtXMvBbnpnqVb+/dsj6/8QKBgDBgvs30sffsgJPrsVvDWgcVfII4UaAALOG+lcdt8PXDL0sL/yq36uwVycmTi7CKwMZZkvinvkuSCxrfL0NQELIdBm2VmJSb/z/1k9noDF36seLnHLIAjkYvAxxcmXF49Hqlj2efYrT9Rspz8Xm8nEZwf3KpGzzMHMkqutt3ime9AoGAHW46QdjUfQLY94ft/Y1MD0EbFzoq2n469qCz1yd4eswO8L2TKCQCKxc1u4GEy28pNRcDaUl4HyTMb7rARsgL2SbAGOVVwXgFZY0K2Sdo6TfZR6JazbGoOs9qfkkucxYMIgdLHzThoBxb17nY2pMxLY8n0EAFQjEpBAWAQbRd/BECgYByWnhh9jthIeo0FxE70LSBBs7hwqTH1CQWB39RDnn7/cy+h9zzrO6M/GoC6pq0EUYaoIFTgAIBmocTq+7EGnHZFhYPIBxOHIYZvHBt1jRqh3AbP8DA9jRJPzvzY53FzXXPn8kLurJ6TmbNBR9/7XLwYNM2ytqUBgdAwRmYeUgHJg==";
	static final ObjectMapper MAPPER = new ObjectMapper();

	public static final String DEFAULT_TOKEN = encode(ACCOUNT_ID,
		NULL_EMAIL,
		IDENTITY_ZONE,
		IDENTITY_ZONE,
		IDENTITY_ZONE_ID,
		PRIVATE_KEY,
		Collections.emptyList());

	private String accountId = ACCOUNT_ID;
	private String email;
	private String tenant = IDENTITY_ZONE;
	private String identityZone = IDENTITY_ZONE;
	private String identityZoneId = IDENTITY_ZONE_ID;
	private String privateKey = PRIVATE_KEY;
	private List<String> scopes = new ArrayList<>();

	public String getAccountId()
	{
		return accountId;
	}

	public void setAccountId(final String accountId)
	{
		this.accountId = accountId;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(final String email)
	{
		this.email = email;
	}

	public String getTenant()
	{
		return tenant;
	}

	public void setTenant(final String tenant)
	{
		this.tenant = tenant;
	}

	public String getIdentityZone()
	{
		return identityZone;
	}

	public String getSubaccountId()
	{
		return identityZoneId;
	}

	public void setIdentityZone(final String identityZone)
	{
		this.identityZone = identityZone;
	}

	public String getPrivateKey()
	{
		return privateKey;
	}

	public void setPrivateKey(final String privateKey)
	{
		this.privateKey = privateKey;
	}

	public List<String> getScopes()
	{
		return scopes;
	}

	public void setScopes(final List<String> scopes)
	{
		this.scopes = scopes;
	}

	static CaasJwtToken of(final TokenProperties.SingleTokenProperties tokenProperties)
	{
		final CaasJwtToken token = new CaasJwtToken();
		Optional.ofNullable(tokenProperties.getAccountId()).ifPresent(token::setAccountId);
		Optional.ofNullable(tokenProperties.getEmail()).ifPresent(token::setEmail);
		Optional.ofNullable(tokenProperties.getTenant()).ifPresent(token::setTenant);
		Optional.ofNullable(tokenProperties.getPrivateKey()).ifPresent(token::setPrivateKey);
		Optional.ofNullable(tokenProperties.getScopes()).ifPresent(token::setScopes);
		Optional.ofNullable(tokenProperties.getIdentityZone()).ifPresent(token::setIdentityZone);
		return token;
	}

	public String encode()
	{
		return encode(accountId, email, tenant, identityZone, identityZoneId, privateKey, scopes);
	}

	private static String encode(final String accountId,
		final String email,
		final String tenant,
		final String identityZone,
		final String identityZoneId,
		final String privateKey,
		final List<String> scopes)
	{
		final String clientId = identityZone + "!t1";
		final ObjectNode payload = MAPPER.createObjectNode();
		payload.put("client_id", clientId);
		payload.put("cid", clientId);
		payload.put("azp", clientId);
		payload.put("zid", identityZoneId);
		payload.put("iat", Integer.MIN_VALUE);
		payload.put("exp", Integer.MAX_VALUE);
		payload.put("email", email);

		payload.put("iss", String.format(ISS, identityZone));
		payload.put("grant_type", "authorization_code");
		final ArrayNode arrayNode = payload.putArray("scope");
		scopes.forEach(scope -> arrayNode.add(clientId + "." + scope));

		final ObjectNode extAttributes = payload.putObject("ext_attr");
		extAttributes.put("zdn", tenant);

		final ObjectNode userAttributes = payload.putObject("xs.user.attributes");
		userAttributes.putArray("accountId").add(accountId);

		try
		{
			// Extract RSA private key from Base64 encoded string.
			final byte[] encoded = Base64.decodeBase64(privateKey);
			final KeyFactory kf = KeyFactory.getInstance("RSA");
			final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

			final JWSSigner signer = new RSASSASigner(kf.generatePrivate(keySpec));
			final JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);
			final JWTClaimsSet claimsSet = JWTClaimsSet.parse(MAPPER.writeValueAsString(payload));

			final SignedJWT signedJWT = new SignedJWT(header, claimsSet);
			signedJWT.sign(signer);
			return signedJWT.serialize();
		}
		catch (final ParseException | JOSEException | JsonProcessingException | NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			throw new IllegalArgumentException("Invalid JWT private key.", e);
		}
	}

	public static Builder builder()
	{
		return new Builder();
	}

	public static class Builder
	{
		private CaasJwtToken token;

		public Builder()
		{
			this.token = new CaasJwtToken();
		}

		public Builder accountId(final String accountId)
		{
			token.setAccountId(accountId);
			return this;
		}

		public Builder email(final String email)
		{
			token.setEmail(email);
			return this;
		}

		public Builder tenant(final String tenant)
		{
			token.setTenant(tenant);
			return this;
		}

		public Builder identityZone(final String identityZone)
		{
			token.setIdentityZone(identityZone);
			return this;
		}

		public Builder privateKey(final String privateKey)
		{
			token.setPrivateKey(privateKey);
			return this;
		}

		public Builder scopes(final List<String> scopes)
		{
			token.setScopes(scopes);
			return this;
		}

		public CaasJwtToken build()
		{
			return token;
		}
	}

}
