package com.hybris.caas.security.sanitization;

import com.hybris.caas.security.config.SecurityConfig;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The HTML policy factory will find all beans of type {@link PolicyFactory} and
 * add them to a static map. The factory then allows to look for a policy by the
 * original bean name.
 */
public class HtmlPolicyFactory implements ApplicationContextAware
{
	private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);
	private static Map<String, PolicyFactory> policies = new HashMap<>();
	private ApplicationContext applicationContext;

	/**
	 * Get the policy matching the given bean name.
	 *
	 * @param name the name of the bean that declares the policy being search for.
	 * @return the policy
	 */
	public static Optional<PolicyFactory> getPolicy(final String name)
	{
		return Optional.ofNullable(policies.get(name));
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext)
	{
		this.applicationContext = applicationContext;
	}

	@PostConstruct
	@SuppressWarnings({"squid:S2696", "findbugs:ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD"})
	public void postConstruct()
	{
		policies = this.applicationContext.getBeansOfType(PolicyFactory.class);
		LOG.info("CaaS Security - loaded {} HTML sanitization policies.", policies.size());
	}

}
