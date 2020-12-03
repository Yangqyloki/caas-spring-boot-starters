package com.hybris.caas.log.audit.aspect;

import com.hybris.caas.log.audit.annotation.AuditSecurityEvent;
import com.hybris.caas.log.audit.service.AuditLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

/**
 * Aspect providing around advice for methods annotated with
 * {@link AuditSecurityEvent}. The advice will automatically attempt to
 * log a security event audit message with the method's return value.
 * <p>
 * If the method's return value is in the method's signature is <code>void</code>,
 * then an {@link IllegalArgumentException} will be thrown.
 * <p>
 * The {@link Order} annotation has been added to ensure that the aspect
 * is applied after the transaction aspect, if any.
 *
 * @see AuditSecurityEvent
 * @see AuditLogger
 */
@Aspect
@Order(300)
public class SecurityEventAspect
{
	private static final String ERROR_MESSAGE = "Security event log request failed. Please verify your configuration and make sure that the method's return type is not 'void'.";
	private final AuditLogger auditLogger;

	public SecurityEventAspect(final AuditLogger auditLogger)
	{
		this.auditLogger = auditLogger;
	}

	@AfterReturning(pointcut = "@annotation(annotation)", returning = "returnValue")
	public void logSecurityEvent(final JoinPoint joinPoint, final AuditSecurityEvent annotation, final Object returnValue)
	{
		final boolean isVoidMethod = Void.TYPE.equals(((MethodSignature) joinPoint.getSignature()).getReturnType());
		if (isVoidMethod)
		{
			throw new IllegalArgumentException(ERROR_MESSAGE);
		}

		auditLogger.logSecurityEventAuditMessage(returnValue);
	}

}
