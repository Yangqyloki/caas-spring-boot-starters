package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.exception.UpdateConflictException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.hybris.caas.error.converter.custom.UpdateConflictExceptionConverter.DETAIL_MESSAGE;
import static com.hybris.caas.error.converter.custom.UpdateConflictExceptionConverter.MESSAGE;
import static com.hybris.caas.error.converter.custom.UpdateConflictExceptionConverter.UNMODIFIABLE_FIELD;
import static com.hybris.caas.error.ErrorConstants.INFO;
import static com.hybris.caas.error.ErrorConstants.TYPE_409_CONFLICT_RESOURCE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpStatus.CONFLICT;

public class UpdateConflictExceptionConverterTest
{
	private final UpdateConflictExceptionConverter converter = new UpdateConflictExceptionConverter();
	private UpdateConflictException exception;
	private Map<String, Object[]> conflicts;

	@Before
	public void setUp()
	{
		conflicts = new HashMap<>();
	}

	@Test
	public void should_convert_multiple_conflicts()
	{
		conflicts.put("foo", new Object[] {"hello", "world"});
		conflicts.put("bar", new Object[] {"sap", "hybris"});
		exception = new UpdateConflictException(conflicts);

		final ErrorMessage error = converter.toErrorMessage(exception);
		assertThat(error.getMessage(), equalTo(MESSAGE));
		assertThat(error.getType(), equalTo(TYPE_409_CONFLICT_RESOURCE));
		assertThat(error.getStatus(), equalTo(CONFLICT.value()));
		assertThat(error.getMoreInfo(), equalTo(INFO));

		final ErrorMessageDetail detail1 = error.getDetails().stream().filter(detail -> detail.getField().equals("foo")).findFirst().get();
		assertThat(detail1.getMessage(), equalTo(String.format(DETAIL_MESSAGE, "hello", "world")));
		assertThat(detail1.getType(), equalTo(UNMODIFIABLE_FIELD));
		assertThat(detail1.getField(), equalTo("foo"));

		final ErrorMessageDetail detail2 = error.getDetails().stream().filter(detail -> detail.getField().equals("bar")).findFirst().get();
		assertThat(detail2.getMessage(), equalTo(String.format(DETAIL_MESSAGE, "sap", "hybris")));
		assertThat(detail2.getType(), equalTo(UNMODIFIABLE_FIELD));
		assertThat(detail2.getField(), equalTo("bar"));
	}

	@Test
	public void should_convert_empty_object_array()
	{
		conflicts.put("foo", new Object[] {});
		exception = new UpdateConflictException(conflicts);

		final ErrorMessage error = converter.toErrorMessage(exception);
		final ErrorMessageDetail detail = error.getDetails().get(0);
		assertThat(detail.getMessage(), equalTo(String.format(DETAIL_MESSAGE, null, null)));
		assertThat(detail.getType(), equalTo(UNMODIFIABLE_FIELD));
		assertThat(detail.getField(), equalTo("foo"));
	}

	@Test
	public void should_convert_null_source_in_object_array()
	{
		conflicts.put("foo", new Object[] {null, "world"});
		exception = new UpdateConflictException(conflicts);

		final ErrorMessage error = converter.toErrorMessage(exception);
		final ErrorMessageDetail detail = error.getDetails().get(0);
		assertThat(detail.getMessage(), equalTo(String.format(DETAIL_MESSAGE, null, "world")));
		assertThat(detail.getType(), equalTo(UNMODIFIABLE_FIELD));
		assertThat(detail.getField(), equalTo("foo"));
	}

	@Test
	public void should_convert_null_target_in_object_array()
	{
		conflicts.put("foo", new Object[] {"Hello", null});
		exception = new UpdateConflictException(conflicts);

		final ErrorMessage error = converter.toErrorMessage(exception);
		final ErrorMessageDetail detail = error.getDetails().get(0);
		assertThat(detail.getMessage(), equalTo(String.format(DETAIL_MESSAGE, "Hello", null)));
		assertThat(detail.getType(), equalTo(UNMODIFIABLE_FIELD));
		assertThat(detail.getField(), equalTo("foo"));
	}
}
