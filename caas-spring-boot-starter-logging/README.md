# SAP Hybris - CaaS Spring Boot Starter for Logging
---
Adds Slf4j logging support to your Spring Boot application.

## Features
___
* STDOUT text appender with CaaS pattern.
* STDOUT json appender wth SAP expected pattern.
* Logging integration with EclipseLink.
* Tracing with Spring Sleuth.
* Audit Logging API.
___

### Enabling Logging
In order to enable logging, you simply need to add the `com.hybris.caas:caas-spring-boot-starter-logging` dependency to your build. All configurations will be done automatically via the spring auto-configuration process.

With the dependency added, you can now use one of the logging appenders provided to log following the pre-configured patterns.

### Text Appender
The text-based appender will ensure that application logs are outputted to the console using the following format:

	%date %highlight(%-5level) [%thread] - %cyan([%logger]) %magenta([%mdc]) - %msg%n

The text appender can be found in the `logback-caas-text-appender.xml` file.

### JSON Appender
The json appender  will ensure that application logs are outputted to the console using the format suggested by SCP.
The json appender can be found in the `logback-caas-json-appender.xml` file.

### How To Use The Appenders
With the text and json appenders added to your classpath, you can use them by including them in your local logback configuration.

##### Example
This example will use the text-based appender and assumes that a new configuration is at `src/main/resources/logback-spring.xml`.
 ```xml
<configuration>
	<include resource="logback-caas-text-appender.xml" />

	...

	<root level="INFO">
		<appender-ref ref="STDOUT" />
	</root>
</configuration>
 ```

### Spring Profile Integration
In order to use the text-based appender for `dev` and `testing` environments, but use the json appender for `stage` or `prod` environments running cloud foundry, you can use spring profiles and select which appender to use based on the active profile.

##### Example
```xml
<configuration>
	<springProfile name="dev, test">
		<include resource="logback-caas-text-appender.xml" />
	</springProfile>
	<springProfile name="stage, prod">
		<include resource="logback-caas-json-appender.xml" />
	</springProfile>

	...

	<root level="INFO">
		<appender-ref ref="STDOUT" />
	</root>
</configuration>
```

NOTE: When using spring profiles in a `logback-spring.xml` file, you may experience some problems if the active profile does not match any of the profiles mentioned in the `logback-spring.xml` file. This is because no appender will be loaded and so the `<appender-ref ref="STDOUT">` will fail since logback cannot find the appender named `STDOUT`.

The root cause of this issue is usally that you do not have an active profile set at the time the logging is being bootstrapped by spring. You can fix this in a few ways:
* Define an environment variable in your OS like this: `SPRING_PROFILES_ACTIVE=dev` (where 'dev' is the name of the profile you want to be active at bootstrap)
* Start your application with the property like this: `$gradle bootRun -Dspring.profiles.active=dev`
* Set the default profile in a `bootstrap.yml` file (if no active profile is defined at bootstrap, then the default profile is actually `default`, so you can set that profile to also activate the profiles of your choice at bootstrap):
```yaml
spring
  profiles: default
spring.profiles.active: dev  # where 'dev' is the name of the profile you want to be active at bootstrap
```

