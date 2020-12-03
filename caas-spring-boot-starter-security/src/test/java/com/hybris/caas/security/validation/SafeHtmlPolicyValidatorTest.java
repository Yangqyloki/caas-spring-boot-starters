package com.hybris.caas.security.validation;

import com.hybris.caas.security.sanitization.HtmlPolicyFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.context.ApplicationContext;

import javax.validation.ConstraintValidatorContext;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SafeHtmlPolicyValidatorTest
{
	private static final String POLICY_STR = "POLICY1";
	private static final PolicyFactory POLICY = new HtmlPolicyBuilder().allowUrlProtocols("https")
			.allowAttributes("src")
			.onElements("img")
			.allowElements("p", "img")
			.toFactory();
	private static final String UNSANITIZED_HTML = "<script><div><p>DUMMY</p></div></script>";
	private static final String SANITIZED_HTML = "<p>DUMMY</p>";
	private static final String SANITIZED_HTML_SPECIAL_VALUES = "<img src=\"https://www.xyz.com/hyper-precise.jpg?context=abc\">";

	private final HtmlPolicyFactory factory = new HtmlPolicyFactory();
	private SafeHtmlPolicyValidator validator;

	@Mock
	private ApplicationContext appContext;
	@Mock
	private SafeHtmlPolicy annotation;
	@Mock
	private ConstraintValidatorContext validatorContext;

	@Before
	public void setupHtmlPolicyFactory()
	{
		factory.setApplicationContext(appContext);

		final Map<String, PolicyFactory> beansMap = new HashMap<>();
		beansMap.put(POLICY_STR, POLICY);

		when(appContext.getBeansOfType(PolicyFactory.class)).thenReturn(beansMap);
		factory.postConstruct();
	}

	@Before
	public void setupValidator()
	{
		validator = new SafeHtmlPolicyValidator();
		when(annotation.policy()).thenReturn(POLICY_STR);
	}

	@Test
	public void should_initialize_validator_policy_found()
	{
		validator.initialize(annotation);

		assertThat(validator.getPolicy(), equalTo(POLICY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_fail_initialize_validator_policy_not_found()
	{
		when(annotation.policy()).thenReturn(null);
		validator.initialize(annotation);
		fail();
	}

	@Test
	public void should_be_valid_on_valid_input()
	{
		validator.initialize(annotation);
		final boolean isvalid = validator.isValid(SANITIZED_HTML, validatorContext);
		assertTrue(isvalid);
	}

	@Test
	public void should_be_valid_on_null_input()
	{
		validator.initialize(annotation);
		final boolean isvalid = validator.isValid(null, validatorContext);
		assertTrue(isvalid);
	}

	@Test
	public void should_be_valid_on_empty_input()
	{
		validator.initialize(annotation);
		final boolean isvalid = validator.isValid("", validatorContext);
		assertTrue(isvalid);
	}

	@Test
	public void should_be_valid_on_special_characters()
	{
		validator.initialize(annotation);
		final boolean isvalid = validator.isValid(SANITIZED_HTML_SPECIAL_VALUES, validatorContext);
		assertTrue(isvalid);
	}

	@Test
	public void should_be_invalid_on_invalid_input()
	{
		validator.initialize(annotation);
		final boolean isvalid = validator.isValid(UNSANITIZED_HTML, validatorContext);
		assertFalse(isvalid);
	}
}
