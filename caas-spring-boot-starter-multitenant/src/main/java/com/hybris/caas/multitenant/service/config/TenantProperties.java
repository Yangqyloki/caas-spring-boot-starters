package com.hybris.caas.multitenant.service.config;

import com.hybris.caas.multitenant.jpa.aspect.TransactionTenantSetterAspect;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "tenant")
@Validated
public class TenantProperties
{
	private static final Logger LOG = LoggerFactory.getLogger(TenantProperties.class);

	static final String DEFAULT_PATH_PATTERN = "/**/*";
	static final int DEFAULT_CAPTURING_GROUP = 1;

	@NotEmpty
	private String forwardedHostRegex;

	@NotEmpty
	private String paasName;

	@Valid
	private List<TenantPathProperties> paths = new ArrayList<>();

	private TenantPathProperties defaultProperties = new TenantPathProperties();

	/**
	 * The "<code>multiTenantSessionProperty</code>" property specifies the
	 * default context property used to populate multitenant entities for the JPA implementation eclipselink.
	 *
	 * @see org.eclipse.persistence.annotations.Multitenant
	 * @see org.eclipse.persistence.annotations.TenantDiscriminatorColumn
	 * @see TransactionTenantSetterAspect
	 */
	@NotEmpty
	private String multiTenantSessionProperty = PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT;

	public String getForwardedHostRegex()
	{
		return forwardedHostRegex;
	}

	public String getPaasName()
	{
		return paasName;
	}

	public void setPaasName(final String paasName)
	{
		this.paasName = paasName;
	}

	public List<TenantPathProperties> getPaths()
	{
		return paths;
	}

	public void setPaths(final List<TenantPathProperties> paths)
	{
		this.paths = paths;
	}

	public TenantPathProperties getDefaultProperties()
	{
		return defaultProperties;
	}

	public void setDefaultProperties(final TenantPathProperties defaultProperties)
	{
		this.defaultProperties = defaultProperties;
	}

	public String getMultiTenantSessionProperty()
	{
		return multiTenantSessionProperty;
	}

	public void setMultiTenantSessionProperty(final String multiTenantSessionProperty)
	{
		this.multiTenantSessionProperty = multiTenantSessionProperty;
	}

	public void setForwardedHostRegex(final String forwardedHostRegex)
	{
		this.forwardedHostRegex = forwardedHostRegex;

		this.defaultProperties.setForwardedHostRegex(forwardedHostRegex);
		this.defaultProperties.setPathPattern(DEFAULT_PATH_PATTERN);
		this.defaultProperties.setCapturingGroup(DEFAULT_CAPTURING_GROUP);
	}

	/**
	 * Get the tenant properties that match the given request path.
	 *
	 * @param path the request path
	 * @return the tenant properties for the given path
	 */
	public TenantPathProperties getPropertiesForPath(final String path)
	{
		TenantPathProperties match = defaultProperties;
		for (final TenantPathProperties pathProperties : paths)
		{
			if (pathProperties.matchesPath(path))
			{
				match = pathProperties;
				break;
			}
		}
		return match;
	}

	@Validated
	public static class TenantPathProperties
	{
		private final AntPathMatcher antPathMatcher = new AntPathMatcher();

		@NotEmpty
		private String pathPattern;

		@NotEmpty
		private String forwardedHostRegex;
		private Pattern pattern;

		@Min(1)
		private int capturingGroup = 1;

		public AntPathMatcher getAntPathMatcher()
		{
			return antPathMatcher;
		}

		public String getPathPattern()
		{
			return pathPattern;
		}

		public void setPathPattern(final String pathPattern)
		{
			this.pathPattern = pathPattern;
		}

		public String getForwardedHostRegex()
		{
			return forwardedHostRegex;
		}

		public Pattern getPattern()
		{
			return pattern;
		}

		public void setPattern(final Pattern pattern)
		{
			this.pattern = pattern;
		}

		public int getCapturingGroup()
		{
			return capturingGroup;
		}

		public void setCapturingGroup(final int capturingGroup)
		{
			this.capturingGroup = capturingGroup;
		}

		public void setForwardedHostRegex(final String forwardedHostRegex)
		{
			this.forwardedHostRegex = forwardedHostRegex;
			this.pattern = Pattern.compile(forwardedHostRegex);
		}

		/**
		 * Gets the tenant from a given request host.
		 *
		 * @param forwardedHost the request host
		 * @return the tenant or empty optional
		 */
		public Optional<String> getTenantFromHost(final String forwardedHost)
		{
			return Optional.ofNullable(forwardedHost)
					.map(pattern::matcher)
					.filter(Matcher::matches)
					.filter(matcher -> isValidCapturingGroup(matcher, capturingGroup))
					.map(matcher -> matcher.group(capturingGroup))
					.filter(group -> !StringUtils.isEmpty(group));
		}

		boolean matchesPath(final String path)
		{
			if (Objects.isNull(path))
			{
				return false;
			}
			return antPathMatcher.match(this.pathPattern, path);
		}

		boolean isValidCapturingGroup(final Matcher matcher, final int capturingGroup)
		{
			boolean valid = true;
			if (capturingGroup > matcher.groupCount())
			{
				LOG.warn("Invalid tenant configuration. Please review your 'forwarded-host-regex' properties and ensure that the accompanying capturing groups are correct.");
				valid = false;
			}
			return valid;
		}
	}

}
