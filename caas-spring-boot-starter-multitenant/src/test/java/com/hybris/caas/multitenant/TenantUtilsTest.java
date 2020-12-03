package com.hybris.caas.multitenant;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TenantUtilsTest
{

	@Test
	public void shouldReturnTrueForValidTenantIdentifiers()
	{
		assertThat(TenantUtils.isValid("t")).isTrue();
		assertThat(TenantUtils.isValid("T")).isTrue();
		assertThat(TenantUtils.isValid("0")).isTrue();
		assertThat(TenantUtils.isValid("9")).isTrue();
		assertThat(TenantUtils.isValid(".")).isTrue();
		assertThat(TenantUtils.isValid("-")).isTrue();
		assertThat(TenantUtils.isValid("tenant")).isTrue();
		assertThat(TenantUtils.isValid("TENANT")).isTrue();
		assertThat(TenantUtils.isValid("t3n4nt")).isTrue();
		assertThat(TenantUtils.isValid("T3N4NT")).isTrue();
		assertThat(TenantUtils.isValid("tenant-a")).isTrue();
		assertThat(TenantUtils.isValid("tenant.a")).isTrue();
		assertThat(TenantUtils.isValid("T3-N4NT.")).isTrue();
	}

	@Test
	public void shouldReturnFalseForInvalidTenantIdentifiers()
	{
		assertThat(TenantUtils.isValid(null)).isFalse();
		assertThat(TenantUtils.isValid("")).isFalse();
		assertThat(TenantUtils.isValid("@")).isFalse();
		assertThat(TenantUtils.isValid("ten@nt")).isFalse();
		assertThat(TenantUtils.isValid("TEN@NT")).isFalse();
		assertThat(TenantUtils.isValid("tenant_a")).isFalse();
	}
}