### HTTP Request/Response Logging
If you want to log the HTTP request and response, we recommend using the [Zalando logbook](https://github.com/zalando/logbook) spring-boot-starter. This logging framework is highly configurable through your `application.yml`, is very well documented and has an MIT open source license.

Another **alternative** is to register the SAP `RequestLoggingFilter` from `com.sap.hcp.cf.logging:cf-java-logging-support-servlet`.
```java
@Configuration
public class LoggingConfiguration
{
	@Bean
	public FilterRegistrationBean<RequestLoggingFilter> loggingFilterRegistrationBean()
	{
		final FilterRegistrationBean<RequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
		final RequestLoggingFilter filter = new RequestLoggingFilter();
		registrationBean.setFilter(filter);
		registrationBean.setOrder(-100);
		return registrationBean;
	}
}
```

### Log Levels
If you want to be able to update log levels at runtime, we recommend using the [Spring Boot Actuator](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready) spring-boot-starter.

REMINDER: You need to properly secure these new endpoints with basic auth or OAuth2 via a special hidden scope.

### EclipseLink Integration
If you are using eclipselink, all logs from eclipselink will also be formatted following the expected format. You can override some of the eclipselink logging properties in your `application.properties`.

* `spring.jpa.properties.eclipselink.logging.timestamp`= true|false
* `spring.jpa.properties.eclipselink.logging.session`=true|false
* `spring.jpa.properties.eclipselink.logging.thread`=true|false
* `spring.jpa.properties.eclipselink.logging.exception`=true|false

Eclipselink offers many logging levels. Here is how they are mapped to Slf4j levels:

| EclipseLink | Slf4J |
| ------------|-------|
| OFF         | No Log|
| ALL         | TRACE |
| FINEST      | TRACE |
| FINER       | TRACE |
| FINE        | DEBUG |
| CONFIG      | DEBUG |
| INFO        | INFO  |
| WARNING     | WARN  |
| SEVERE      | ERROR |

For more information on eclipselink logging see [EclipseLink JPA Logging][1].

To enable eclipselink logging, an entry such as the following will need to be added to your `src/main/resources/logback-spring.xml` file:
```<logger name="org.eclipse.persistence.logging" level="WARN" />```

To enable eclipselink logging for a specific category, fo example sql, or for fine grained control an entry such as the following will need to be added to your `src/main/resources/logback-spring.xml` file:
```<logger name="org.eclipse.persistence.logging.sql" level="WARN" />```

**Important:** A setting such as `spring.jpa.properties.eclipselink.logging.level=INFO` in your `application.properties` it is not taken into consideration. You must have entries such as the ones indicated above in your `src/main/resources/logback-spring.xml` file to enable eclipselink logging.

### Tracing with Spring Sleuth
In a microservices architecture, tracing refers to the ability to track individual requests through the system. Each entry point into the system should generate a unique `traceId` that will identify the request during it's lifecyle. This trace identifier needs to be propagated to other services (via HTTP or AMQP) or other threads within the same service.

The trace information will be added to the Slf4j MDC and be printed in the application logs to help trace requests when analyzing application logs.

Currently, the following integrations have been tested:
* `Runnable` via `TraceRunnable`
* `Runnable` via `tracing.currentTraceContext().wrap(runnable)`
* `Callable` via `TraceCallable`
* `Callable` via `tracing.currentTraceContext().wrap(callable)`
* HTTP integration via `TraceFilter`
* HTTP client integration via `RestTemplate`
* Asynchronous communication via `@Async`
* Asynchronous communication via `TraceableExecutorService`
* Messaging via `RabbitTemplate`
* Logging via `org.slf4j.Logger`

**NOTE**

If you use asynchronous communication within your service: `ExecutorService` with `Runnable` or `Callable`, `Hystrix`, `RxJava`, `@Async`, `AsyncRestTemplate` then you may have more work to do to enable the sleuth tracing in those cases.
Please see this link for more information on how to do that: https://cloud.spring.io/spring-cloud-sleuth/single/spring-cloud-sleuth.html#_integrations or reach out to Polar Bears for assistance.

#### correlation_id
The source of the `correlation_id` is the `X-CorrelationId` HTTP request header which is originally sent by the AppRouter. The `X-CorrelationId` will then be propagated to other threads and services via Sleuth as mentioned above.

#### tenant_id
The source of the `tenant_id` is the `TenantService`. Also, the `tenant_id` will be propagated to other threads and services via Brave's `baggage-` utility.

#### Injected Values
There are currently 5 values that are being injected into the tracing context for propagation. They are the `tenant`, the `user_id`, `subaccount_id`, `client_ip` and the `X-CorrelationID`.

1. `tenant`: The tenant is extracted via the `TenantTracingPropagationFilter` in the `caas-spring-boot-starter-multitenant`.
2. `user_id`: The user's `accountId` is extracted via the `UserTracingPropagationFilter` in the `caas-spring-boot-starter-security`.
3. `subaccount_id`: The tenant's `subaccountId` is extracted via the `UserTracingPropagationFilter` in the `caas-spring-boot-starter-security`.
4. `client_ip`: The client's IP address is extracted via the `UserTracingPropagationFilter` in the `caas-spring-boot-starter-security`.
5. `X-CorrelationID`: The correlation ID is always appended by AppRouter. For more information please refer to https://stash.hybris.com/projectgs/CAAS2/repos/approuter/browse/lib/ext/correlation-id.js


##### User Provider Strategy
`UserProvider` strategy that extracts and provides the values mentioned in `Injected Values` section.

The default implementation of the `UserProvider` strategy is provided by `TracingUserProvider` that attempts to extract the injected values from the `Tracing` context.

In the case of `tenant`, `user_id`, and `subaccount_id` if no value is found then `-` is being used to indicate an unknown value.
As for `client_ip`, `0.0.0.0` is being used to indicate an unknown value.

###### Overriding Tenant Service Strategy
`UserProvider` default strategy could be overridden in a service, if needed, by defining a bean of `UserProvider` type.

```java
@Bean
public UserProvider overridingUserProvider(final Tracing tracing)
{
	return new OverriddenUserProvider(tracing);
}
```

#### Slf4j MDC
Please note that the `tenant` from the tracing context is added to the mapped diagnostic context. Also, for every new trace, the SAP logging context fields are added to the thread's MDC automatically by the `CaasSlf4jCurrentTraceContext` bean.

The SAP logging context fields are not added to the tracing context since these are different per service and thus should not be propagated. Instead, they should only be added to the MDC.

The SAP logging context fields are as follows: `organization_id`, `organization_name`, `space_id`, `space_name`, `container_id`, `component_id`, `component_name`, `component_instance`, `component_type`.

Sample log output with text appender:
```
2018-05-31 14:39:55,439 INFO  [http-nio-8181-exec-1] - [com.hybris.caas.log.tracing.test.TestRestController] [component_id=fa05c1a9-0fc1-4fbd-bae1-139850dec7a3, component_name=my-app, organization_name=-, component_type=application, space_name=my-space, component_instance=1, organization_id=-, space_id=06450c72-4669-4dc6-8096-45f9777db68a, container_id=my-container, correlation_id=088aafb0-3351-4c0e-82c8-78d310acfc27, tenant_id=my-tenant] - GET /test-resource
```

#### Help me!
If you need help setting up an integration with Sleuth, please look at the following test controller for some complete end-to-end examples of how to integrate: `com.hybris.caas.log.tracing.test.TestRestController`.

Or you can visit the official Spring Sleuth documentation: <https://cloud.spring.io/spring-cloud-sleuth/single/spring-cloud-sleuth.html#_integrations>.

#### How do I know if it's working?
1. Just look at your application logs! You should see the following 3 fields printed in the **MDC**: `correlation_id` and `tenant_id`.
2. You can peek at the tracing context anywhere in your code by injecting the `Tracing` bean and extracting information from it. You can extract the `user_id`, `subaccount_id`, and `tenant` by looking at the extra fields like this:
```java
@Autowired
private Tracing tracing
...
final TraceContext traceContext = tracing.tracer().currentSpan().context();
final String userId = ExtraFieldPropagation.get(traceContext, "user_id");
final String subaccountId = ExtraFieldPropagation.get(traceContext, "subaccount_id");
final String tenantId = ExtraFieldPropagation.get(traceContext, "tenant_id");
```
3. If you want to verify that the `user_id`, `subaccount_id` and `tenant_id` are propagated via **HTTP**, then you can inspect the downstream service's HTTP headers and see if Sleuth has added the `baggage-user_id`, `baggage-subaccount_id` and `baggage-tenant_id` HTTP header.
4. If you want to verify that the `user_id`, `subaccount_id` and `tenant_id` are propagated via **AMQP**, then you can inspect the downstream service's AMQP property named `headers` and see if Sleuth has added the `baggage_user_id`, `baggage-subaccount_id` and `baggage_tenant_id` headers.

### Audit Logging API
SAP provides a Java API for audit logging, but this has several limitations:
1. It it fully synchronous and errors would result in 500 errors in the service.
2. It does not integrate well with SQL transactions (requires 2 calls for prepare & log).
3. It does not provide meaningful defaults to known values.
4. It does not provide assistance to serialize change sets to string format.
5. It requires a lot of boilerplate code.
6. It requires propagation of cross-cutting concern code throughout the codebase (user id, tenant, ...).

In order to provide a better developer experience, we created the `AuditLogger` Java API. For a list of some of the features that this API offers, please see below.

#### Enable/Disable SAP Audit Logging Service
The SAP audit logging API will fallback to logging audit messages to the console if the Cloud Foundry binding is not present. This could lead to an application writing audit logs to the application logs by mistake if the binding is ommitted. In order to avoid this, the `sap.audit.service.enabled` property defaults to `true`. This means that if the Cloud Foundry binding is missing, an `AuditLogException` will be thrown at application startup, thus preventing the application from logging audit messages in the wrong place. In the `sap.audit.service.enabled` property set to `false`, then the application will be allowed to startup normally and used log the audit messages to the console.

**Only set `sap.audit.service.enabled=false` for development or CI environments.**

#### Synchronous vs. Asynchronous
When the application using `AsyncAuditLogger` logger has enabled spring asynchronous processing via `@EnableAsync`, then all audit logging will be done by another thread, otherwise in the same thread.

By default, when adding `@EnableAsync` to your application, spring will create a `SimpleAsyncTaskExecutor` which **does not reuse threads**. You can and **should** change this be registering a bean that implements `AsyncConfigurer` and overrides the `default` methods.

`AsyncAuditLogger` makes use of `UserProvider` to set the `tenant`, `user`, and `ip` fields as needed for an `AuditLogMessage`.

#### Transaction Support
The standard audit logging configuration will use `AuditLoggingTransactionalDecorator`.
This will ensure that if the audit logging is requested from within an active transaction, that this audit logging is scheduled
for execution after the current transaction is committed. If the transaction rolls back, then no audit logging will take place.

If the method performing the audit logging is not transactional, then the audit logging is invoked immediately.

In both cases, the audit logging is done through the use of `TransactionalAuditLogMessage#logSuccess` method provided by the SAP audit library.

##### Annotation-Driven Example
```
	@Transactional
	@AuditConfigurationChange(oldValue = "previousActiveEdition")
	public ActiveEdition setActiveEdition(final TenantHolder tenantHolder, final ActiveEdition previousActiveEdition,
			final ActiveEditionDto activeEditionDto)
	{
	    ...
	}
```

#### Meaningful Default Values
The `AuditLogger` API will only require the attributes that represent the actual object and data subject in the audit log.

The following attributes will be populated with the values described below:
* `user`: user account-id
* `tenant`: tenant identifier
* `channel`: "web service"
* `object.type`: "online system"
* `object.identifier[0]`: "name" -> "sap_hybris_caas"
* `object.identifier[1]`: "module" -> service name
* `ip`: client ip address

#### Standard Serialization
In order to be consistent and effecient across all teams, the `AuditLogger` API will assume that all audited objects be in `JsonNode` or `Object` formats. These formats will then be serialized to JSON string via an `ObjectMapper` encapsulated in the audit logging utility.

There is also a helpful utility `JsonNodeUtils` that is useful for converting Java `Object` to `JsonNode`. This will ensure that all teams are using the same `ObjectMapper` and will help promote consistency.
```java
final JsonNode oldValue = JsonNodeUtils.valueToTree(myEntity);
```

##### Custom Serialization Annotations
The JsonNodeUtils class contains an `ObjectMapper` that's configured with a custom annotation introspector, the `AuditLoggingIntrospector`.
This allows the use of custom annotations to hide or obfuscate certain entity attributes when the object is serialized into a JSON, ie: a secret key or a credit card number.

Three annotations are provided

* `@NonAuditableField`: The attribute value is converted to `null` during serialization. ie: `{"CreditCard": null}`
* `@MaskedAuditField`: The attribute value is obfuscated during serialization by converting all its characters to asterisks (*). ie: `{"CreditCard": "****************""}`
* `@PartiallyMaskedAuditField`: The attribute value is obfuscated during serialization by converting all its characters to asterisks except the last 4 digits. ie: `{"CreditCard": "************8673""}`

#### Modes
There are 2 modes currently available when logging changes to configuration or private data:
* `Mode.FULL`: The complete JSON string representations of the `<NewEntity>` and `<OldEntity>` objects are being audited. This is the default auditing mode. The audited objects are being logged using the `addValue` method of the `ConfigurationChangeAuditMessage` with the key set to `value`.
* [Experimental] `Mode.DENORMALIZED_DIFF`: This is an experimental feature. It should be used with care and for simple objects with no nested objects. Please ensure that the expected audit log entries are written before relying on it. Also, please note that it might change in the future.

##### Experimental - Mode.DENORMALIZED_DIFF
The `Mode.DENORMALIZED_DIFF` uses JSON Patch to capture the differences between objects and only audits these differences using as key to identify a change the name of the object member (nested members are separated by `.`) that changed and in the case of a list it includes the index of the element from the list as well part of the key.

Due to the fact that nested members are separated with `.`, only **java objects** are supported (no support for objects built from free JSON that could contain `/` within the name part of a name/value pair).

In the case of creation and deletion, the differences are captured against an empty object either as source or target, respectively.
Also, when no differences are detected between objects, nothing gets added to the audit log.

The `JsonNodeUtils` utility provides a means to produce a diff between 2 `JsonNode` objects.

```java
final Set<AttributeDiff> diffs = JsonNodeUtils.diff(oldObject, newObject);
```
The `Attribute` diff encapsulates a single JSON attribute change, containing the key of the attribute along with the old value and new value.

The implementation is based on [ZJSONPATCH](|https://github.com/flipkart-incubator/zjsonpatch) library that follows [RFC 6902 JSON Patch Specification](https://tools.ietf.org/html/rfc6902).

#### Usage - Configuration Changes
The `AuditLogger` API provides a simple method for auditing configuration changes.

##### Java API
```java
	/**
	 * Log a configuration change audit message.
	 *
	 * @param objectId   the identifier of the object being audited
	 * @param objectType the type of object being audited
	 * @param dataChangeObject the old and new values of the object bing audited
	 * @param auditMode  the audit mode
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logConfigurationChangeAuditMessage(String objectId, String objectType, DataChangeObject dataChangeObject, Mode auditMode);
```

##### Example
```java
public void update(final String tenant, final String id, final ApplicationDto applicationDto)
	{
		final Application application = applicationRepository.findById(id);
		final DataChangeObject dataChangeObject = DataChangeObject.withOldValue(application);

		final Application updatedApplication = conversionService.convert(applicationDto, Application.class);
		application.setName(updatedApplication.getName());
		application.setBundleName(updatedApplication.getBundleName());
		application.setBundleNumber(updatedApplication.getBundleNumber());
		application.setBundleVersion(updatedApplication.getBundleVersion());

		final Application saved = applicationRepository.save(application);

		auditLogger.logConfigurationChangeAuditMessage(application.getId(), Application.class.getSimpleName(),
				dataChangeObject.setNewValue(application), Mode.FULL);
	}
```

##### Annotation-Driven
`AuditConfigurationChange` annotation should be used to mark methods that require configuration audit logging.

The annotation support is handled through `ConfigurationChangeAspect` aspect providing around advice for the annotated methods.
The advice will automatically attempt to log a configuration change audit message with the join point arguments and the method's return value.

##### Example
```java
	@AuditConfigurationChange(oldValue = "previousActiveEdition")
	public ActiveEdition setActiveEdition(final TenantHolder tenantHolder, final ActiveEdition previousActiveEdition,
			final ActiveEditionDto activeEditionDto)
	{
	    ...
	}
```

The supported method signatures are:
* `<NewEntity>` create(...) | `<NewEntity>` create(`<Identifier>` id, ...)
* `<NewEntity>` update(`<OldEntity>` oldValue, ...) | `<NewEntity>` update(`<Identifier>` id, `<OldEntity>` oldValue, ...)
* `void` delete(`<OldEntity>` oldValue, ...) | `void` delete(`<Identifier>` id, `<OldEntity>` oldValue, ...)

##### Important Notes
* The order of parameters to the `create` | `update` | `delete` methods is not important.
* The name of the method is not important. The signatures above use `create` | `update` | `delete` for the method names in order to easily identify them.
* The name of the parameters `id` | `oldValue` could be overridden by providing them in the `@AuditConfigurationChange` annotation.
* `<NewEntity>` | `<OldEntity>` should be **java objects**, and not just simple types like `String` or `Array`, for example.
* `<Identifier>` type should implement `toString` method used to extract its value.
* `<Identifier> id` is an optional parameter and when not provided, the object identifier will be extracted, if possible, from a member of the `<NewEntity>` object that is annotated with `javax.persistence.Id` annotation. The `Id` member should also implement `toString` method.
* In the case when `<OldEntity>` is not found in the method's parameters or when `<NewEntity>` is not required as is the case of `delete`, their values default to `Object` instance.
* In the case when `<NewEntity>` is required and its value is null, as is the case of `create` | `update` an `IllegalArgumentException` is thrown.   
* The actual audit logging is done by the `AuditLogger` API under the hood.

#### Usage - Security Event
The `AuditLogger` API provides a simple method for auditing security events.

##### Java API
```java
	/**
	 * Log a security event audit message.
	 *
	 * @param securityEventData the security event data to write to the audit log
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logSecurityEventAuditMessage(@Nullable Object securityEventData);
```

##### Example
```java
public boolean logIn(final String tenant, final String email, final String password)
	{
		final User user = userRepository.findByEmail(user);
		final boolean authenticated = userAuthenticationService.authenticate(user, password)

        final LoginSecurityEvent = new LoginSecurityEvent(user, authenticated);
		auditLogger.logSecurityEventAuditMessage(loginSecurityEvent);

        return authenticated;
	}
```

##### Annotation-Driven
`AuditSecurityEvent` annotation should be used to mark methods that require security event logging.

The annotation support is handled through `SecurityEventAspect` aspect providing after returning advice for the annotated methods.
The advice will automatically attempt to log a security event audit message with the method's return value.

##### Example
```java
	@AuditSecurityEvent
	public LoginSecurityEvent logIn(final String tenant, final String email, final String password)
	{
	    final User user = userRepository.findByEmail(user);
		final boolean authenticated = userAuthenticationService.authenticate(user, password)
        return new LoginSecurityEvent(user, authenticated);
	}
```

##### Important Notes
* In the case when the annotated method's return type is `void`, then an `IllegalArgumentException` is thrown.
* The actual audit logging is done by the `AuditLogger` API under the hood.

#### Usage - Data Access
The `AuditLogger` API provides two methods for auditing data access.

The first method requires the developer to pass a DataAccessObject and the type of the object being accessed.
This allows for fine grained control over which attributes are accessed.

##### Java API
```java
	/**
	 * Log a data access audit message.
	 *
	 * @param objectId    the identifier of the object being audited
	 * @param objectType  the type of the object being audited
	 * @param object      the attributes and/or attachments being accessed
	 * @param dataSubject the owner of the data being accessed
	 */
	void logDataAccessAuditMessage(final String objectId, final String objectType, final DataAccessObject object, final DataSubject dataSubject);
```

##### Example
```java
public User getUserDetails(final String userAccountId)
	{
		final User user = userRepository.findById(userAccountId);

		auditLogger.logDataAccessAuditMessage(userAccountId, "User",
				DataAccessObject.build().addAttributes(true, "accountId", "firstName", "lastName", "role"),
				DataSubject.of(userAccountId, Role.MERCHANT));

		return user;
	}
```

The second method involves simply passing the accessed object to the parameter. The object type is inferred from the object itself.
A `read` boolean is used to determine if the passed object was read or not.

##### Java API
```java
        /**
	 * Logs a data access audit message.
	 * The object type is inferred from the {@code DataAccessObject}
	 *
	 * @param objectId    the identifier of the object being audited
	 * @param object      the object being accessed
	 * @param read		  boolean indicating whether the object was successfully read or not.
	 * @param dataSubject the owner of the data being accessed
	 */
	void logDataAccessAuditMessage(final String objectId, final Object object, final boolean read, final DataSubject dataSubject);
```

##### Example
```java
public User getUserDetails(final String userAccountId)
	{
		final User user = userRepository.findById(userAccountId);

		auditLogger.logDataAccessAuditMessage(userAccountId, user, true, DataSubject.of(userAccountId, Role.MERCHANT));

		return user;
	}
```

#### Usage - Data Modification
The `AuditLogger` API provides a simple method for auditing personal data modification.

##### Java API
```java
	/**
	 * Log a data modification message.
	 *
	 * @param objectId         the identifier of the object being audited
	 * @param objectType       the type of the object being audited
	 * @param dataChangeObject the old and new values of the object bing audited
	 * @param dataSubject      the owner of the data being accessed
	 * @param auditMode        the audit mode
	 * @throws AuditLoggingException when the logger is unable to process the audit log
	 */
	void logDataModificationAuditMessage(String objectId, String objectType, DataChangeObject dataChangeObject, DataSubject dataSubject, Mode auditMode);
```

##### Example: Data Update
```java
public User updateUser(final User user, final UserDto userDto)
	{
		final DataChangeObject dataChangeObject = DataChangeObject.withOldValue(user);

		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setRole(userDto.getRole());

		auditLogger.logDataModificationAuditMessage(user.getId(), User.getClass().getSimpleName(),
				dataChangeObject.setNewValue(user), DataSubject.of(user.getId(), Role.MERCHANT), Mode.FULL);

		userRepository.save(user);
		return user;
	}
```

##### Example: Data Creation
```java
public User createUser(final UserDto userDto)
	{
		final User user = new User();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setRole(userDto.getRole());

		final User savedUser = userRepository.save(user);

		auditLogger.logDataModificationAuditMessage(savedUser.getId(), User.getClass().getSimpleName(),
				DataChangeObject.withNewValue(savedUser), DataSubject.of(savedUser.getId(), Role.MERCHANT), Mode.FULL);

		return user;
	}
```

##### Example: Data Deletion
```java
public void deleteUser(final String userId)
	{
		final User user = userRepository.findById(userId);
		auditLogger.logDataModificationAuditMessage(userId, User.getClass().getSimpleName(),
				DataChangeObject.withOldValue(user), DataSubject.of(userId, Role.MERCHANT), Mode.FULL);

		userRepository.delete(user);
	}
```

#### Usage - Other...
If the `AuditLogger` APIs are not suitable for your use case, please open a pull request for Polar Bears to review or ask for enhancements.

#### Useful Link
More information about audit logging with SAP java client v2:

    https://github.wdf.sap.corp/xs-audit-log/audit-java-client
    https://github.wdf.sap.corp/xs-audit-log/audit-java-client/wiki/Audit-Log-V2

[1]:https://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
