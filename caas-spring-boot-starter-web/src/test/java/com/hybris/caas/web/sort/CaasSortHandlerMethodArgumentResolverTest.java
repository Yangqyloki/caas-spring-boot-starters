package com.hybris.caas.web.sort;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaasSortHandlerMethodArgumentResolverTest
{
	private static final String[] VALID_ATTRIBUTES = { "foo", "bar" };
	private static final String[] COLUMN = { "foo_column", "bar" };
	private static final String[] MORE_COLUMN_PROVIDED = { "foo_column", "bar", "baz" };
	private static final String[] LESS_COLUMN_PROVIDED = { "foo_column" };

	private CaasSortHandlerMethodArgumentResolver argumentResolver;

	@Mock
	private MethodParameter methodParameter;
	@Mock
	private ModelAndViewContainer modelAndViewContainer;
	@Mock
	private NativeWebRequest nativeWebRequest;
	@Mock
	private WebDataBinderFactory webDataBinderFactory;
	@Mock
	private SortProperties sortParam;

	@Before
	public void setup()
	{
		final Sort fallbackSort = Sort.unsorted();
		argumentResolver = new CaasSortHandlerMethodArgumentResolver();
		argumentResolver.setFallbackSort(fallbackSort);
		argumentResolver.setSortParameter("sort");
		argumentResolver.setPropertyDelimiter(":");

		when(methodParameter.getParameterAnnotation(SortProperties.class)).thenReturn(null);
		when(sortParam.value()).thenReturn(VALID_ATTRIBUTES);
	}

	@Test
	public void should_resolve_argument_UNSORTED_when_sort_parameter_is_null()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn(null);
		final Object sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort, equalTo(Sort.unsorted()));
	}

	@Test
	public void should_resolve_argument_UNSORTED_when_sort_parameter_is_empty()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn("");
		final Object sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort, equalTo(Sort.unsorted()));
	}

	@Test
	public void should_resolve_argument_null_when_only_invalid_directions()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo:invalid,bar:invalid");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort.getOrderFor("foo").getDirection(), equalTo(Direction.ASC));
		assertThat(sort.getOrderFor("bar").getDirection(), equalTo(Direction.ASC));
	}

	@Test
	public void should_resolve_argument_some_invalid_attribute_and_direction()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo:asc,bar:invalid,baz:invalid");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort.getOrderFor("foo").getDirection(), equalTo(Direction.ASC));
		assertThat(sort.getOrderFor("bar").getDirection(), equalTo(Direction.ASC));
		assertThat(sort.getOrderFor("baz").getDirection(), equalTo(Direction.ASC));
	}

	@Test
	public void should_resolve_argument_missing_direction_as_ASC()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort.getOrderFor("foo").getDirection(), equalTo(Direction.ASC));
	}

	@Test
	public void should_resolve_argument_valid_attributes()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo,bar,baz");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort.getOrderFor("foo").getProperty(), equalTo("foo"));
		assertThat(sort.getOrderFor("bar").getProperty(), equalTo("bar"));
		assertThat(sort.getOrderFor("baz").getProperty(), equalTo("baz"));
	}

	@Test
	public void should_resolve_argument_valid_directions_all_cases()
	{
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo:desc,bar:DESC,baz:deSC");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort.getOrderFor("foo").getDirection(), equalTo(Direction.DESC));
		assertThat(sort.getOrderFor("bar").getDirection(), equalTo(Direction.DESC));
		assertThat(sort.getOrderFor("baz").getDirection(), equalTo(Direction.DESC));
	}

	@Test
	public void should_filter_out_invalid_properties()
	{
		when(methodParameter.getParameterAnnotation(SortProperties.class)).thenReturn(sortParam);
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo,bar,baz");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort.getOrderFor("foo"), notNullValue());
		assertThat(sort.getOrderFor("bar"), notNullValue());
		assertThat(sort.getOrderFor("baz"), nullValue());
	}

	@Test
	public void should_filter_out_all_properties()
	{
		when(methodParameter.getParameterAnnotation(SortProperties.class)).thenReturn(sortParam);
		when(nativeWebRequest.getParameter("sort")).thenReturn("invalid-1,invalid-2");
		final Object sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);
		assertThat(sort, equalTo(Sort.unsorted()));
	}

	@Test
	public void should_sort_by_column_name()
	{
		when(sortParam.column()).thenReturn(COLUMN);
		when(methodParameter.getParameterAnnotation(SortProperties.class)).thenReturn(sortParam);
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo,bar");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);

		assertThat(sort.getOrderFor("foo"), nullValue());
		assertThat(sort.getOrderFor("foo_column"), notNullValue());
		assertThat(sort.getOrderFor("bar"), notNullValue());
	}

	@Test
	public void should_sort_by_column_name_more_column_provided_than_values()
	{
		when(sortParam.column()).thenReturn(MORE_COLUMN_PROVIDED);
		when(methodParameter.getParameterAnnotation(SortProperties.class)).thenReturn(sortParam);
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo,bar");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);

		assertThat(sort.getOrderFor("foo_column"), notNullValue());
		assertThat(sort.getOrderFor("bar"), notNullValue());
		assertThat(sort.getOrderFor("baz"), nullValue());
	}

	@Test
	public void should_sort_by_column_name_less_column_provided_than_values()
	{
		when(sortParam.column()).thenReturn(LESS_COLUMN_PROVIDED);
		when(methodParameter.getParameterAnnotation(SortProperties.class)).thenReturn(sortParam);
		when(nativeWebRequest.getParameter("sort")).thenReturn("foo,bar");
		final Sort sort = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);

		assertThat(sort.getOrderFor("foo_column"), notNullValue());
		assertThat(sort.getOrderFor("bar"), notNullValue());
	}
}
