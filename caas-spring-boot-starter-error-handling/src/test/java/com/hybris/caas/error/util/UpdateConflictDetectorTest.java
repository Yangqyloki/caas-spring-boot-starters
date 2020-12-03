package com.hybris.caas.error.util;

import com.hybris.caas.error.exception.UpdateConflictException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UpdateConflictDetectorTest
{
	@Test
	public void should_fail_detection_null_attribute()
	{
		Assert.assertThrows(IllegalArgumentException.class,
				() -> UpdateConflictDetector.withAttribute(null).withValues("hello", "world").detect());
	}

	@Test
	public void should_detect_conflict_strings()
	{
		Assert.assertThrows(UpdateConflictException.class,
				() -> UpdateConflictDetector.withAttribute("attr1").withValues("hello", "world").detect());
	}

	@Test
	public void should_detect_no_conflict_strings()
	{
		UpdateConflictDetector.withAttribute("attr1").withValues("hello", "hello").detect();
	}

	@Test
	public void should_detect_conflict_integers()
	{
		Assert.assertThrows(UpdateConflictException.class,
				() -> UpdateConflictDetector.withAttribute("attr1").withValues(5, 10).detect());
	}

	@Test
	public void should_detect_no_conflict_integers()
	{
		UpdateConflictDetector.withAttribute("attr1").withValues(5, 5).detect();
	}

	@Test
	public void should_detect_conflict_enums()
	{
		Assert.assertThrows(UpdateConflictException.class,
				() -> UpdateConflictDetector.withAttribute("attr1").withValues(HttpStatus.OK, HttpStatus.CONFLICT).detect());
	}

	@Test
	public void should_detect_no_conflict_enums()
	{
		UpdateConflictDetector.withAttribute("attr1").withValues(HttpStatus.OK, HttpStatus.OK).detect();
	}

	@Test
	public void should_detect_conflict_null_source()
	{
		Assert.assertThrows(UpdateConflictException.class,
				() -> UpdateConflictDetector.withAttribute("attr1").withValues(null, HttpStatus.CONFLICT).detect());
	}

	@Test
	public void should_detect_conflict_null_target()
	{
		Assert.assertThrows(UpdateConflictException.class,
				() -> UpdateConflictDetector.withAttribute("attr1").withValues(HttpStatus.OK, null).detect());
	}

	@Test
	public void should_detect_no_conflict_both_null()
	{
		UpdateConflictDetector.withAttribute("attr1").withValues(null, null).detect();
	}

}
