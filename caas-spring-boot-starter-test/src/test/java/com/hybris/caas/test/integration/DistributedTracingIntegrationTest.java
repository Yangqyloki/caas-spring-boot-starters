package com.hybris.caas.test.integration;

import brave.ScopedSpan;
import brave.Tracing;
import com.hybris.caas.kafka.transaction.SyncKafkaTemplate;
import com.hybris.caas.test.integration.util.TracingAssertions;
import com.hybris.caas.test.security.CaasJwtToken;
import com.sap.hcp.cf.logging.common.Fields;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sap.hcp.cf.logging.common.request.HttpHeaders.CORRELATION_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.http.HttpMethod.GET;

public class DistributedTracingIntegrationTest extends AbstractIntegrationTest
{
	private static final Logger LOG = LoggerFactory.getLogger(DistributedTracingIntegrationTest.class);
	private static final String X_FORWARDED_FOR = com.sap.hcp.cf.logging.common.request.HttpHeaders.X_FORWARDED_FOR.getName();

	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private SyncKafkaTemplate kafkaTemplate;
	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private Tracing tracing;

	private WebTestClient webTestClient;
	private HttpHeaders headers;
	private HttpEntity<?> request;
	private ScopedSpan span;

	@BeforeEach
	public void setUp()
	{
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(CaasJwtToken.DEFAULT_TOKEN);
		headers.add(X_FORWARDED_FOR, TracingAssertions.IP_ADDRESSES);
		headers.add(CORRELATION_ID.getName(), TracingAssertions.CORRELATION_ID_VALUE);
		request = new HttpEntity<>(headers);

		webTestClient = WebTestClient.bindToServer().baseUrl(restTemplate.getRootUri()).build();

		span = tracing.tracer().startScopedSpan("test-span");
		LOG.info("Starting new span with correlation_id '{}'", TracingAssertions.CORRELATION_ID_VALUE);
	}

	@AfterEach
	public void tearDown()
	{
		span.finish();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void should_set_traces_in_MDC()
	{
		final Map<String, String> mdc = restTemplate.exchange("/tracing/mdc", GET, request, Map.class).getBody();

		assertThat(mdc.get(Fields.TENANT_ID), equalTo(TracingAssertions.TENANT_VALUE));
		assertThat(mdc.get(Fields.ORGANIZATION_ID), equalTo(TracingAssertions.ORGANIZATION_ID));
		assertThat(mdc.get(Fields.ORGANIZATION_NAME), equalTo(TracingAssertions.ORGANIZATION_NAME));
		assertThat(mdc.get(Fields.COMPONENT_TYPE), equalTo(TracingAssertions.COMPONENT_TYPE));
		assertThat(mdc.get(Fields.CORRELATION_ID), equalTo(TracingAssertions.CORRELATION_ID_VALUE));
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_send_traces_via_Async()
	{
		final List mdcs = restTemplate.exchange("/tracing/async", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> asyncMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, asyncMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_send_traces_via_RestTemplate()
	{
		final List mdcs = restTemplate.exchange("/tracing/http", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> remoteMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, remoteMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_send_traces_via_WebClient()
	{
		final Map<String, String> localMdc = new HashMap<>();
		final Map<String, String> remoteMdc = new HashMap<>();

		webTestClient.get()
				.uri("/tracing/web-client")
				.headers(httpHeaders -> request.getHeaders().toSingleValueMap().forEach(httpHeaders::add))
				.exchange()
				.expectStatus()
				.isOk()
				.expectBodyList(Map.class)
				.consumeWith(result -> {
					localMdc.putAll(result.getResponseBody().get(0));
					remoteMdc.putAll(result.getResponseBody().get(1));
				});

		assertMdcPropagation(localMdc, remoteMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_send_traces_via_RabbitTemplate()
	{
		final List mdcs = restTemplate.exchange("/tracing/rabbit", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> remoteMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, remoteMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_send_traces_via_Kafka()
	{
		final List mdcs = restTemplate.exchange("/tracing/kafka", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> remoteMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, remoteMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_set_traces_in_Runnable_by_wrapping()
	{
		final List mdcs = restTemplate.exchange("/tracing/wrap-runnable", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> runnableMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, runnableMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_set_traces_in_Runnable_with_TraceRunnable()
	{
		final List mdcs = restTemplate.exchange("/tracing/trace-runnable", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> runnableMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, runnableMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_set_traces_in_Callable_by_wrapping()
	{
		final List mdcs = restTemplate.exchange("/tracing/wrap-callable", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> callableMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, callableMdc);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void should_set_traces_in_Callable_by_with_TraceCallable()
	{
		final List mdcs = restTemplate.exchange("/tracing/trace-callable", GET, request, List.class).getBody();
		final Map<String, String> localMdc = (Map<String, String>) mdcs.get(0);
		final Map<String, String> callableMdc = (Map<String, String>) mdcs.get(1);

		assertMdcPropagation(localMdc, callableMdc);
	}

	private void assertMdcPropagation(Map<String, String> localMdc, Map<String, String> otherMdc)
	{
		Assert.assertNotNull(localMdc.get(Fields.ORGANIZATION_ID));
		Assert.assertNotNull(localMdc.get(Fields.ORGANIZATION_NAME));
		Assert.assertNotNull(localMdc.get(Fields.SPACE_ID));
		Assert.assertNotNull(localMdc.get(Fields.SPACE_NAME));
		Assert.assertNotNull(localMdc.get(Fields.CONTAINER_ID));
		Assert.assertNotNull(localMdc.get(Fields.COMPONENT_ID));
		Assert.assertNotNull(localMdc.get(Fields.COMPONENT_NAME));
		Assert.assertNotNull(localMdc.get(Fields.COMPONENT_INSTANCE));
		Assert.assertNotNull(localMdc.get(Fields.COMPONENT_TYPE));
		Assert.assertNotNull(localMdc.get(Fields.TENANT_ID));
		Assert.assertEquals(TracingAssertions.CORRELATION_ID_VALUE, localMdc.get(Fields.CORRELATION_ID));

		Assert.assertEquals(localMdc.get(Fields.ORGANIZATION_ID), otherMdc.get(Fields.ORGANIZATION_ID));
		Assert.assertEquals(localMdc.get(Fields.ORGANIZATION_NAME), otherMdc.get(Fields.ORGANIZATION_NAME));
		Assert.assertEquals(localMdc.get(Fields.SPACE_ID), otherMdc.get(Fields.SPACE_ID));
		Assert.assertEquals(localMdc.get(Fields.SPACE_NAME), otherMdc.get(Fields.SPACE_NAME));
		Assert.assertEquals(localMdc.get(Fields.CONTAINER_ID), otherMdc.get(Fields.CONTAINER_ID));
		Assert.assertEquals(localMdc.get(Fields.COMPONENT_ID), otherMdc.get(Fields.COMPONENT_ID));
		Assert.assertEquals(localMdc.get(Fields.COMPONENT_NAME), otherMdc.get(Fields.COMPONENT_NAME));
		Assert.assertEquals(localMdc.get(Fields.COMPONENT_INSTANCE), otherMdc.get(Fields.COMPONENT_INSTANCE));
		Assert.assertEquals(localMdc.get(Fields.COMPONENT_TYPE), otherMdc.get(Fields.COMPONENT_TYPE));
		Assert.assertEquals(localMdc.get(Fields.TENANT_ID), otherMdc.get(Fields.TENANT_ID));
		Assert.assertEquals(localMdc.get(Fields.CORRELATION_ID), otherMdc.get(Fields.CORRELATION_ID));
	}
}
