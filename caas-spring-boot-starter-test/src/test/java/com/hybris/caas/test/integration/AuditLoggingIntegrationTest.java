package com.hybris.caas.test.integration;

import com.hybris.caas.test.integration.model.AuditDto;
import com.hybris.caas.test.integration.model.AuditEntity;
import com.hybris.caas.test.integration.service.AuditConfigurationService;
import com.hybris.caas.test.integration.service.AuditDataAccessService;
import com.hybris.caas.test.integration.service.AuditDataModService;
import com.hybris.caas.test.integration.service.AuditSecurityEventService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

@ExtendWith(OutputCaptureExtension.class)
public class AuditLoggingIntegrationTest extends AbstractIntegrationTest
{
	private static final String OBJECT_ID = "abc123";
	private static final String ATTRIBUTES_MARKER = ",\"attributes\":[";
	private static final String END_MARKER = "],\"status\":\"END\",";

	private static final String AUDITED_CONFIGURATION_CHANGE = "{\"name\":\"value\",\"old\":\"{\\\"id\\\":\\\"abc123\\\",\\\"string\\\":\\\"FOO BAR\\\",\\\"number\\\":17,\\\"listStrings\\\":[\\\"WORLD\\\",\\\"HELLO\\\"],\\\"subEntity\\\":{\\\"subString\\\":\\\"SUB FOO BAR\\\",\\\"subNumber\\\":59}}\",\"new\":\"{\\\"id\\\":\\\"abc123\\\",\\\"string\\\":\\\"BAR BAZ\\\",\\\"number\\\":79,\\\"listStrings\\\":[\\\"HELLO\\\",\\\"WORLD\\\"],\\\"subEntity\\\":{\\\"subString\\\":\\\"SUB BAR BAZ\\\",\\\"subNumber\\\":117}}\"}";
	private static final String AUDITED_SECURITY_EVENT_DATA = "\"data\":\"{\\\"id\\\":\\\"abc123\\\",\\\"string\\\":\\\"FOO BAR\\\",\\\"number\\\":17,\\\"listStrings\\\":[\\\"WORLD\\\",\\\"HELLO\\\"],\\\"subEntity\\\":{\\\"subString\\\":\\\"SUB FOO BAR\\\",\\\"subNumber\\\":59}}\"";
	private static final String AUDITED_DATA_SUBJECT = "\"data_subject\":{\"type\":\"Merchant\",\"role\":\"Merchant\",\"id\":{\"account_id\":\"zxy987\"}}";
	private static final String AUDITED_OBJECT = "\"object\":{\"type\":\"online_system\",\"id\":{\"name\":\"sap_hybris_caas\",\"module\":\"dummy-service\",\"objectType\":\"AuditEntity\",\"objectId\":\"abc123\"}}";
	private static final String AUDITED_DATA_ACCESS_ATTRIBUTES = "\"attributes\":[{\"name\":\"number\",\"successful\":true},{\"name\":\"string\",\"successful\":true},{\"name\":\"subEntity.subString\",\"successful\":true},{\"name\":\"subEntity.subNumber\",\"successful\":true},{\"name\":\"id\",\"successful\":false},{\"name\":\"listString\",\"successful\":true}]";

	@Autowired
	private AuditConfigurationService testConfigurationService;
	@Autowired
	private AuditSecurityEventService testSecurityEventService;
	@Autowired
	private AuditDataAccessService testDataAccessService;
	@Autowired
	private AuditDataModService testDataModService;

	private AuditDto auditDto;
	private AuditEntity auditEntity;

	@BeforeEach
	public void setUp()
	{
		final AuditDto.SubResource subResource = new AuditDto.SubResource();
		subResource.setSubString("SUB BAR BAZ");
		subResource.setSubNumber(117);

		auditEntity = testConfigurationService.get(OBJECT_ID);
		auditDto = new AuditDto();
		auditDto.setNumber(79);
		auditDto.setString("BAR BAZ");
		auditDto.setListStrings(Arrays.asList("HELLO", "WORLD"));
		auditDto.setSubResource(subResource);
	}

	@Test
	public void should_audit_log_configuration_change(final CapturedOutput outputCapture)
	{
		testConfigurationService.update(auditEntity, auditDto);

		final String loggedText = outputCapture.toString();
		final String attributes = StringUtils.substringBetween(loggedText, ATTRIBUTES_MARKER, END_MARKER);

		assertNewItem(attributes);
		assertOldItem(attributes);
	}

	@Test
	public void should_audit_log_configuration_partially_updated(final CapturedOutput outputCapture)
	{
		testConfigurationService.updatePartial(auditEntity, auditDto);

		final String loggedText = outputCapture.toString();
		final String beforeAttributes = StringUtils.substringBefore(loggedText, ATTRIBUTES_MARKER);
		final String afterStatusEnd = StringUtils.substringAfter(loggedText, END_MARKER);
		final String attributes = StringUtils.substringBetween(loggedText, ATTRIBUTES_MARKER, END_MARKER);

		assertThat(beforeAttributes, containsString(
				"\"success\":true,\"object\":{\"type\":\"online_system\",\"id\":{\"name\":\"sap_hybris_caas\",\"module\":\"dummy-service\",\"objectType\":\"AuditEntity\",\"objectId\":\"abc123\"}}"));

		assertThat(afterStatusEnd, containsString("\"tenant\":\"-\""));
		assertThat(attributes, equalTo(AUDITED_CONFIGURATION_CHANGE));
	}

	@Test
	public void should_audit_log_configuration_created(final CapturedOutput outputCapture)
	{
		testConfigurationService.create(auditDto);

		final String loggedText = outputCapture.toString();
		final String attributes = StringUtils.substringBetween(loggedText, ATTRIBUTES_MARKER, END_MARKER);

		assertThat(attributes, containsString("\"old\":\"{}\""));
		assertNewItem(attributes);
	}

