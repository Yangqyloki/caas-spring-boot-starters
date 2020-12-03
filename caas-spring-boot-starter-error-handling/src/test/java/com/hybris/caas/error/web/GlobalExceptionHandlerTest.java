package com.hybris.caas.error.web;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.converter.ExceptionConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerTest
{
	private final static Logger ROOT_LOGGER = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	public static final String TEST_HEADER = "test-header";
	public static final String TEST_HEADER_VALUE = "test-value";

	private GlobalExceptionHandler exceptionHandler;

	@Mock
	private Appender mockAppender;
	@Mock
	private ExceptionConverter exceptionConverter;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private FilterChain filterChain;
	@Mock
	private HttpEntityMethodProcessor httpEntityMethodProcessor;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
	@Captor
	private ArgumentCaptor<ResponseEntity<ErrorMessage>> captorResponseEntity;

	private final static HttpHeaders httpHeaders = new HttpHeaders();
	{
		httpHeaders.add(TEST_HEADER, TEST_HEADER_VALUE);
	}

	private final static ErrorMessage message500 = ErrorMessage.builder()
			.withStatus(500)
			.withType("Internal Server Error")
			.withMessage("Test Message")
			.withResponseHeaders(httpHeaders)
			.build();

	private final static ErrorMessage message404 = ErrorMessage.builder()
			.withStatus(404)
			.withType("Not Found")
			.withMessage("Test Message")
			.withResponseHeaders(httpHeaders)
			.build();

	private final static ErrorMessage message406 = ErrorMessage.builder()
			.withStatus(406)
			.withType("Unsupported Content-Type")
			.withMessage("Test Message")
			.withResponseHeaders(httpHeaders)
			.build();

	@Before
	public void setup() throws IOException
	{
		exceptionHandler = new GlobalExceptionHandler(exceptionConverter);
		exceptionHandler.setHttpEntityMethodProcessor(httpEntityMethodProcessor);

		ROOT_LOGGER.addAppender(mockAppender);
	}

	@After
	public void teardown()
	{
		ROOT_LOGGER.detachAppender(mockAppender);
	}

	@Test
	public void should_handle_HttpMediaTypeNotAcceptableException()
	{
		final HttpMediaTypeNotAcceptableException exception = new HttpMediaTypeNotAcceptableException(Arrays.asList(MediaType.APPLICATION_JSON));
		when(exceptionConverter.toErrorMessage(exception)).thenReturn(message406);

		final ResponseEntity<String> responseEntity = exceptionHandler.handleHttpMediaTypeNotAcceptableException(exception);

		// assert ResponseEntity conversion
		verify(exceptionConverter).toErrorMessage(exception);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_ACCEPTABLE.value()));
		assertThat(responseEntity.getHeaders().getFirst(TEST_HEADER), is(TEST_HEADER_VALUE));
		assertThat(responseEntity.getBody(), nullValue());

		// assert logging
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertThat(loggingEvent.getFormattedMessage(),
				is("\n*** 4xx exception occurred: \n{\n  \"status\" : 406,\n  \"type\" : \"Unsupported Content-Type\",\n  \"message\""
						+ " : \"Test Message\",\n  \"details\" : [ ]\n}\n"));
		assertThat(loggingEvent.getThrowableProxy().getMessage(), is("Could not find acceptable representation"));
	}

	@Test
	public void should_handle_controller_advice_Throwable_4xx()
	{
		final Throwable exception = new RuntimeException("4xx Test exception message");
		when(exceptionConverter.toErrorMessage(exception)).thenReturn(message404);

		final ResponseEntity<ErrorMessage> responseEntity = exceptionHandler.handleThrowable(exception);

		// assert ResponseEntity conversion
		verify(exceptionConverter).toErrorMessage(exception);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getHeaders().getFirst(TEST_HEADER), is(TEST_HEADER_VALUE));
		assertThat(responseEntity.getBody(), is(message404));

		// assert logging
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertThat(loggingEvent.getFormattedMessage(),
				is("\n*** 4xx exception occurred: \n{\n  \"status\" : 404,\n  \"type\" : \"Not Found\",\n  \"message\""
						+ " : \"Test Message\",\n  \"details\" : [ ]\n}\n"));
		assertThat(loggingEvent.getThrowableProxy().getMessage(), is("4xx Test exception message"));
	}

	@Test
	public void should_handle_controller_advice_Throwable_5xx()
	{
		final Throwable exception = new RuntimeException("5xx Test exception message");
		when(exceptionConverter.toErrorMessage(exception)).thenReturn(message500);

		final ResponseEntity<ErrorMessage> responseEntity = exceptionHandler.handleThrowable(exception);

		// assert ResponseEntity conversion
		verify(exceptionConverter).toErrorMessage(exception);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		assertThat(responseEntity.getHeaders().getFirst(TEST_HEADER), is(TEST_HEADER_VALUE));
		assertThat(responseEntity.getBody(), is(message500));

		// assert logging
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.ERROR));
		assertThat(loggingEvent.getFormattedMessage(),
				is("\n*** 5xx exception occurred: \n{\n  \"status\" : 500,\n  \"type\" : \"Internal Server Error\",\n  \"message\" :"
						+ " \"Test Message\",\n  \"details\" : [ ]\n}\n"));
		assertThat(loggingEvent.getThrowableProxy().getMessage(), is("5xx Test exception message"));
	}

	@Test
	public void should_handle_filter_Throwable_4xx() throws Exception
	{
		final Throwable exception = new RuntimeException("4xx Test exception message");
		when(exceptionConverter.toErrorMessage(exception)).thenReturn(message404);
		doThrow(exception).when(filterChain).doFilter(request, response);

		exceptionHandler.doFilterInternal(request, response, filterChain);

		// assert ResponseEntity conversion
		verify(exceptionConverter).toErrorMessage(exception);
		verify(httpEntityMethodProcessor).handleReturnValue(captorResponseEntity.capture(), Mockito.any(MethodParameter.class),
				Mockito.any(ModelAndViewContainer.class), Mockito.any(NativeWebRequest.class));

		final ResponseEntity<ErrorMessage> responseEntity = captorResponseEntity.getValue();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.NOT_FOUND.value()));
		assertThat(responseEntity.getHeaders().getFirst(TEST_HEADER), is(TEST_HEADER_VALUE));
		assertThat(responseEntity.getBody(), is(message404));

		// assert logging
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.INFO));
		assertThat(loggingEvent.getFormattedMessage(),
				is("\n*** 4xx exception occurred: \n{\n  \"status\" : 404,\n  \"type\" : \"Not Found\",\n  \"message\""
						+ " : \"Test Message\",\n  \"details\" : [ ]\n}\n"));
		assertThat(loggingEvent.getThrowableProxy().getMessage(), is("4xx Test exception message"));
	}

	@Test
	public void should_handle_application_exception_for_status_more_than_500() throws Exception
	{
		final Throwable exception = new RuntimeException("5xx Test exception message");
		doThrow(exception).when(filterChain).doFilter(request, response);
		when(exceptionConverter.toErrorMessage(exception)).thenReturn(message500);

		exceptionHandler.doFilterInternal(request, response, filterChain);

		// assert ResponseEntity conversion
		verify(exceptionConverter).toErrorMessage(exception);
		verify(httpEntityMethodProcessor).handleReturnValue(captorResponseEntity.capture(), Mockito.any(MethodParameter.class),
				Mockito.any(ModelAndViewContainer.class), Mockito.any(NativeWebRequest.class));

		final ResponseEntity<ErrorMessage> responseEntity = captorResponseEntity.getValue();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
		assertThat(responseEntity.getStatusCodeValue(), is(HttpStatus.INTERNAL_SERVER_ERROR.value()));
		assertThat(responseEntity.getHeaders().getFirst(TEST_HEADER), is(TEST_HEADER_VALUE));
		assertThat(responseEntity.getBody(), is(message500));

		// assert logging
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertThat(loggingEvent.getLevel(), is(Level.ERROR));
		assertThat(loggingEvent.getFormattedMessage(),
				is("\n*** 5xx exception occurred: \n{\n  \"status\" : 500,\n  \"type\" : \"Internal Server Error\",\n  \"message\" :"
						+ " \"Test Message\",\n  \"details\" : [ ]\n}\n"));
		assertThat(loggingEvent.getThrowableProxy().getMessage(), is("5xx Test exception message"));
	}

}
