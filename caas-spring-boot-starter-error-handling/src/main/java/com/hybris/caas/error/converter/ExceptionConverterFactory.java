package com.hybris.caas.error.converter;

import com.hybris.caas.error.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.NestedExceptionUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This implementation of the {@link ExceptionConverter} acts as a converter
 * factory. All beans extending {@link AbstractExceptionConverter} will be
 * injected into the factory automatically.
 * <p>
 * When using this factory to convert an exception to a canonical error message, the
 * factory will consult the injected converters to find the one whose generic
 * type matches the exception type given. If no converter is found, then the
 * {@link DefaultExceptionConverter} will be used.
 * </p>
 */
@Primary
@SuppressWarnings("squid:S1452")
public class ExceptionConverterFactory implements ExceptionConverter
{
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionConverterFactory.class);

	@Autowired
	private Set<AbstractExceptionConverter<?>> abstractConverters;
	private AbstractExceptionConverter<?> defaultConverter;
	private final Map<Class<?>, AbstractExceptionConverter<?>> converters = new HashMap<>();

	public ExceptionConverterFactory(@Qualifier("defaultExceptionConverter") final AbstractExceptionConverter<?> defaultConverter)
	{
		this.defaultConverter = defaultConverter;
	}

	@Override
	@SuppressWarnings("pmd:CompareObjectsWithEquals")
	// line 56 [ex == rootCause] is checking for same instance so == is correct. Also HP Fortify will complain if using .equals here.
	public ErrorMessage toErrorMessage(Throwable ex)
	{
		AbstractExceptionConverter<?> converter = converters.get(ex.getClass());

		if (Objects.isNull(converter))
		{
			return defaultConverter.toErrorMessage(ex);
		}

		if (converter instanceof AbstractCauseExceptionConverter)
		{
			final Throwable cause = ((AbstractCauseExceptionConverter) converter).useRootCause() ?
					NestedExceptionUtils.getRootCause(ex) :
					ex.getCause();
			if (Objects.isNull(cause) || ex == cause)
			{
				// if Cause is null or has a cycle, then try the error message conversion with the abstractCauseExceptionConverter first.
				// if the abstract cause converter doesn't support conversion then use default converter.
				try
				{
					return converter.toErrorMessage(ex);
				}
				catch (UnsupportedOperationException unsupportedOperationException)
				{
					LOG.warn(String.format(
							"Failed to convert the exception to an error message with %s, will use default converter instead. Details: %s",
							converter.getClass(), unsupportedOperationException.getMessage()));
					return defaultConverter.toErrorMessage(Objects.nonNull(cause) ? cause : ex);
				}
			}
			else
			{
				// Recursively search for converter if the current converter is a cause converter.
				return toErrorMessage(cause);
			}
		}

		return converter.toErrorMessage(ex);
	}

	@PostConstruct
	public void postConstruct()
	{
		abstractConverters.forEach(converter -> converters.put(getConverterClass(converter), converter));
	}

	private static Class<?> getConverterClass(AbstractExceptionConverter<?> converter)
	{
		return GenericTypeResolver.resolveTypeArgument(converter.getClass(), AbstractExceptionConverter.class);
	}

	public AbstractExceptionConverter<?> getDefaultConverter()
	{
		return defaultConverter;
	}

	public void setDefaultConverter(AbstractExceptionConverter<?> defaultConverter)
	{
		this.defaultConverter = defaultConverter;
	}

	public Set<AbstractExceptionConverter<?>> getAbstractConverters()
	{
		return abstractConverters;
	}

	public void setAbstractConverters(Set<AbstractExceptionConverter<?>> abstractConverters)
	{
		this.abstractConverters = abstractConverters;
	}

	protected Map<Class<?>, AbstractExceptionConverter<?>> getConverters()
	{
		return converters;
	}

}
