package com.hybris.caas.web.converter;

import com.hybris.caas.web.annotation.Guid;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GuidConverterTest
{
	private GuidConverter guidConverter;

	@Mock
	private TypeDescriptor sourceType;

	@Mock
	private TypeDescriptor targetType;

	@Mock
	private Guid guid;

	@Before
	public void setup()
	{
		guidConverter = Mockito.spy(new GuidConverter());
	}

	@Test
	public void should_fail_when_annotation_not_present()
	{
		when(targetType.getAnnotation(Guid.class)).thenReturn(null);
		final Object object = guidConverter.convert("GUID", sourceType, targetType);

		assertThat(object.toString(), equalTo("GUID"));
	}

	@Test
	public void should_succeed_when_annotation_is_present()
	{
		when(targetType.getAnnotation(Guid.class)).thenReturn(guid);
		final Object object = guidConverter.convert("GUID", sourceType, targetType);

		assertThat(object.toString(), equalTo("guid"));
	}
}
