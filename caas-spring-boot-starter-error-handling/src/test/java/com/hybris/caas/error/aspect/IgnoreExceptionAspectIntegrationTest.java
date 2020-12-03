package com.hybris.caas.error.aspect;

import com.hybris.caas.error.annotation.IgnoreException;
import com.hybris.caas.error.annotation.IgnoreUniqueConstraint;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.TransactionSystemException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertThrows;

@ContextConfiguration(classes = IgnoreExceptionAspectIntegrationTest.TestConf.class)
public class IgnoreExceptionAspectIntegrationTest extends AbstractJUnit4SpringContextTests
{

	@Autowired
	private ClassUnderTest classUnderTest;

	@Test
	public void shouldIgnoreException()
	{
		try
		{
			classUnderTest.ignore(() -> {
				throw new IllegalArgumentException();
			});
		}
		catch (final Exception e)
		{
			fail("should have not thrown exception");
		}
	}

	@Test
	public void shouldIgnoreExceptionAndRootCauseMessage()
	{
		try
		{
			classUnderTest.ignoreWithRootCauseMessage(() -> {
				throw new IllegalArgumentException(new Exception("the root cause"));
			});
		}
		catch (final Exception e)
		{
			fail("should have not thrown exception");
		}
	}

	@Test
	public void shouldIgnoreExceptionAndCauseMessage()
	{
		try
		{
			classUnderTest.ignoreWithRootCauseMessage(() -> {
				throw new IllegalArgumentException("the root cause");
			});
		}
		catch (final Exception e)
		{
			fail("should have not thrown exception");
		}
	}

	@Test
	public void shouldIgnoreUniqueConstraintForTransactionException()
	{
		try
		{
			classUnderTest.ignoreUniqueConstraint(() -> {
				throw new TransactionSystemException("unique");
			});
		}
		catch (final Exception e)
		{
			fail("should have not thrown exception");
		}
	}

	@Test
	public void shouldIgnoreUniqueConstraintForJpaSystemException()
	{
		try
		{
			classUnderTest.ignoreUniqueConstraint(() -> {
				throw new JpaSystemException(new RuntimeException("duplicate key"));
			});
		}
		catch (final Exception e)
		{
			fail("should have not thrown exception");
		}
	}

	@Test
	public void shouldNotIgnoreException()
	{
		// we ignore illegal argument exception, null pointer exception thrown instead
		assertThrows(NullPointerException.class, () -> classUnderTest.ignore(() -> {
			throw new NullPointerException();
		}));
	}

	@Test
	public void shouldNotIgnoreExceptionDueToRootCauseMessage()
	{
		// we ignore illegal argument exception with 'root; cause message, message doesn't match
		assertThrows(IllegalArgumentException.class, () -> classUnderTest.ignoreWithRootCauseMessage(() -> {
			throw new IllegalArgumentException(new Exception("fail because of this message"));
		}));
	}

	@Test
	public void shouldNotIgnoreUniqueConstraintForTransactionException()
	{
		// message does not contain the expected root cause message, then won't get ignored
		assertThrows(TransactionSystemException.class, () -> classUnderTest.ignoreUniqueConstraint(() -> {
			throw new TransactionSystemException("different cause");
		}));
	}

	@Test
	public void shouldNotIgnoreUniqueConstraintForJpaSystemException()
	{
		// message does not contain the expected root cause message, then won't get ignored
		assertThrows(JpaSystemException.class, () -> classUnderTest.ignoreUniqueConstraint(() -> {
			throw new JpaSystemException(new RuntimeException("different cause"));
		}));
	}

	@Test
	public void shouldNotIgnoreExceptionDueToAopAdviseNotMatching()
	{
		// message does not contain the expected root cause message, then won't get ignored
		assertThrows(Exception.class, () -> classUnderTest.aopAdviseNotMatch());
	}

	// setup for test case

	@Configuration
	@EnableAspectJAutoProxy(proxyTargetClass = true)
	static class TestConf
	{
		@Bean
		public IgnoreExceptionAspect ignoreExceptionAspect()
		{
			return new IgnoreExceptionAspect();
		}

		@Bean
		public ClassUnderTest classUnderTest()
		{
			return new ClassUnderTest();
		}

	}

	static class ClassUnderTest
	{

		@IgnoreException(IllegalArgumentException.class)
		void ignore(final Supplier<?> supplier)
		{
			supplier.get();
		}

		@IgnoreException(value = IllegalArgumentException.class, rootCauseMessageFilter = "root")
		void ignoreWithRootCauseMessage(Supplier<?> supplier)
		{
			supplier.get();
		}

		@IgnoreUniqueConstraint
		void ignoreUniqueConstraint(Supplier<?> supplier)
		{
			supplier.get();
		}

		/**
		 * The advice only checks @IgnoreException or annotations annotated with @IgnoreException
		 * If more than two annotation deep level annotated with @IgnoreException, won't match the AOP advise
		 * For example: an annotation is annotated by an annotation annotated with @IgnoreException, it won't match the AOP advise
		 */
		@IgnoreExceptionReExtended
		void aopAdviseNotMatch()
		{
			throwAsUnchecked(new Exception("shouldn't be ignored"));
		}
	}

	@IgnoreExceptionExtended
	@interface IgnoreExceptionReExtended
	{
	}

	@Target(ElementType.ANNOTATION_TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@IgnoreException(value = Exception.class)
	@interface IgnoreExceptionExtended
	{
	}

	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwAsUnchecked(Throwable e) throws E
	{
		throw (E) e;
	}

}
