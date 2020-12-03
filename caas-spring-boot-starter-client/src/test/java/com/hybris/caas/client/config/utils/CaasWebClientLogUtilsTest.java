package com.hybris.caas.client.config.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(MockitoJUnitRunner.class)
public class CaasWebClientLogUtilsTest
{
	@Test
	public void should_obfuscate()
	{
		final StringBuilder sb = new StringBuilder();
		CaasWebClientLogUtils.filterHeaderValues(sb, List.of("Authorization"), "Authorization", List.of("abc"));
		assertThat(sb.toString(), equalTo("Authorization: *****, "));
	}

	@Test
	public void should_obfuscate_case_insensitive()
	{
		final StringBuilder sb = new StringBuilder();
		CaasWebClientLogUtils.filterHeaderValues(sb, List.of("authorization"), "Authorization", List.of("abc"));
		assertThat(sb.toString(), equalTo("Authorization: *****, "));
	}

	@Test
	public void should_not_obfuscate()
	{
		final StringBuilder sb = new StringBuilder();
		CaasWebClientLogUtils.filterHeaderValues(sb, List.of("Authorization"), "Accept-Language", List.of("fr-ca"));
		assertThat(sb.toString(), equalTo("Accept-Language: fr-ca, "));
	}
}
