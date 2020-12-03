package com.hybris.caas.log.audit.aspect;

import com.hybris.caas.log.audit.annotation.AuditConfigurationChange;
import com.hybris.caas.log.audit.service.AuditLogger;
import com.hybris.caas.log.audit.service.DataChangeObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import javax.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Aspect providing around advice for methods annotated with
 * {@link AuditConfigurationChange}. The advice will automatically attempt to
 * log a configuration change audit message with the join point arguments and
 * the method's return value. If the <code>newValue</code> is <code>null</code>,
 * then an {@link IllegalArgumentException} will be thrown.
 * <p>
 * If some arguments are missing, the advice will attempt to retrieve the
 * information by other means: a field annotated with {@link Id} for the
 * <code>object id</code>.
 * <p>
 * The {@link Order} annotation has been added to ensure that the aspect
 * it is applied after the transaction aspect, if any.
 *
 * @see AuditConfigurationChange
 * @see AuditLogger
 */
@Aspect
@Order(300)
public class ConfigurationChangeAspect
{
	private static final String ERROR_MESSAGE = "Configuration change audit log request failed. Please verify your configuration.";
	private static final Logger LOG = LoggerFactory.getLogger(ConfigurationChangeAspect.class);

	private final AuditLogger auditLogger;

	public ConfigurationChangeAspect(final AuditLogger auditLogger)
	{
		this.auditLogger = auditLogger;
	}

	/**
	 * Supported method signatures:
	 * <p>
	 * newEntity create() - oldEntity -> new Object()
	 * newEntity update(oldEntity)
	 * void delete(oldEntity)
	 */
	@Around(value = "@annotation(annotation)")
	public void storeConfigurationValue(final ProceedingJoinPoint joinPoint, final AuditConfigurationChange annotation)
			throws Throwable
	{
		// Get all argument values from join point
		final List<Object> arguments = Arrays.asList(ArrayUtils.nullToEmpty(joinPoint.getArgs()));

		// Get all method parameter names from join point.
		final List<String> parameterNames = Arrays.asList(
				ArrayUtils.nullToEmpty(((MethodSignature) joinPoint.getSignature()).getParameterNames()));

		// Get method return type and check for void to support delete
		final boolean isDeleteMethod = Void.TYPE.equals(((MethodSignature) joinPoint.getSignature()).getReturnType());

		// Get old value (could be missing in which case a new Object is created)
		final String oldValueParameterName = annotation.oldValue();
		final Object oldValueObject = getArgumentByParameterName(oldValueParameterName, parameterNames, arguments).orElseGet(Object::new);
		final DataChangeObject dataChangeObject = DataChangeObject.withOldValue(oldValueObject);

		// Call target service and only continue if no exception was thrown.
		final Object returnValueObject = joinPoint.proceed();
		final String objectType;
		final Object newValueObject;
		// Delete should have void return type
		if (isDeleteMethod)
		{
			newValueObject = new Object();
			objectType = oldValueObject.getClass().getSimpleName();
		}
		else
		{
			newValueObject = Optional.ofNullable(returnValueObject).orElseThrow(() -> new IllegalArgumentException(ERROR_MESSAGE));
			objectType = returnValueObject.getClass().getSimpleName();
		}

		// Get object id, can be null
		final String objectIdParameterName = annotation.objectId();
		final Optional<String> objectIdFromArgument = getArgumentAsStringByParameterName(objectIdParameterName, parameterNames, arguments);
		final String objectId = objectIdFromArgument.orElseGet(() -> getObjectId(newValueObject).orElse(null));
		auditLogger.logConfigurationChangeAuditMessage(objectId, objectType, dataChangeObject.setNewValue(newValueObject));
	}

	/**
	 * Get the argument value in the join point by it's parameter name.
	 *
	 * @param parameterName the name of the parameter to retrieve
	 * @param parameters    a list of all parameter names in the signature
	 * @param arguments     a list of all arguments in the join point
	 * @return the argument value as an {@link Object} or {@link Optional#empty()}
	 */
	private Optional<Object> getArgumentByParameterName(final String parameterName, final List<String> parameters,
			final List<Object> arguments)
	{
		return Optional.of(parameters.indexOf(parameterName)).filter(index -> index >= 0).map(arguments::get);
	}

	/**
	 * Get the stringified argument value in the join point by it's parameter name.
	 *
	 * @param parameterName the name of the parameter to retrieve
	 * @param parameters    a list of all parameter names in the signature
	 * @param arguments     a list of all arguments in the join point
	 * @return the argument value as an {@link String} or {@link Optional#empty()}
	 */
	private Optional<String> getArgumentAsStringByParameterName(final String parameterName, final List<String> parameters,
			final List<Object> arguments)
	{
		return getArgumentByParameterName(parameterName, parameters, arguments).map(Object::toString);
	}

	/**
	 * Get the audited object's object id from either the old value or the new value. This will find any field annotated with {@link Id}.
	 *
	 * @param object the object holder a reference to the object id
	 * @return the object id or {@link Optional#empty()}
	 */
	private Optional<String> getObjectId(final Object object)
	{
		final List<Field> fields = Arrays.asList(FieldUtils.getFieldsWithAnnotation(object.getClass(), Id.class));
		if (fields.isEmpty())
		{
			return Optional.empty();
		}
		try
		{
			final Field field = fields.iterator().next();
			field.setAccessible(Boolean.TRUE);
			return Optional.ofNullable(field.get(object)).map(Object::toString);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			LOG.warn("An error occurred while extracting the object id for a configuration change request.", e);
			return Optional.empty();
		}
	}
}
