package com.hybris.caas.data.persistence.converter;

import com.hybris.caas.data.config.AesEncryptionProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AesEncryptionConverterTest
{
	private static final String AES_KEY = "3272357538782F413F4428472B4B6150645367566B5970337336763979244226";
	private static final String AES_SALT = "397A24432646294A";
	private AesEncryptionConverter converter = new AesEncryptionConverter();

	@Test
	public void shouldConvertToDatabaseColumn()
	{
		AesEncryptionProperties properties = new AesEncryptionProperties();
		properties.setKey(AES_KEY);
		properties.setSalt(AES_SALT);
		converter.setAesEncryptionConverter(properties);

		assertThat(converter.convertToDatabaseColumn(null)).isNull();
		assertThat(converter.convertToDatabaseColumn("")).isBlank();
		assertThat(converter.convertToDatabaseColumn("password")).isNotBlank();
	}

	@Test
	public void shouldConvertToEntityAttribute()
	{
		AesEncryptionProperties properties = new AesEncryptionProperties();
		properties.setKey(AES_KEY);
		properties.setSalt(AES_SALT);
		converter.setAesEncryptionConverter(properties);

		assertThat(converter.convertToEntityAttribute(null)).isNull();
		assertThat(converter.convertToEntityAttribute("")).isBlank();
		assertThat(
				converter.convertToEntityAttribute("a9e6a490c12ee4dadb89166f680bf19e0fa7208a7da479bf7356d5ba9b4992ad")).isNotBlank();
	}

	@Test
	public void shouldThrowIllegalStateExceptionWhenConfigurationMissing()
	{
		assertThrows(IllegalStateException.class, () -> converter.convertToDatabaseColumn("password"));
	}
}
