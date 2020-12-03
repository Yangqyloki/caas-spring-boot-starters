package com.hybris.caas.log.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hybris.caas.log.config.AuditLogProperties;
import com.hybris.caas.log.context.UserProvider;
import com.hybris.caas.log.util.JsonNodeUtils;
import com.sap.xs.audit.api.exception.AuditLogNotAvailableException;
import com.sap.xs.audit.api.exception.AuditLogWriteException;
import com.sap.xs.audit.api.v2.AuditLogMessageFactory;
import com.sap.xs.audit.api.v2.AuditedDataSubject;
import com.sap.xs.audit.api.v2.AuditedObject;
import com.sap.xs.audit.api.v2.ConfigurationChangeAuditMessage;
import com.sap.xs.audit.api.v2.DataAccessAuditMessage;
import com.sap.xs.audit.api.v2.DataModificationAuditMessage;
import com.sap.xs.audit.api.v2.SecurityEventAuditMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static com.hybris.caas.log.audit.service.Constants.AuditedObject.AuditedObjectType.ONLINE_SYSTEM;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Identifier.MODULE;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Identifier.NAME;
import static com.hybris.caas.log.audit.service.Constants.AuditedObject.Name.SAP_HYBRIS_CAAS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AsyncAuditLoggerTest
{
	private static final String SERVICE_NAME = "service-name";
	private static final String SUBACCOUNT_ID = "subaccount-id";
	private static final String UNKNOWN_USER = "-";
	private static final String UNKNOWN_CLIENT_IP = "-";
	private static final String OBJECT_ID = "objectId";
	private static final String OBJECT_TYPE = "objectType";
	private static final String ACCOUNT_ID = "account-id";

	private static final ObjectMapper MAPPER = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

	@Mock
	private AuditLogMessageFactory auditLogMessageFactory;
	@Mock
	private UserProvider userProvider;
	@Mock
	private ConfigurationChangeAuditMessage configChangeAuditMessage;
	@Mock
	private SecurityEventAuditMessage securityEventAuditMessage;
	@Mock
	private DataAccessAuditMessage dataAccessAuditMessage;
	@Mock
	private DataModificationAuditMessage dataModificationAuditMessage;
	@Mock
	private AuditedObject auditedObject;
	@Mock
	private AuditedDataSubject auditedDataSubject;
	@Mock
	private AuditLogProperties auditLogProperties;

	private final DataChangeObjectProcessor dataChangeObjectProcessor = new FullDataChangeObjectProcessor();
	private AsyncAuditLogger auditLogger;

	private Person alice;
	private Person bob;
	private DataAccessObject dataAccessObject;
	private DataSubject dataSubject;
	private DataChangeObject dataChangeObject;

	@Before
	public void setUp()
	{
		when(auditLogProperties.getServiceName()).thenReturn(SERVICE_NAME);
		when(auditLogProperties.isEnabled()).thenReturn(Boolean.TRUE);

		when(userProvider.getSubaccountId()).thenReturn(SUBACCOUNT_ID);
		when(userProvider.getUserId()).thenReturn(UNKNOWN_USER);
		when(userProvider.getClientIp()).thenReturn(UNKNOWN_CLIENT_IP);

		when(auditLogMessageFactory.createConfigurationChangeAuditMessage()).thenReturn(configChangeAuditMessage);
		when(auditLogMessageFactory.createSecurityEventAuditMessage()).thenReturn(securityEventAuditMessage);
		when(auditLogMessageFactory.createDataAccessAuditMessage()).thenReturn(dataAccessAuditMessage);
		when(auditLogMessageFactory.createDataModificationAuditMessage()).thenReturn(dataModificationAuditMessage);
		when(auditLogMessageFactory.createAuditedObject()).thenReturn(auditedObject);
		when(auditLogMessageFactory.createAuditedDataSubject()).thenReturn(auditedDataSubject);

		auditLogger = spy(new AsyncAuditLogger(auditLogProperties, auditLogMessageFactory, dataChangeObjectProcessor, userProvider));

		final Address aliceAddress = new Address();
		aliceAddress.setStreetNumber(99);
		aliceAddress.setStreetLine1("line-1");
		aliceAddress.setCity("city");

		alice = new Person();
		alice.setId("id");
		alice.setName("Alice");
		alice.setPhone(Arrays.asList("phone-1", "phone-2"));
		alice.setAddress(aliceAddress);

		final Address bobAddress = new Address();
		bobAddress.setStreetNumber(99);
		bobAddress.setStreetLine1("new-line-1");
		bobAddress.setStreetLine2("line-2");
		bobAddress.setCity("city");

		bob = new Person();
		bob.setId("id");
		bob.setName("Bob");
		bob.setPhone(Arrays.asList("phone-2", "phone-3"));
		bob.setAddress(bobAddress);

		dataAccessObject = DataAccessObject.build().addAttributes(true, "foo", "bar").addAttributes(false, "baz").addAttachment("attachment-id", "attachment-name");
		dataSubject = DataSubject.of(ACCOUNT_ID, DataSubject.Role.MERCHANT);
		dataChangeObject = DataChangeObject.withOldValue(alice).setNewValue(bob);
	}

	@Test
	public void should_audit_full_objects() throws AuditLogNotAvailableException, AuditLogWriteException, JsonProcessingException
	{
		auditLogger.logConfigurationChangeAuditMessage(OBJECT_ID, OBJECT_TYPE, dataChangeObject);

		assertAuditMessage();
		verify(configChangeAuditMessage).addValue("value", MAPPER.writeValueAsString(alice), MAPPER.writeValueAsString(bob));
		verify(configChangeAuditMessage).log();
		verifyNoMoreInteractions(configChangeAuditMessage);
	}

	@Test
	public void should_audit_null_objects() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		auditLogger.logConfigurationChangeAuditMessage(OBJECT_ID, OBJECT_TYPE, null);

		assertAuditMessage();
		verify(configChangeAuditMessage).addValue(anyString(), anyString(), anyString());
		verify(configChangeAuditMessage).log();
		verifyNoMoreInteractions(configChangeAuditMessage);
	}

	@Test
	public void should_log_security_event() throws JsonProcessingException
	{
		auditLogger.logSecurityEventAuditMessage(alice);
		verify(securityEventAuditMessage).setUser(UNKNOWN_USER);
		verify(securityEventAuditMessage).setTenant(SUBACCOUNT_ID);
		verify(securityEventAuditMessage).setIp(UNKNOWN_CLIENT_IP);
		verify(securityEventAuditMessage).setData(JsonNodeUtils.getMapper().writeValueAsString(alice));
	}

	@Test
	public void should_log_null_security_event() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		auditLogger.logSecurityEventAuditMessage(null);
		verify(securityEventAuditMessage).setUser(UNKNOWN_USER);
		verify(securityEventAuditMessage).setTenant(SUBACCOUNT_ID);
		verify(securityEventAuditMessage).setIp(UNKNOWN_CLIENT_IP);
		verify(securityEventAuditMessage).setData("{}");
		verify(securityEventAuditMessage).log();
	}

	@Test(expected = AuditLoggingException.class)
	public void should_fail_log_security_event_AuditLogNotAvailableException() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogNotAvailableException.class).when(securityEventAuditMessage).log();
		final Person alice = new Person();
		alice.setId("1");
		alice.setName("Alice");

		auditLogger.logSecurityEventAuditMessage(alice);
	}

	@Test(expected = AuditLoggingException.class)
	public void should_fail_log_security_event_AuditLogWriteException() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogWriteException.class).when(securityEventAuditMessage).log();
		final Person alice = new Person();
		alice.setId("1");
		alice.setName("Alice");

		auditLogger.logSecurityEventAuditMessage(alice);
	}

	@Test
	public void should_not_fail_log_security_event_AuditLogNotAvailableException_service_disabled() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogNotAvailableException.class).when(securityEventAuditMessage).log();
		when(auditLogProperties.isEnabled()).thenReturn(Boolean.FALSE);
		final Person alice = new Person();
		alice.setId("1");
		alice.setName("Alice");

		auditLogger.logSecurityEventAuditMessage(alice);
		verify(securityEventAuditMessage).log();
	}

	@Test
	public void should_log_data_access_with_explicit_object_type()
	{
		auditLogger.logDataAccessAuditMessage(OBJECT_ID, OBJECT_TYPE, dataAccessObject, dataSubject);
		verify(dataAccessAuditMessage).setUser(UNKNOWN_USER);
		verify(dataAccessAuditMessage).setTenant(SUBACCOUNT_ID);
		verify(dataAccessAuditMessage).addAttribute("foo", true);
		verify(dataAccessAuditMessage).addAttribute("bar", true);
		verify(dataAccessAuditMessage).addAttribute("baz", false);
		verify(dataAccessAuditMessage).addAttachment("attachment-id", "attachment-name");
		verify(dataAccessAuditMessage).setObject(auditedObject);
		verify(dataAccessAuditMessage).setDataSubject(auditedDataSubject);
	}

	@Test
	public void should_log_data_access_without_object_type()
	{
		auditLogger.logDataAccessAuditMessage(OBJECT_ID, bob, true, dataSubject);
		verify(dataAccessAuditMessage).setUser(UNKNOWN_USER);
		verify(dataAccessAuditMessage).setTenant(SUBACCOUNT_ID);
		verify(dataAccessAuditMessage).addAttribute("id", true);
		verify(dataAccessAuditMessage).addAttribute("name", true);
		verify(dataAccessAuditMessage).addAttribute("phone[]", true);
		verify(dataAccessAuditMessage).addAttribute("address.streetLine1", true);
		verify(dataAccessAuditMessage).addAttribute("address.streetLine2", true);
		verify(dataAccessAuditMessage).addAttribute("address.city", true);
		verify(dataAccessAuditMessage).setObject(auditedObject);
		verify(dataAccessAuditMessage).setDataSubject(auditedDataSubject);
	}

	@Test(expected = AuditLoggingException.class)
	public void should_fail_log_data_access_AuditLogNotAvailableException() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogNotAvailableException.class).when(dataAccessAuditMessage).log();
		auditLogger.logDataAccessAuditMessage(OBJECT_ID, OBJECT_TYPE, dataAccessObject, dataSubject);
	}

	@Test(expected = AuditLoggingException.class)
	public void should_fail_log_data_access_AuditLogWriteException() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogWriteException.class).when(dataAccessAuditMessage).log();
		auditLogger.logDataAccessAuditMessage(OBJECT_ID, OBJECT_TYPE, dataAccessObject, dataSubject);
	}

	@Test
	public void should_not_fail_log_data_access_AuditLogNotAvailableException_service_disabled() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		when(auditLogProperties.isEnabled()).thenReturn(Boolean.FALSE);
		doThrow(AuditLogNotAvailableException.class).when(dataAccessAuditMessage).log();
		auditLogger.logDataAccessAuditMessage(OBJECT_ID, OBJECT_TYPE, dataAccessObject, dataSubject);
		verify(dataAccessAuditMessage).log();
	}

	@Test
	public void should_log_data_modification_full()
			throws JsonProcessingException, AuditLogNotAvailableException, AuditLogWriteException
	{
		auditLogger.logDataModificationAuditMessage(OBJECT_ID, OBJECT_TYPE, dataChangeObject, dataSubject);
		verify(dataModificationAuditMessage).setUser(UNKNOWN_USER);
		verify(dataModificationAuditMessage).setTenant(SUBACCOUNT_ID);
		verify(dataModificationAuditMessage).addAttribute("value", JsonNodeUtils.getMapper().writeValueAsString(alice), JsonNodeUtils.getMapper().writeValueAsString(bob));
		verify(dataModificationAuditMessage).setObject(auditedObject);
		verify(dataModificationAuditMessage).setDataSubject(auditedDataSubject);
		verify(dataModificationAuditMessage).log();
	}

	@Test(expected = AuditLoggingException.class)
	public void should_fail_log_data_modification_AuditLogNotAvailableException() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogNotAvailableException.class).when(dataModificationAuditMessage).log();
		auditLogger.logDataModificationAuditMessage(OBJECT_ID, OBJECT_TYPE, dataChangeObject, dataSubject);
	}

	@Test(expected = AuditLoggingException.class)
	public void should_fail_log_data_modification_AuditLogWriteException() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		doThrow(AuditLogWriteException.class).when(dataModificationAuditMessage).log();
		auditLogger.logDataModificationAuditMessage(OBJECT_ID, OBJECT_TYPE, dataChangeObject, dataSubject);
	}

	@Test
	public void should_not_fail_log_data_modification_AuditLogNotAvailableException_service_disabled() throws AuditLogNotAvailableException, AuditLogWriteException
	{
		when(auditLogProperties.isEnabled()).thenReturn(Boolean.FALSE);
		doThrow(AuditLogNotAvailableException.class).when(dataModificationAuditMessage).log();
		auditLogger.logDataModificationAuditMessage(OBJECT_ID, OBJECT_TYPE, dataChangeObject, dataSubject);
		verify(dataModificationAuditMessage).log();
	}

	private void assertAuditMessage()
	{
		verify(configChangeAuditMessage).setUser(UNKNOWN_USER);
		verify(configChangeAuditMessage).setTenant(SUBACCOUNT_ID);
		verify(configChangeAuditMessage).setObject(auditedObject);
		assertAuditedObject();
	}

	private void assertAuditedObject()
	{
		verify(auditedObject).setType(ONLINE_SYSTEM);
		verify(auditedObject).addIdentifier(NAME, SAP_HYBRIS_CAAS);
		verify(auditedObject).addIdentifier(MODULE, SERVICE_NAME);
		verify(auditedObject).addIdentifier(OBJECT_TYPE, OBJECT_TYPE);
		verify(auditedObject).addIdentifier(OBJECT_ID, OBJECT_ID);
		verifyNoMoreInteractions(auditedObject);
	}

	private static class Person
	{
		private String id;
		private String name;
		private List<String> phone;
		private Address address;

		public String getId()
		{
			return id;
		}

		public void setId(final String id)
		{
			this.id = id;
		}

		public String getName()
		{
			return name;
		}

		public void setName(final String name)
		{
			this.name = name;
		}

		public List<String> getPhone()
		{
			return phone;
		}

		public void setPhone(final List<String> phone)
		{
			this.phone = phone;
		}

		public Address getAddress()
		{
			return address;
		}

		public void setAddress(final Address address)
		{
			this.address = address;
		}
	}

	private static class Address
	{
		private int streetNumber;
		private String streetLine1;
		private String streetLine2;
		private String city;

		public int getStreetNumber()
		{
			return streetNumber;
		}

		public void setStreetNumber(final int streetNumber)
		{
			this.streetNumber = streetNumber;
		}

		public String getStreetLine1()
		{
			return streetLine1;
		}

		public void setStreetLine1(final String streetLine1)
		{
			this.streetLine1 = streetLine1;
		}

		public String getStreetLine2()
		{
			return streetLine2;
		}

		public void setStreetLine2(final String streetLine2)
		{
			this.streetLine2 = streetLine2;
		}

		public String getCity()
		{
			return city;
		}

		public void setCity(final String city)
		{
			this.city = city;
		}
	}
}