	@Test
	public void should_audit_log_configuration_deleted(final CapturedOutput outputCapture)
	{
		testConfigurationService.delete(auditEntity);

		final String loggedText = outputCapture.toString();
		final String attributes = StringUtils.substringBetween(loggedText, ATTRIBUTES_MARKER, END_MARKER);

		assertThat(attributes, containsString("\"new\":\"{}\"}"));
		assertOldItem(attributes);
	}

	@Test
	public void should_not_audit_log_on_rollback(final CapturedOutput outputCapture)
	{
		auditEntity.setId("throwIt");
		try
		{
			testConfigurationService.delete(auditEntity);
			fail();
		}
		catch (final IllegalArgumentException e)
		{
			final String loggedText = outputCapture.toString();
			assertThat(loggedText.length(), is(0));
		}
		catch (final Throwable t)
		{
			fail();
		}

	}

	@Test
	public void should_log_security_event(final CapturedOutput outputCapture)
	{
		testSecurityEventService.trigger();
		assertThat(outputCapture.toString(), containsString(AUDITED_SECURITY_EVENT_DATA));
	}

	@Test
	public void should_log_data_access(final CapturedOutput outputCapture)
	{
		testDataAccessService.dataAccess();
		assertThat(outputCapture.toString(), containsString(AUDITED_DATA_SUBJECT));
		assertThat(outputCapture.toString(), containsString(AUDITED_OBJECT));
		assertThat(outputCapture.toString(), containsString(AUDITED_DATA_ACCESS_ATTRIBUTES));
	}

	@Test
	public void should_audit_log_data_modification(final CapturedOutput outputCapture)
	{
		testDataModService.update(auditEntity, auditDto);

		assertThat(outputCapture.toString(), containsString(AUDITED_DATA_SUBJECT));
		assertThat(outputCapture.toString(), containsString(AUDITED_OBJECT));
		assertThat(outputCapture.toString(), containsString("\"attributes\":[{\"name\":\"value\",\"old\":\"{\\\"id\\\":\\\"abc123\\\",\\\"string\\\":\\\"FOO BAR\\\",\\\"number\\\":17,\\\"listStrings\\\":[\\\"WORLD\\\",\\\"HELLO\\\"],\\\"subEntity\\\":{\\\"subString\\\":\\\"SUB FOO BAR\\\",\\\"subNumber\\\":59}}\",\"new\":\"{\\\"id\\\":\\\"abc123\\\",\\\"string\\\":\\\"BAR BAZ\\\",\\\"number\\\":79,\\\"listStrings\\\":[\\\"HELLO\\\",\\\"WORLD\\\"],\\\"subEntity\\\":{\\\"subString\\\":\\\"SUB BAR BAZ\\\",\\\"subNumber\\\":117}}\"}]"));
	}

	@Test
	public void should_audit_log_data_modification_partially_updated(final CapturedOutput outputCapture)
	{
		testDataModService.updatePartial(auditEntity, auditDto);

		assertThat(outputCapture.toString(), containsString(AUDITED_CONFIGURATION_CHANGE));
		assertThat(outputCapture.toString(), containsString(AUDITED_DATA_SUBJECT));
		assertThat(outputCapture.toString(), containsString(AUDITED_OBJECT));
	}

	@Test
	public void should_audit_log_data_modification_created(final CapturedOutput outputCapture)
	{
		testDataModService.create(auditDto);

		assertThat(outputCapture.toString(), containsString(AUDITED_DATA_SUBJECT));
		assertThat(outputCapture.toString(), containsString(AUDITED_OBJECT));
		assertThat(outputCapture.toString(), containsString("\"attributes\":[{\"name\":\"value\",\"old\":\"{}\""));
	}

	@Test
	public void should_audit_log_data_modification_deleted(final CapturedOutput outputCapture)
	{
		testDataModService.delete(auditEntity);

		assertThat(outputCapture.toString(), containsString(AUDITED_DATA_SUBJECT));
		assertThat(outputCapture.toString(), containsString(AUDITED_OBJECT));
		assertThat(outputCapture.toString(), containsString("\"new\":\"{}\"}"));
	}

	@Test
	public void should_not_audit_log_data_modification_on_rollback(final CapturedOutput outputCapture)
	{
		auditEntity.setId("throwIt");
		try
		{
			testDataModService.delete(auditEntity);
			fail();
		}
		catch (final IllegalArgumentException e)
		{
			final String loggedText = outputCapture.toString();
			assertThat(loggedText.length(), is(0));
		}
		catch (final Throwable t)
		{
			fail();
		}

	}

	private void assertOldItem(final String attributes)
	{
		final String oldItem = StringUtils.substringBetween(attributes, "old\":\"{", "}");
		assertThat(oldItem, containsString("\\\"id\\\":\\\"abc123"));
		assertThat(oldItem, containsString("\\\"number\\\":17"));
		assertThat(oldItem, containsString("\\\"string\\\":\\\"FOO BAR"));
		assertThat(oldItem, containsString("\\\"listStrings\\\":[\\\"WORLD"));
	}

	private void assertNewItem(final String attributes)
	{
		final String newItem = StringUtils.substringBetween(attributes, "new\":\"{", "}");
		assertThat(newItem, containsString("\\\"id\\\":\\\"abc123"));
		assertThat(newItem, containsString("\\\"number\\\":79"));
		assertThat(newItem, containsString("\\\"string\\\":\\\"BAR BAZ"));
		assertThat(newItem, containsString("\\\"listStrings\\\":[\\\"HELLO"));
	}

}
