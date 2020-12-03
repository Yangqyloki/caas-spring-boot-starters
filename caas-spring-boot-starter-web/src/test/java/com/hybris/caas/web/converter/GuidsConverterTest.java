package com.hybris.caas.web.converter;

import com.hybris.caas.web.annotation.Guids;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.TypeDescriptor;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GuidsConverterTest
{
	private GuidsConverter guidsConverter;

	@Mock
	private TypeDescriptor sourceType;

	@Mock
	private TypeDescriptor targetType;

	@Mock
	private Guids guids;

	@Before
	public void setup()
	{
		guidsConverter = Mockito.spy(new GuidsConverter());
	}

	@Test
	public void should_fail_when_annotation_not_present()
	{
		List<String> test = new ArrayList<>();
		test.add("GUID");
		test.add("GUIDS");

		when(targetType.getAnnotation(Guids.class)).thenReturn(null);
		final Object object = guidsConverter.convert(test, sourceType, targetType);

		assertThat((List<String>)object, equalTo(test));
	}

	@Test
	public void should_succeed_when_annotation_is_present()
	{
		List<String> test = new ArrayList<>();
		test.add("GUID");
		test.add("GUIDS");

		List<String> result = new ArrayList<>();
		result.add("guid");
		result.add("guids");

		when(targetType.getAnnotation(Guids.class)).thenReturn(guids);
		final Object object = guidsConverter.convert(test, sourceType, targetType);

		assertThat((List<String>)object, equalTo(result));
	}
}
