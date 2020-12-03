package com.hybris.caas.security.sanitization;

import org.owasp.html.PolicyFactory;

/**
 * Simple interface to make a class return an OWASP {@link PolicyFactory}.
 */
public interface HtmlPolicyAware
{
	/**
	 * Returns the policy associated with the given component.
	 * @return the policy or <code>null</code>
	 */
	PolicyFactory getPolicy();
}
