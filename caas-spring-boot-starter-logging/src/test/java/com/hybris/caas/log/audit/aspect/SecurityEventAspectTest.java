package com.hybris.caas.log.audit.aspect;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.hybris.caas.log.audit.annotation.AuditSecurityEvent;
import com.hybris.caas.log.audit.service.AuditLogger;

@RunWith(MockitoJUnitRunner.class)
public class SecurityEventAspectTest
{
	@Mock
	private JoinPoint joinPoint;
	@Mock
	private MethodSignature signature;
	@Mock
	private AuditSecurityEvent annotation;
	@Mock
	private AuditLogger auditLogger;

	private SecurityEventAspect aspect;
	private Object securityEventObject;

	@Before
	public void setUp() throws Throwable
	{
		when(joinPoint.getSignature()).thenReturn(signature);
		aspect = new SecurityEventAspect(auditLogger);
	}

	@Test(expected = IllegalArgumentException.class)
	public void should_throw_exception_when_method_signature_returns_void() throws Throwable
	{
		when(signature.getReturnType()).thenReturn(Void.TYPE);
		aspect.logSecurityEvent(joinPoint, annotation, securityEventObject);
	}

	@Test
	public void should_handle_valid_object() throws Throwable
	{
		when(signature.getReturnType()).thenReturn(Object.class);
		aspect.logSecurityEvent(joinPoint, annotation, securityEventObject);
		verify(auditLogger).logSecurityEventAuditMessage(securityEventObject);
	}

	@Test
	public void should_handle_null_object() throws Throwable
	{
		when(signature.getReturnType()).thenReturn(Object.class);
		aspect.logSecurityEvent(joinPoint, annotation, null);
		verify(auditLogger).logSecurityEventAuditMessage(null);
	}

}
