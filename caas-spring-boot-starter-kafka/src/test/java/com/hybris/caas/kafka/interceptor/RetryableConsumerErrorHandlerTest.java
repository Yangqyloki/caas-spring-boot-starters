package com.hybris.caas.kafka.interceptor;

import com.hybris.caas.kafka.error.RetryableConsumerDeadLetterPublishingRecoverer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(MockitoJUnitRunner.class)
public class RetryableConsumerErrorHandlerTest
{
	private static final String DUMMY_TOPIC_NAME = "dummy";
	private static final int PARTITION = 10;
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final long RECORD_OFFSET = 15L;

	@Mock
	private Consumer<?, ?> consumer;
	@Mock
	private MessageListenerContainer listenerContainer;
	@Mock
	private RetryableConsumerDeadLetterPublishingRecoverer retryableConsumerDeadLetterPublishingRecoverer;
	@Captor
	private ArgumentCaptor<TopicPartition> topicPartitionArgumentCaptor;
	@Captor
	private ArgumentCaptor<Long> offsetArgumentCaptor;

	private ConsumerRecord<?, ?> consumerRecord = new ConsumerRecord<>(DUMMY_TOPIC_NAME, PARTITION, RECORD_OFFSET, KEY, VALUE);
	private List<ConsumerRecord<?, ?>> consumerRecordList = Collections.singletonList(consumerRecord);
	private Exception exception = new Exception("must have a message for DLT headers to be set!");
	private RetryableConsumerErrorHandler retryableConsumerErrorHandler;

	@Before
	public void setUp()
	{
		retryableConsumerErrorHandler = new RetryableConsumerErrorHandler(retryableConsumerDeadLetterPublishingRecoverer);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void should_not_support_non_container_aware_handle_method()
	{
		retryableConsumerErrorHandler.handle(new Exception(), Collections.emptyList(),
				new MockConsumer<>(OffsetResetStrategy.EARLIEST));
	}

	@Test
	public void should_further_propagate_DelayException()
	{
		final List<ConsumerRecord<?, ?>> recordList = new ArrayList<>();
		recordList.add(consumerRecord);
		recordList.add(new ConsumerRecord<>("another-topic", 8, 3, KEY, VALUE));

		try
		{
			retryableConsumerErrorHandler.handle(new ListenerExecutionFailedException("dummy", new DelayException(5)), recordList,
					consumer, listenerContainer);
			fail();
		}
		catch (final DelayException ex)
		{
			verify(consumer, times(2)).seek(topicPartitionArgumentCaptor.capture(), offsetArgumentCaptor.capture());

			final List<TopicPartition> topicPartitions = topicPartitionArgumentCaptor.getAllValues();
			final List<Long> offsets = offsetArgumentCaptor.getAllValues();

			assertThat(topicPartitions.get(0).topic(), equalTo(DUMMY_TOPIC_NAME));
			assertThat(topicPartitions.get(0).partition(), equalTo(PARTITION));
			assertThat(offsets.get(0), equalTo(RECORD_OFFSET));

			assertThat(topicPartitions.get(1).topic(), equalTo("another-topic"));
			assertThat(topicPartitions.get(1).partition(), equalTo(8));
			assertThat(offsets.get(1), equalTo(3L));
		}
		catch (final Exception ex)
		{
			fail();
		}
	}

	@Test
	public void should_throw_IllegalStateException_when_no_consumer_records_are_provided()
	{
		try
		{
			retryableConsumerErrorHandler.handle(exception, Collections.emptyList(), consumer, listenerContainer);
			fail();
		}
		catch (final IllegalStateException ex)
		{
			assertThat(ex.getMessage(), containsString("no record information is available"));
			verifyNoInteractions(consumer);
			verifyNoInteractions(retryableConsumerDeadLetterPublishingRecoverer);
		}
		catch (final Exception ex)
		{
			fail();
		}
	}

	@Test
	public void should_publish_record_to_short_delay_retry_topic()
	{
		retryableConsumerErrorHandler.handle(exception, consumerRecordList, consumer, listenerContainer);

		verifyNoInteractions(consumer);
		verify(retryableConsumerDeadLetterPublishingRecoverer).accept(eq(consumerRecord), eq(exception));
	}

	@Test
	public void should_publish_record_to_short_delay_retry_topic_and_seek_unprocessed_record()
	{
		final List<ConsumerRecord<?, ?>> recordList = new ArrayList<>();
		recordList.add(consumerRecord);
		recordList.add(new ConsumerRecord<>("another-topic", 8, 3, KEY, VALUE));

		retryableConsumerErrorHandler.handle(exception, recordList, consumer, listenerContainer);

		verify(retryableConsumerDeadLetterPublishingRecoverer).accept(eq(consumerRecord), eq(exception));

		// check seek is performed for the unconsumed message
		verify(consumer).seek(topicPartitionArgumentCaptor.capture(), eq(3L));
		assertThat(topicPartitionArgumentCaptor.getValue().topic(), equalTo("another-topic"));
		assertThat(topicPartitionArgumentCaptor.getValue().partition(), equalTo(8));
	}
}
