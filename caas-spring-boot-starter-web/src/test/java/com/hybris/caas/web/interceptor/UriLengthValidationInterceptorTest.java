package com.hybris.caas.web.interceptor;

import com.hybris.caas.web.exception.UriLengthValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UriLengthValidationInterceptorTest
{
	private static final String DUMMY = "dummy";
	private static final String TEST_METHOD_NAME = "testMethod";
	private static final String ANNOTATED_TEST_METHOD_NAME = "annotatedTestMethod";

	private UriLengthValidationInterceptor uriLengthValidationInterceptor;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	private HandlerMethod handlerMethod;

	@Before
	public void setUp() throws NoSuchMethodException
	{
		uriLengthValidationInterceptor = new UriLengthValidationInterceptor(11); // 11 = length(dummy?dummy)
		handlerMethod = new HandlerMethod(new MyHandlerMethod(), ANNOTATED_TEST_METHOD_NAME, String.class);

		when(request.getRequestURL()).thenReturn(new StringBuffer(DUMMY));
		when(request.getQueryString()).thenReturn(null);
	}

	@Test
	public void should_handle_request_with_uri_length_less_than_max_uri_length_allowed() throws Exception
	{
		final boolean proceed = uriLengthValidationInterceptor.preHandle(request, response, handlerMethod);

		assertThat(proceed, is(Boolean.TRUE));
	}

	@Test
	public void should_handle_request_with_uri_and_query_string_length_equal_to_max_uri_length_allowed() throws Exception
	{
		when(request.getQueryString()).thenReturn(DUMMY);

		final boolean proceed = uriLengthValidationInterceptor.preHandle(request, response, handlerMethod);

		assertThat(proceed, is(Boolean.TRUE));
	}

	@Test(expected = UriLengthValidationException.class)
	public void should_fail_request_with_uri_length_greater_than_max_uri_length_allowed() throws Exception
	{
		when(request.getQueryString()).thenReturn(DUMMY + "1");

		uriLengthValidationInterceptor.preHandle(request, response, handlerMethod);
	}

	@Test
	public void should_proceed_when_non_handler_method_object_is_provided_as_handler() throws Exception
	{
		final boolean proceed = uriLengthValidationInterceptor.preHandle(request, response, new Object());

		verifyNoInteractions(request);
		assertThat(proceed, is(Boolean.TRUE));
	}

	@Test
	public void should_proceed_when_handler_method_does_not_check_uri_length() throws Exception
	{
		final boolean proceed = uriLengthValidationInterceptor.preHandle(request, response,
				new HandlerMethod(new MyHandlerMethod(), TEST_METHOD_NAME, String.class));

		verifyNoInteractions(request);
		assertThat(proceed, is(Boolean.TRUE));
	}

	@Test
	public void should_set_default_max_uri_length_for_zero_max_uri_length()
	{
		final UriLengthValidationInterceptor interceptor = new UriLengthValidationInterceptor(0);

		assertThat(interceptor.getMaxUriLength(), is(UriLengthValidationInterceptor.DEFAULT_MAX_URI_LENGTH));
	}

	@Test
	public void should_set_default_max_uri_length_for_negative_max_uri_length()
	{
		final UriLengthValidationInterceptor interceptor = new UriLengthValidationInterceptor(-1);

		assertThat(interceptor.getMaxUriLength(), is(UriLengthValidationInterceptor.DEFAULT_MAX_URI_LENGTH));
	}

	@Test
	public void should_handle_request_with_annotated_max_uri_length() throws Exception
	{
		when(request.getQueryString()).thenReturn(DUMMY);
		handlerMethod = new HandlerMethod(new MyHandlerMethodWithValue(), ANNOTATED_TEST_METHOD_NAME, String.class);

		final boolean proceed = uriLengthValidationInterceptor.preHandle(request, response, handlerMethod);

		assertThat(proceed, is(Boolean.TRUE));
	}

	@Test(expected = UriLengthValidationException.class)
	public void should_throw_UriLengthValidationException_when_annotated_max_uri_length_lower_than_uri_provided() throws Exception
	{
		when(request.getQueryString()).thenReturn(DUMMY + DUMMY);
		handlerMethod = new HandlerMethod(new MyHandlerMethodWithValue(), ANNOTATED_TEST_METHOD_NAME, String.class);

		uriLengthValidationInterceptor.preHandle(request, response, handlerMethod);
	}

	@Test
	public void should_set_environment_variable_when_annotated_is_negative()
	{
		final UriLengthValidationInterceptor interceptor = new UriLengthValidationInterceptor(25);

		assertThat(interceptor.getMaxUriLength(), is(25));
	}

	private static class MyHandlerMethod
	{
		@UriLengthValidation
		public void annotatedTestMethod(final String dummy)
		{
			// empty
		}

		@SuppressWarnings("unused")
		public void testMethod(final String dummy)
		{
			// empty
		}
	}

	private static class MyHandlerMethodWithValue
	{
		@UriLengthValidation(maxLength = 12)
		public void annotatedTestMethod(final String dummy)
		{
			// empty
		}

		@SuppressWarnings("unused")
		public void testMethod(final String dummy)
		{
			// empty
		}
	}
}
