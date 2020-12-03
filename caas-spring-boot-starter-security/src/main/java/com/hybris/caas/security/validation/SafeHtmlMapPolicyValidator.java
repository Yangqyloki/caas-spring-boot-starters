package com.hybris.caas.security.validation;

import com.hybris.caas.security.sanitization.HtmlPolicyFactory;
import org.owasp.html.HtmlChangeListener;
import org.owasp.html.PolicyFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Validate that the given HTML Map string values meet the requested sanitization policy.
 */
public class SafeHtmlMapPolicyValidator implements ConstraintValidator<SafeHtmlPolicy, Map<?, String>>
{
	private PolicyFactory policy;

	@Override
	public void initialize(final SafeHtmlPolicy constraintAnnotation)
	{
		policy = HtmlPolicyFactory.getPolicy(constraintAnnotation.policy())
				.orElseThrow(() -> new IllegalArgumentException(
						"No HTML policy found that matches the name: " + constraintAnnotation.policy()));
	}

	/**
	 * Validates that the values of the Map meet the requested sanitization policy.
	 *
	 * @param value   a map with string values
	 * @param context context in which the constraint is evaluated
	 * @return {@code false} if {@code value} does not pass the constraint
	 */
	@Override
	public boolean isValid(final Map<?, String> value, final ConstraintValidatorContext context)
	{
		if (Objects.isNull(value))
		{
			return Boolean.TRUE;
		}
		//AtomicBoolean is used instead of wrapper Boolean to avoid sonar issues when the content is changed inside of listener.
		final AtomicBoolean discardedContent = new AtomicBoolean(false);
		value.values().forEach(html -> policy.sanitize(html, new SanitizeListener(), discardedContent));
		return !discardedContent.get();
	}

	public PolicyFactory getPolicy()
	{
		return this.policy;
	}

	private static class SanitizeListener implements HtmlChangeListener<AtomicBoolean>
	{
		@Override
		public void discardedTag(final AtomicBoolean context, final String elementName)
		{
			context.set(true);
		}

		@Override
		public void discardedAttributes(final AtomicBoolean context, final String tagName, final String... attributeNames)
		{
			context.set(true);
		}
	}
}
