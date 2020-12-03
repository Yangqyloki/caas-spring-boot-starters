package com.hybris.caas.error.exception;

/**
 * Exception thrown when a request is missing an HTTP request header parameter.
 */
public class MissingHttpRequestHeaderException extends RuntimeException
{

	private static final long serialVersionUID = 9162136393750639515L;
	public static final String MESSAGE = "Missing HTTP request header '%s'.";

	private final String header;

	public MissingHttpRequestHeaderException(String header)
	{
		super(String.format(MESSAGE, header));
		this.header = header;
	}

	public String getHeader()
	{
		return header;
	}
}
