package com.hybris.caas.multitenant.service.config;

import com.hybris.caas.multitenant.service.config.TenantProperties.TenantPathProperties;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;
import java.util.regex.PatternSyntaxException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TenantProperiesTest
{
	private static final String TENANT = "tenant";
	private static final String FORWARDED_HOST_REGEX = "(.*)-domain";

	private TenantProperties tenantProperties;
	private TenantPathProperties pathProperties;

	@Before
	public void setUp()
	{
		this.tenantProperties = new TenantProperties();
		pathProperties = new TenantPathProperties();
	}

	@Test
	public void should_set_defaultProperties_on_setForwardedHostRegex()
	{
		tenantProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);

		assertThat(tenantProperties.getDefaultProperties().getForwardedHostRegex(), equalTo(FORWARDED_HOST_REGEX));
		assertThat(tenantProperties.getDefaultProperties().getPathPattern(), equalTo(TenantProperties.DEFAULT_PATH_PATTERN));
		assertThat(tenantProperties.getDefaultProperties().getCapturingGroup(), equalTo(TenantProperties.DEFAULT_CAPTURING_GROUP));
		assertThat(tenantProperties.getDefaultProperties().getAntPathMatcher(), notNullValue());
		assertThat(tenantProperties.getDefaultProperties().getPattern(), notNullValue());
	}

	@Test(expected = PatternSyntaxException.class)
	public void should_fail_to_create_pattern_invalid_regex_in_TenantProperties()
	{
		tenantProperties.setForwardedHostRegex("(*.)-invalid");
		fail();
	}

	@Test
	public void should_set_pattern_on_setForwardedHostRegex()
	{
		pathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);

		assertThat(pathProperties.getForwardedHostRegex(), equalTo(FORWARDED_HOST_REGEX));
		assertThat(pathProperties.getPattern(), notNullValue());
	}

	@Test(expected = PatternSyntaxException.class)
	public void should_fail_to_create_pattern_invalid_regex_in_TenantPathProperties()
	{
		pathProperties.setForwardedHostRegex("(*.)-invalid");
		fail();
	}

	@Test
	public void should_match_path()
	{
		pathProperties.setPathPattern("/foo/**/*");
		final boolean match = pathProperties.matchesPath("/foo/bar.baz");
		assertTrue(match);
	}

	@Test
	public void should_not_match_path_null()
	{
		pathProperties.setPathPattern("/foo/**/*");
		final boolean match = pathProperties.matchesPath(null);
		assertFalse(match);
	}

	@Test
	public void should_not_match_path_empty()
	{
		pathProperties.setPathPattern("/foo/**/*");
		final boolean match = pathProperties.matchesPath("");
		assertFalse(match);
	}

	@Test
	public void should_not_match_path_slash()
	{
		pathProperties.setPathPattern("/foo/**/*");
		final boolean match = pathProperties.matchesPath("/");
		assertFalse(match);
	}

	@Test
	public void should_not_match_path_invalid()
	{
		pathProperties.setPathPattern("/foo/**/*");
		final boolean match = pathProperties.matchesPath("/bar/baz.foo");
		assertFalse(match);
	}

	@Test
	public void should_getTenantFromHost_match()
	{
		pathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		final String tenant = pathProperties.getTenantFromHost("tenant-domain").get();
		assertThat(tenant, equalTo(TENANT));
	}

	@Test
	public void should_not_getTenantFromHost_no_match()
	{
		pathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		final Optional<String> tenant = pathProperties.getTenantFromHost("tenant-invalid");
		assertThat(tenant, equalTo(Optional.empty()));
	}

	@Test
	public void should_not_getTenantFromHost_invalid_capturingGroup()
	{
		pathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		pathProperties.setCapturingGroup(3);

		final Optional<String> tenant = pathProperties.getTenantFromHost("tenant-domain");
		assertThat(tenant, equalTo(Optional.empty()));
	}

	@Test
	public void should_getPropertiesForPath_match_found()
	{
		pathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		pathProperties.setCapturingGroup(1);
		pathProperties.setPathPattern("/foo/**/*");

		tenantProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		tenantProperties.setPaths(Collections.singletonList(pathProperties));

		final TenantPathProperties match = tenantProperties.getPropertiesForPath("/foo/bar.baz");
		assertThat(match, equalTo(pathProperties));
	}

	@Test
	public void should_getPropertiesForPath_no_match_uses_defaultProperties()
	{
		pathProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		pathProperties.setCapturingGroup(1);
		pathProperties.setPathPattern("/foo/**/*");

		tenantProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);
		tenantProperties.setPaths(Collections.singletonList(pathProperties));

		final TenantPathProperties match = tenantProperties.getPropertiesForPath("/bar/baz.foo");
		assertThat(match, equalTo(tenantProperties.getDefaultProperties()));
	}

	@Test
	public void should_getPropertiesForPath_no_paths_defined_uses_defaultProperties()
	{
		tenantProperties.setForwardedHostRegex(FORWARDED_HOST_REGEX);

		final TenantPathProperties match = tenantProperties.getPropertiesForPath("/bar/baz.foo");
		assertThat(match, equalTo(tenantProperties.getDefaultProperties()));
	}
}
