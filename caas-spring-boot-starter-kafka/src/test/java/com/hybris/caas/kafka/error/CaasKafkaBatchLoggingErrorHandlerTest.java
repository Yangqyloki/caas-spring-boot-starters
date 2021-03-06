package com.hybris.caas.kafka.error;

import com.hybris.caas.error.converter.DefaultExceptionConverter;
import com.hybris.caas.error.converter.ExceptionConverterFactory;
import com.hybris.caas.error.converter.javax.ConstraintViolationExceptionConverter;
import com.hybris.caas.kafka.util.CaasKafkaHeaders;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(OutputCaptureExtension.class)
public class CaasKafkaBatchLoggingErrorHandlerTest
{
	private ExceptionConverterFactory exceptionConverter;
	private CaasKafkaBatchLoggingErrorHandler caasKafkaBatchLoggingErrorHandler;
	private ConsumerRecords records;

	@BeforeEach
	public void setup()
	{
		this.exceptionConverter = new ExceptionConverterFactory(new DefaultExceptionConverter());
		this.exceptionConverter.setAbstractConverters(Set.of(new ConstraintViolationExceptionConverter()));
		this.exceptionConverter.postConstruct();
		this.caasKafkaBatchLoggingErrorHandler = new CaasKafkaBatchLoggingErrorHandler(exceptionConverter);

		this.records = new ConsumerRecords(Map.of(new TopicPartition("dummy-topic", 0),
				List.of(new ConsumerRecord("dummy-topic", 0, 0, System.currentTimeMillis(), TimestampType.CREATE_TIME, 0L, 10, 10,
						"01", "dummy message",
						new RecordHeaders(Set.of(new RecordHeader(CaasKafkaHeaders.TENANT, "dummy-tenant".getBytes())))))));

	}

	@Test
	public void should_log_when_record_is_null(final CapturedOutput output)
	{
		caasKafkaBatchLoggingErrorHandler.handle(new ConstraintViolationException("dummy", Set.of()), null);
		Assertions.assertTrue(output.getOut().contains("Failed to process the message. The message is null"));
	}

	@Test
	public void should_log_when_exception_cause_is_null(final CapturedOutput output)
	{

		caasKafkaBatchLoggingErrorHandler.handle(new ConstraintViolationException("dummy", Set.of()), records);
		Assertions.assertTrue(output.getOut()
				.contains(
						"Failed to process the message: topic: dummy-topic, headers: {tenant=dummy-tenant}, payload: dummy message"));
	}

	@Test
	public void should_log_when_exception_cause_is_not_null(final CapturedOutput output)
	{
		caasKafkaBatchLoggingErrorHandler.handle(new RuntimeException(new ConstraintViolationException("dummy", Set.of())), records);
		Assertions.assertTrue(output.getOut()
				.contains(
						"Failed to process the message: topic: dummy-topic, headers: {tenant=dummy-tenant}, payload: dummy message"));
	}
}