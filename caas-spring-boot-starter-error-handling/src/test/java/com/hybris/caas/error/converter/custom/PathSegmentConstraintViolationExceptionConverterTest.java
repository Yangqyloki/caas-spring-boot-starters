package com.hybris.caas.error.converter.custom;

import com.hybris.caas.error.ErrorConstants;
import com.hybris.caas.error.ErrorMessage;
import com.hybris.caas.error.ErrorMessageDetail;
import com.hybris.caas.error.exception.PathSegmentConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PathSegmentConstraintViolationExceptionConverterTest
{
	private static final String VIOLATION_MESSAGE = "violation message";

	private final PathSegmentConstraintViolationExceptionConverter converter = new PathSegmentConstraintViolationExceptionConverter();

	@Mock
	private Path path;
	@Mock
	private ConstraintViolation<?> violation;

	private Set<ConstraintViolation<?>> violations;
	private PathSegmentConstraintViolationException exception;

	@Before
	public void setUp()
	{
		violations = new HashSet<>();
		violations.add(violation);
		exception = new PathSegmentConstraintViolationException(violations);

		when(path.toString()).thenReturn("path.name");
		when(violation.getPropertyPath()).thenReturn(path);
		when(violation.getMessage()).thenReturn(VIOLATION_MESSAGE);
	}

	@Test
	public void should_convert_to_error_message_with_field()
	{
		final ErrorMessage errorMessage = converter.convert(exception);
		assertThat(errorMessage.getMessage(), equalTo(ErrorConstants.MESSAGE_404));
		assertThat(errorMessage.getStatus(), equalTo(HttpStatus.NOT_FOUND.value()));
		assertThat(errorMessage.getType(), equalTo(ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING));
		assertThat(errorMessage.getMoreInfo(), equalTo(ErrorConstants.INFO));

		final ErrorMessageDetail detail = errorMessage.getDetails().get(0);
		assertThat(detail.getMessage(), equalTo(VIOLATION_MESSAGE));
		assertThat(detail.getField(), equalTo("path.name"));
		assertThat(detail.getType(), equalTo(ErrorConstants.SUB_TYPE_400_INVALID_PATH_SEGMENT));
	}
}
