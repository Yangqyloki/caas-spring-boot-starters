package com.hybris.caas.test.integration.controller;

import brave.Tracing;
import com.hybris.caas.kafka.transaction.SyncKafkaTemplate;
import com.hybris.caas.test.integration.config.RabbitConfig;
import com.hybris.caas.test.integration.service.TracingService;
import com.hybris.caas.test.integration.util.TracingAssertions;
import com.hybris.caas.test.security.CaasJwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.instrument.async.TraceCallable;
import org.springframework.cloud.sleuth.instrument.async.TraceRunnable;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpMethod.GET;

@RestController
@RequestMapping(path = "/tracing")
public class TracingController
{
	private static final Logger LOG = LoggerFactory.getLogger("test-logger");
	public static BlockingQueue<Map<String, String>> queue = new ArrayBlockingQueue<>(1);

	private final TestRestTemplate restTemplate;
	private final WebClient webClient;
	private final Tracing tracing;
	private final SpanNamer spanNamer;
	private final TraceableExecutorService executorService;
	private final RabbitTemplate rabbitTemplate;
	private final SyncKafkaTemplate kafkaTemplate;
	private final KafkaTransactionManager kafkaTransactionManager;
	private final TracingService testService;

	public TracingController(final TestRestTemplate restTemplate, final WebClient webClient, final Tracing tracing,
			final SpanNamer spanNamer, final TraceableExecutorService executorService, final RabbitTemplate rabbitTemplate,
			final SyncKafkaTemplate kafkaTemplate, final KafkaTransactionManager kafkaTransactionManager,
			final TracingService testService)
	{
		this.restTemplate = restTemplate;
		this.webClient = webClient;
		this.tracing = tracing;
		this.spanNamer = spanNamer;
		this.executorService = executorService;
		this.rabbitTemplate = rabbitTemplate;
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaTransactionManager = kafkaTransactionManager;
		this.testService = testService;
	}

	@GetMapping("/mdc")
	public Map<String, String> getLocalMdc(@RequestHeader HttpHeaders headers)
	{
		LOG.info("GET /mdc");
		TracingAssertions.assertUserTraceContext(tracing);
		return MDC.getCopyOfContextMap();
	}

	@GetMapping("/async")
	public List<Map<String, String>> getAsyncMdc(@RequestHeader HttpHeaders headers) throws InterruptedException
	{
		LOG.info("GET /async");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();
		testService.execute();
		return Arrays.asList(localMdc, queue.poll(2, TimeUnit.SECONDS));
	}

	@GetMapping("/remote-mdc")
	public Map<String, String> getRemoteMdc(@RequestHeader HttpHeaders headers)
	{
		LOG.info("GET /remote-mdc");
		TracingAssertions.assertUserTraceContext(tracing);
		TracingAssertions.assertSpanParentId(tracing);
		return MDC.getCopyOfContextMap();
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/http")
	public List<Map<String, String>> getRemoteMdc(final HttpServletRequest servletRequest)
	{
		LOG.info("GET /http");
		TracingAssertions.assertUserTraceContext(tracing);
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + CaasJwtToken.DEFAULT_TOKEN);
		final HttpEntity<?> request = new HttpEntity<>(headers);
		final Map<String, String> remoteMdc = restTemplate.exchange("/tracing/remote-mdc", GET, request, Map.class).getBody();
		return Arrays.asList(localMdc, remoteMdc);
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/web-client")
	public List<Map<String, String>> getWebClientBasedRemoteMdc(final HttpServletRequest servletRequest)
	{
		LOG.info("GET /web-client");
		TracingAssertions.assertUserTraceContext(tracing);
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final Map<String, String> remoteMdc = webClient.get()
				.uri(this.restTemplate.getRootUri() + "/tracing/remote-mdc")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + CaasJwtToken.DEFAULT_TOKEN)
				.retrieve()
				.bodyToMono(Map.class)
				.block();

		return Arrays.asList(localMdc, remoteMdc);
	}

	@GetMapping("/rabbit")
	public List<Map<String, String>> getRabbitMdc() throws InterruptedException
	{
		LOG.info("GET /rabbit");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		rabbitTemplate.convertAndSend(RabbitConfig.topicExchangeName, RabbitConfig.routingKey, "Some useless message.");
		return Arrays.asList(localMdc, queue.poll(2, TimeUnit.SECONDS));
	}

	@GetMapping("/kafka")
	public List<Map<String, String>> getKafkaMdc() throws InterruptedException
	{
		LOG.info("GET /kafka");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final Message<String> message = MessageBuilder.withPayload("Some useless message.")
				.setHeader(KafkaHeaders.TOPIC, "my-topic")
				.build();

		new TransactionTemplate(kafkaTransactionManager).execute(s -> {
			kafkaTemplate.send(message);
			return null;
		});

		final Map<String, String> remoteMdc = queue.poll(2, TimeUnit.SECONDS);
		return Arrays.asList(localMdc, remoteMdc);
	}

	@GetMapping("/wrap-runnable")
	public List<Map<String, String>> getRunnableMdcByWrapping() throws InterruptedException, ExecutionException
	{
		LOG.info("GET /runnable");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final Runnable runnable = () -> {
			LOG.info("In Runnable");
			TracingAssertions.assertUserTraceContext(tracing);
			queue.offer(MDC.getCopyOfContextMap());
			return;
		};

		executorService.submit(tracing.currentTraceContext().wrap(runnable)).get();
		return Arrays.asList(localMdc, queue.poll(2, TimeUnit.SECONDS));
	}

	@GetMapping("/trace-runnable")
	public List<Map<String, String>> getRunnableMdcWithTraceRunnable() throws InterruptedException, ExecutionException
	{
		LOG.info("GET /runnable");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final Runnable runnable = () -> {
			LOG.info("In Runnable");
			TracingAssertions.assertUserTraceContext(tracing);
			queue.offer(MDC.getCopyOfContextMap());
			return;
		};

		final TraceRunnable traceRunnable = new TraceRunnable(tracing, spanNamer, runnable, "runnableSpan");
		executorService.submit(traceRunnable).get();
		return Arrays.asList(localMdc, queue.poll(2, TimeUnit.SECONDS));

	}

	@GetMapping("/wrap-callable")
	public List<Map<String, String>> getCallableMdcByWrapping() throws Exception
	{
		LOG.info("GET /callable");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final Callable<Void> callable = () -> {
			LOG.info("In Callable");
			TracingAssertions.assertUserTraceContext(tracing);
			queue.offer(MDC.getCopyOfContextMap());
			return null;
		};

		executorService.submit(tracing.currentTraceContext().wrap(callable));
		return Arrays.asList(localMdc, queue.poll(2, TimeUnit.SECONDS));
	}

	@GetMapping("/trace-callable")
	public List<Map<String, String>> getCallableMdcWithTraceCallable() throws Exception
	{
		LOG.info("GET /callable");
		final Map<String, String> localMdc = MDC.getCopyOfContextMap();

		final Callable<Void> callable = () -> {
			LOG.info("In Callable");
			TracingAssertions.assertUserTraceContext(tracing);
			queue.offer(MDC.getCopyOfContextMap());
			return null;
		};
		final TraceCallable<Void> traceCallable = new TraceCallable<>(tracing, spanNamer, callable, "calableSpan");

		executorService.submit(traceCallable);
		return Arrays.asList(localMdc, queue.poll(2, TimeUnit.SECONDS));
	}

}
