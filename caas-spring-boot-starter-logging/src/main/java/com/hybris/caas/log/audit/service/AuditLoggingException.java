package com.hybris.caas.log.audit.service;

/**
 * Exception to be thrown when audit logging could not be processed.
 */
@SuppressWarnings("serial")
public class AuditLoggingException extends RuntimeException
{
	public AuditLoggingException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
