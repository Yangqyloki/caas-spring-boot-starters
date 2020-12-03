package com.hybris.caas.security.sanitization;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlPolicyFactoryTest
{
	private final HtmlPolicyFactory factory = new HtmlPolicyFactory();

	@Mock
	private ApplicationContext context;

	private PolicyFactory policyContentRepository;
	private PolicyFactory policyProductContent;

	@Before
	public void setUp()
	{
		factory.setApplicationContext(context);
		policyContentRepository = new HtmlPolicyBuilder()
				.allowUrlProtocols("http", "https")
				.requireRelNofollowOnLinks()
				.allowStyling()
				.allowAttributes("lang").matching(Pattern.compile("[a-zA-Z]{2,20}")).globally()
				.allowAttributes("href").onElements("a")
				.allowAttributes("src").onElements("img")
				.allowAttributes("align", "margin")
				.matching(true, "center", "left", "right", "justify", "char", "0")
				.onElements("p")
				.allowElements("p", "u", "div", "i", "b", "em", "blockquote", "tt", "strong", "br", "ul", "ol", "li", "a", "img")
				.toFactory();

		policyProductContent = new HtmlPolicyBuilder()
			.allowElements("pre", "address", "em", "hr")
			.allowAttributes("class")
			.onElements("em")
			.toFactory()
			.and(Sanitizers.BLOCKS)
			.and(Sanitizers.FORMATTING)
			.and(Sanitizers.LINKS)
			.and(Sanitizers.TABLES)
			.and(Sanitizers.STYLES);

		final Map<String, PolicyFactory> beansMap = new HashMap<>();
		beansMap.put("policyContentRepository", policyContentRepository);
		beansMap.put("policyProductContent", policyProductContent);

		when(context.getBeansOfType(PolicyFactory.class)).thenReturn(beansMap);
	}

	@Test
	public void should_get_policy_by_bean_name()
	{
		factory.postConstruct();
		final PolicyFactory policy = HtmlPolicyFactory.getPolicy("policyContentRepository").get();

		assertThat(policy, equalTo(policyContentRepository));
	}

	@Test
	public void should_get_null_policy_bean_name_not_found()
	{
		factory.postConstruct();
		final Optional<PolicyFactory> policy = HtmlPolicyFactory.getPolicy("invalid");

		assertThat(policy, equalTo(Optional.empty()));
	}
}
