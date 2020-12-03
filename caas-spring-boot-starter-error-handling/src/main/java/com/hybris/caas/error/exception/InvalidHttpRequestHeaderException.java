package com.hybris.caas.error.exception;

/**
 * Exception thrown when a request has an invalid HTTP request header parameter.
 */
public class InvalidHttpRequestHeaderException extends RuntimeException
{
	private static final long serialVersionUID = 2664614817635041547L;
	private static final String MESSAGE = "Invalid HTTP request header '%s'.";

	private final String header;

	public InvalidHttpRequestHeaderException(String header)
	{
		super(String.format(MESSAGE, header));
		this.header = header;
	}

	public String getHeader()
	{
		return header;
	}

}

