package com.hybris.caas.web.pagination;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaasPageableHandlerMethodArgumentResolverTest
{
	private CaasPageableHandlerMethodArgumentResolver argumentResolver;

	@Mock
	private MethodParameter methodParameter;
	@Mock
	private ModelAndViewContainer modelAndViewContainer;
	@Mock
	private NativeWebRequest nativeWebRequest;
	@Mock
	private WebDataBinderFactory webDataBinderFactory;
	@Mock
	private MaxPageSize maxPageSize;
	@Mock
	private SortArgumentResolver sortArgumentResolver;

	private Pageable pageableFromSuper;

	@Before
	public void setup()
	{
		pageableFromSuper = PageRequest.of(0, 1000);
		argumentResolver = Mockito.spy(new CaasPageableHandlerMethodArgumentResolver(sortArgumentResolver));
		doReturn(pageableFromSuper).when(argumentResolver).resolveFromSuper(any(), any(), any(), any());
		when(maxPageSize.value()).thenReturn(50);
	}

	@Test
	public void should_skip_max_size_when_not_present()
	{
		when(methodParameter.getParameterAnnotation(MaxPageSize.class)).thenReturn(null);
		final Pageable pageable = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);

		assertThat(pageable.getPageSize(), equalTo(1000));
	}

	@Test
	public void should_enforce_max_size_when_present()
	{
		when(methodParameter.getParameterAnnotation(MaxPageSize.class)).thenReturn(maxPageSize);
		final Pageable pageable = argumentResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest,
				webDataBinderFactory);

		assertThat(pageable.getPageSize(), equalTo(50));
	}


}
