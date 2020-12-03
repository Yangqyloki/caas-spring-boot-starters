package com.hybris.caas.error.aspect;

import com.hybris.caas.error.annotation.IgnoreException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Aspect that ignores exceptions thrown by managed Spring beans' methods annotated by:
 * - {@link IgnoreException} annotation
 * - annotated by annotations annotated by {@link IgnoreException}
 * Possible to filter by:
 * <ul>
 * <li>type</li>
 * <li>type and root cause message</li>
 * </ul>
 */
@Aspect
public class IgnoreExceptionAspect
{
	private static final Logger LOG = LoggerFactory.getLogger(IgnoreExceptionAspect.class);

	@Around("@annotation(com.hybris.caas.error.annotation.IgnoreException) ||"
					+ " execution(@(@com.hybris.caas.error.annotation.IgnoreException *) * *(..))")
	public void ignoreException(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		try
		{
			joinPoint.proceed();
		}
		catch (final Throwable t)
		{
			final IgnoreException ignoreExceptionAnnotation = getIgnoreExceptionAnnotation(joinPoint).orElseThrow(() -> t);

			final List<Class<? extends Exception>> ignoreExceptions = Arrays.asList(ignoreExceptionAnnotation.value());
			final List<String> rootCauseMessageFilters = Arrays.asList(ignoreExceptionAnnotation.rootCauseMessageFilter());

			if (filterByRootCauseMessage(rootCauseMessageFilters))
			{
				ignoreExceptionByTypeAndRootCauseMessage(t, ignoreExceptions, rootCauseMessageFilters);
			}
			else
			{
				ignoreExceptionByType(t, ignoreExceptions);
			}

			LOG.debug("Ignoring exception", t);
		}
	}

	private static Optional<IgnoreException> getIgnoreExceptionAnnotation(final ProceedingJoinPoint joinPoint)
	{
		final MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		final Method method = methodSignature.getMethod();

		// annotation found directly under the joinPoint method
		final Optional<IgnoreException> optionalIgnoreException = Optional.ofNullable(method.getAnnotation(IgnoreException.class));

		if (!optionalIgnoreException.isPresent())
		{
			// attempt to find the annotation under another annotation used in the joinPoint method
			return Arrays.asList(method.getAnnotations())
					.stream()
					.map(a -> Optional.ofNullable(a.annotationType().getAnnotation(IgnoreException.class)))
					.filter(Optional::isPresent)
					.map(Optional::get)
					.findAny();
		}

		return optionalIgnoreException;
	}

	private static boolean filterByRootCauseMessage(final List<String> rootCauseMessageFilters)
	{
		return !rootCauseMessageFilters.isEmpty();
	}

	@SuppressWarnings({"squid:S00112", "squid:S2201"})
	private static void ignoreExceptionByType(final Throwable t, final List<Class<? extends Exception>> ignoreExceptions)
			throws Throwable
	{
		exceptionStream(t, ignoreExceptions).findFirst().orElseThrow(()-> t);
	}

	@SuppressWarnings({"squid:S00112", "squid:S2201"})
	private static void ignoreExceptionByTypeAndRootCauseMessage(final Throwable t,
			final List<Class<? extends Exception>> ignoreExceptions, final List<String> rootCauseMessageFilters) throws Throwable
	{

		exceptionStream(t, ignoreExceptions).map(e -> NestedExceptionUtils.getMostSpecificCause(t))
				.map(Throwable::getMessage)
				.filter(message -> rootCauseMessageFilters.stream().anyMatch(message::contains))
				.findFirst()
				.orElseThrow(() -> t);
	}

	private static Stream<Class<? extends Exception>> exceptionStream(final Throwable t,
			final List<Class<? extends Exception>> ignoreExceptions)
	{
		return ignoreExceptions.stream().filter(ignoreException -> t.getClass().isAssignableFrom(ignoreException));
	}
}
