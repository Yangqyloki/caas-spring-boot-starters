package com.hybris.caas.security.serialize;

import java.io.IOException;

import org.owasp.html.PolicyFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hybris.caas.security.sanitization.HtmlPolicyAware;

/**
 * Jackon serializer that sanitizes the provided HTML content according to a
 * policy provided via the abstract {@link #getPolicy()} method.
 */
public abstract class HtmlSanitizationSerializer extends StdSerializer<String> implements HtmlPolicyAware
{
	private static final long serialVersionUID = 8723496653648943426L;

	public HtmlSanitizationSerializer()
	{
		this(null);
	}

	public HtmlSanitizationSerializer(Class<String> t)
	{
        super(t);
    }

	@Override
	public void serialize(String value, JsonGenerator jgen, SerializerProvider provider) throws IOException
	{
		final PolicyFactory policy = getPolicy();
		jgen.writeString(policy.sanitize(value));
	}

}
