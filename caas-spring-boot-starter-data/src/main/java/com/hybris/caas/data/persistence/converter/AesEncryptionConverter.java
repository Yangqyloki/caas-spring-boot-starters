package com.hybris.caas.data.persistence.converter;

import com.google.common.base.Strings;
import com.hybris.caas.data.config.AesEncryptionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 * AES encrypt/decrypt converter used to encrypt a string value, as credentials should be stored in the database securely.
 */
@Converter
@Component
public class AesEncryptionConverter implements AttributeConverter<String, String>
{
	private static TextEncryptor textEncryptor;

	@Autowired
	public void setAesEncryptionConverter(final AesEncryptionProperties aesEncryptionProperties)
	{
		AesEncryptionConverter.textEncryptor = Encryptors.text(aesEncryptionProperties.getKey(), aesEncryptionProperties.getSalt());
	}

	@Override
	public String convertToDatabaseColumn(final String attribute)
	{
		validateTextEncryptor();
		if (Strings.isNullOrEmpty(attribute))
		{
			return attribute;
		}
		return textEncryptor.encrypt(attribute);
	}

	@Override
	public String convertToEntityAttribute(final String dbData)
	{
		validateTextEncryptor();
		if (Strings.isNullOrEmpty(dbData))
		{
			return dbData;
		}
		return textEncryptor.decrypt(dbData);
	}

	private void validateTextEncryptor()
	{
		if (Objects.isNull(textEncryptor))
		{
			throw new IllegalStateException("Missing AES encryption configurations. AesEncryptionProperties must be populated.");
		}
	}
}
