# SAP CX - CaaS Spring Boot Starters
---

# CaaS Modules
* [caas-spring-boot-starter-error-handling](caas-spring-boot-starter-error-handling/README.md)
* [caas-spring-boot-starter-logging](caas-spring-boot-starter-logging/README.md)
* [caas-spring-boot-starter-kafka](caas-spring-boot-starter-kafka/README.md)
* [caas-spring-boot-starter-multitenant](caas-spring-boot-starter-multitenant/README.md)
* [caas-spring-boot-starter-security](caas-spring-boot-starter-security/README.md)
* [caas-spring-boot-starter-swagger](caas-spring-boot-starter-swagger/README.md)
* [caas-spring-boot-starter-web](caas-spring-boot-starter-web/README.md)
* [caas-spring-boot-starter-test](caas-spring-boot-starter-test/README.md)
* [caas-spring-boot-starter-dependencies](caas-spring-boot-starter-dependencies/README.md)
* [caas-spring-boot-starter-client](caas-spring-boot-starter-client/README.md)
* [caas-spring-boot-starter-data](caas-spring-boot-starter-data/README.md)

# Release Notes

# Version 4
* Requires JDK 11
* Requires spring-boot 2.3.x+

## 4.2.4
* Add support for generating a token with an email address in `caas-spring-boot-start-test` `CaasJwtToken` 

## 4.2.2
* Fix issue with swagger file not loading through the tenant

## 4.2.1
* Spring kafka `security.protocol` property moved

## 4.2.0
* CAAS-25934 - Provides default JPA configuration for eclipse link.
* CAAS-25934 - Provides entity, dto and converter for auditing. More details can be found in [caas-spring-boot-starter-data](caas-spring-boot-starter-data/README.md)

## 4.1.2
* CAAS-25661 - Extract the b3 headers and set the right tracing context when processing Kafka message from batch listener.
* CAAS-26093 - Support multiple swagger documents for one service.

## 4.1.0
* Caas Kafka supports configurable `topicPrefix` for short delay, long delay and dead letter topics for retryable consumer / listener.

## 4.0.3
* Updated outdated `caas-spring-boot-starter-test/src/main/resources/env-vars.yml` file, alternative to `caas-spring-boot-starter-test/src/main/resources/test.properties`
* Add custom error handler to be called when the listener throws an exception.

## 4.0.2
* Fixes in DefaultAuthorizationManager
* Update to spring boot 2.3.4

## 4.0.1
* Improve the OAuth token scope verification using the spring security context to ensure that the requested scopes are present.

## 4.0.0
* Resolve usage of deprecated APIs
    * brave - `ExtraFieldPropagation` moved to `BaggageField` and `BaggagePropagation`
    * brave - `StrictCurrentTraceContext` moved to `StrictScopeDecorator`
    * spring-kafka - `AfterRollbackProcessor#process` requires additional `EOSMode`
    * spring-kafka - `ContainerProperties#setAckOnError` is moved to `GenericErrorHandler#isAckAfterHandle`
    * spring-kafka - `DefaultRollbackProcessor#setCommitRecovered` moved to constructor
    * spring-kafka - `DefaultRollbackProcessor#setKafkaTemplate` moved to constructor
    * spring-kafka - `org.springframework.kafka.support.SeekUtils` moved to `org.springframework.kafka.listener.SeekUtils`
    * junit - `org.junit.Assert.assertThat` changed to `org.hamcrest.MatcherAssert.assertThat`
    * mockito - `org.mockito.Mockito.verifyZeroInteractions` moved to `org.mockito.Mockito.verifyNoInteractions`

* Remove all deprecated code
    * Replace all `caas.kafka.binding` with `caas.kafka.producer` properties
    * Removed deprecated `BatchHelper` methods
    * Removed deprecated `@EnableAuditLogSupport`

* Remove audit logging mode to always use FULL by default. This is due to changes in the underlying library that makes the DIFFs unreliable.
    * Removed `AuditConfigurationChange#mode`
    * Removed parameter `mode` from `AuditLogger#logConfigurationChangeAuditMessage`
    * Removed parameter `mode` from `AuditLogger#logDataModificationAuditMessage`
    * Removed class `Mode`

* Remove caas-spring-boot-starter-test environment variable injection
    * Removed class `CloudFoundryEnvironmentVariables`
    * Removed class `EnvironmentVariableInjector`
    * Removed class `EnvironmentVariables`
    * Removed VCAP_SERVICES injection in `TestConfig` to `test.properties`

* CAAS-22833: 
    * Upgrade to new open source version of xsuaa-spring library
    * Remove dependency to deprecated `spring-security-oauth2` in favour of spring security 5 OAuth2 resource server 
authorization and `spring-security-oauth2-client` for Kafka to OAuth2 client to obtain a token at startup.
    * Introduce `CaasWebSecurityConfigurerAdapter` as an extensible spring security base class; see [caas-spring-boot-starter-security](caas-spring-boot-starter-security/README.md)

# Version 3

## 3.42.0
* Add a configurable recoveryCallback to the kafkaListenerContainerFactory.
* Configurable `HttpClient`, used  underneath the `CaasWebClient`, with a `defaultHttpClient`. 
* Added `HttpClientCustomizer` for customizing the `httpClient`.

## 3.41.0
* CAAS-24789 - Add an encryption converter for secure storage of sensitive data in database using AES.

## 3.40.0
* CAAS-22495: Improve error message for field name in detailed error schema.
  Move InvalidEnumValueException and it's converter to the starter.
  Many services are already using this exception InvalidEnumValueException and defined their converter component.
  This might conflict with InvalidEnumValueExceptionConverter, if it's the case you must remove this converter 
  to use this generic converter.
* CAAS-24104: Support batch configuration and enhance MessageAssembler to accept additional headers (refactoring)               
* CAAS-24228: Swagger not loading on Safari
* CAAS-24210: Update gradle wrapper to 6.6
* CAAS-22624: Support POST with exchange method using WebClient using ParameterizedTypeReferences

## 3.39.0
* CAAS-22624: Support POST method using WebClient using ParameterizedTypeReferences
* CAAS-21902: Support for lenient and wildcard configuration in CaasLocaleResolver
* CAAS-21902: Support for @AcceptLanguageHeader validation
* CAAS-23911: Fix CVE-2019-3795 CVE-2020-13934 CVE-2020-13935
* Update to spring boot v2.3.2.RELEASE
* Update to spring cloud Hoxton.SR6
* CAAS-24104: Support batch configuration and enhance MessageAssembler to accept additional headers
* CAAS-24178: Make `AuthorizationManager` a bean and add `hasAnyAuthority(String...)` method to check against multiple possible scopes

## 3.38.11
* CAAS-22766: Updated to Java 8 date & time

## 3.38.10
* CAAS-22916: Removed deprecated methods in Gradle
* CAAS-23551: Support PUT method using WebClient exchange for REST calls with the CaasWebClient.

## 3.38.9
* CAAS-23248: Support POST method using WebClient exchange for REST calls with the CaasWebClient.
* CAAS-23248: Support DELETE method for REST calls with the CaasWebClient.

## 3.38.8-SNAPSHOT
* CAAS-22751: Moved TenantLock from application to starters

## 3.38.7
* CAAS-22813: Update starters to accept and validate locales with script component
* Improvements to Jackson exceptions converters to include the location of the error in the more info error attribute.
* CAAS-22673: added ValidatorUtil class.
* CAAS-22273: Implementation of a new Exception converter for Jackson JsonMappingException.
* CAAS-22281: Added support for a beta-mark for Swagger

## 3.38.6
* CAAS-21454: Add GeoBlocking exception. Services can throw this exception when a resource resides on a geo blocked location.
* CAAS-22617: Fix root cause based exception conversion bug and force `application/json` to be present in the `accept` request header.

## 3.38.5
* CAAS-22659: Add support to starters for loading translation files using script based locales (i.e. messages_sr_Latn_RS.properties)

## 3.38.4
* CAAS-22151: JPA list to array converter

## 3.38.3
* CAAS-22020: Support PUT/PATCH methods for REST calls with the CaasWebClient.

## 3.38.2
* CAAS-20892: Support PUT/PATCH methods for REST calls with the CaasWebClient.

## 3.38.1
* CAAS-21900: Support additional headers for REST calls with the CaasWebClient.

## 3.38.0
* CAAS-21904: Login fails due to missing Accept-Language header on Chrome
There has been a new property introduced `caas.i18n.locale-resolver.enabled=true` that allows a service to control if a localeResolver bean should not be defined (by default it is set to true to define the localeResolver bean).
* Create `TenantUtils` to validate tenant identifiers

## 3.37.0
* CAAS-21850: Upgrade dependencies: `com.sap.hcp.cf.logging:cf-java-logging-support-logback:3.0.8`, `com.sap.cloud.sjb:xs-env:1.8.10`
`com.sap.cp.auditlog:audit-java-client-api:2.1.3`, `com.sap.cp.auditlog:audit-java-client-impl:2.1.3`.

## 3.36.0
* CAAS-21838: Upgrade dependency `com.sap.cloud.security.xsuaa:java-container-security:3.12.0`.

## 3.35.6
* CAAS-21569: Add content_language header for Kafka messages

## 3.35.4
* CAAS-21033: UUID and HStore JPA converters for EclipseLink and Hibernate. To use them, make your JPA Configuration extend `CaaSJpaBaseConfiguration`, which includes starters package for the component scanning.

## 3.35.3
* CAAS-20218: Implementation of a new Exception converter for Jackson InvalidTypeIdException.

## 3.35.2
* CAAS-20545: Introduce BusinessException in starter-error-handling to handle business requirement validation error.
* CAAS-20908: Avoid leaking implementation details through error messages while handling Jackson JsonParseException and MismatchedInputException exceptions

## 3.35.1
* CAAS-20823: Expose api to get translated labels for all locales in starters
* CAAS-20825: Add a policy validator for @SafeHtmlPolicy that can handle Maps
* CAAS-20860: Add validator for locales of localizable map based properties

## 3.35.0
* CAAS-20711: Change prod-euC1 profile name to lowercase (prod-euc1) & paas tenant name to caaseu
* CAAS-20738: Update rds-combined-ca-bundle to latest
* CAAS-20817: Define locale regex for validating locale values for multi language fields

## 3.34.3
* CAAS-20655: Move message bundles loading code to starters
* CAAS-20364: Add maximum value for the kafka messageDelayMs property
* CAAS-20711: Add tenant regex for prod-euC1 profile
* CAAS-20781: Enhance custom locale resolver to handle star wildcard for Accept-Language header

## 3.34.2
* CAAS-20446: Change missing bean condition for default localeResolver bean

## 3.34.1
* CAAS-20446: Make consistency changes for accept-language header and locale format (w dash)

## 3.34.0
* CAAS-20218: Implementation of a new Exception converter for MissingRequestHeaderException
* CAAS-20050: Investigate and address the disconnect between the documentation in place and actual behaviour for default kafkaListenerContainerFactory and enhancements to consumer as well as retryable consumer

## 3.33.8
* CAAS-20034: Fix vulas issues

## 3.33.7
* CAAS-19957: Support RetryTemplate for kafka retryable consumer

## 3.33.6
* CAAS-19836: Spring CaasWebClient dependency management improvement

## 3.33.4 
* Nothing added.

## 3.33.3
* CAAS-19316: Support multiple delayed retries and DLQ with Kafka
* CAAS-17301: Publish caas starters to artifactory and remove xmake from build and deploy
* CAAS-17929: Remove PageSize and PageNumber ModelAttributes (The parameters pageSize and pageNumber should be injected into Spring MVC controller by specifying the `Pageable` type in the method signature)

## 3.32.1
* CAAS-19417: Remove dependency on VCAP_APPLICATION for audit logging
* CAAS-19033: Move Spring CaasWebClient into starters

## 3.32.0
* CAAS-19364: Update to SpringBoot 2.2

## 3.31.2
* CAAS-19032: Set complete path in the the error message details field in the validation error

## 3.31.1
* CAAS-18513: Ensure propagation of tracing information (correlation_id)
* CAAS-18853: Fix Vulas issues CVE-2019-16942, CVE-2019-16943, CVE-2019-17531, CVE-2019-16869, CVE-2019-17359

## 3.31.0
* CAAS-18376: Migrate to Java CFEnv from spring cloud connectors

## 3.30.5
* CAAS-16218: Exception handling optimisation for Jackson related exceptions
* CAAS-17841: Upgrade Rest Assured to 4.1.1, Awaitility to 4.0.1, Spring Cloud to Greenwich.SR3, and Hamcrest to 2.1
* CAAS-18601: Added KeyMultiValues annotation and corresponding converter to disable the conversion of request parameter from comma delimited string to a collection.

## 3.30.4
* Introduce caas-spring-boot-starter-dependencies to product a BOM for the caas starters

## 3.30.3
* CAAS-17285: Audit log can not be retrieved via API or audit log viewer

## 3.30.2
* Introduce the UpdateConflictException in starter-error-handling.
* CAAS-17276: Upgrade SAP application logging library & adapt MDC attributes: tenant_id, correlation_id 

## 3.30.1
* CAAS-16827: Introduce more utility methods for string and list manipulation in the controllerUtils.
* CAAS-16743: Improve the UriLengthValidationInterceptor to support an annotation member maxLength.
* CAAS-17073: Fix vulas issues CVE-2018-17196, CVE-2019-10072, CVE-2019-11269, CVE-2019-12384, CVE-2019-12814 & CVE-2019-12086.

## 3.30.0
* CAAS-16402: Add support for resource identifiers best practice
* Introduce the ParameterSizeExceededException and ResourceNotFoundException in starter-error-handling
* CAAS-15976: Add support for `@Transactional` at the type for JPA tenant injection

## 3.29.0
* CAAS-13852: Added RequiredScope annotation and corresponding custom introspector
* CAAS-13852: Added RequiredScopeIntrospector to the jackson configuration as a default introspector
* CAAS-15931: Omit starter binary dependencies from Fortify

## 3.28.0
* CAAS-15803: Created Kafka MessageAssembler
* CAAS-15887: Fix vulas issue detected CVE-2019-3802

## 3.27.1
* CAAS-15380: Fix vulas issue detected CVE-2019-3797, CVE-2019-3795 and CVE-2019-0232
* CAAS-15517: Deploy starters artefacts to Nexus

## 3.27.0
* Update to swagger-ui v3.22.1
* CAAS-15117: Add `x-csrf-token` header for swagger requests.

## 3.26.2
* CAAS-15156: Introduce a ControllerUtils class with an utility method (filterIdentifiers) to remove invalid identifiers.

## 3.26.1
* CAAS-15048: Fix security configuration conflict with multiple `ResourceServerConfigurerAdapter` to remove authentication requirement by default in security-starter.

## 3.26.0
* CAAS-14994: Add support for `WrappedCollection<BatchRequest>` batches to be injected into Spring controllers.

## 3.25.2
* CAAS-14446: Improve sorting in starter-web to map the property name to its internal or database column name.
* CAAS-13312: Upgrade 'swagger-ui' to '3.18.3', this breaks certain $ref behavior- see the swagger read.me for details.

## 3.25.1
* CAAS-14199: Upgrade `spring-security-oauth2` to `2.3.5` to fix security vulnerability identified by vulas
* CAAS-13805: Added support for catching foreign key constraint exceptions in the `exceptionToMessageWrapper`

## 3.25.0
* CAAS-12982: Generic support for paging and sorting in starter-web

## 3.24.2
* CAAS-13732: Fix exception logging issue
* CAAS-13280: HP Fortify issue fi
* CAAS-13770: Consolidate integration tests

## 3.24.1
* CAAS-13754: Enable multitenant filter only for `Bearer` tokens
* CAAS-13287: Clean up `caas-spring-boot-kakfa` testing
* CAAS-13218: Fix flaky test

## 3.24.0
* CAAS-13493: Upgrade to spring boot 2.1 version to support JDK 11 with backwards compatibility with JDK 8
* CAAS-11585: Review & change logging levels in starters & services and make sure they are appropriate for the context

## 3.23.1
* CAAS-13422: Simplify default Kafka consumer configuration to ensure ack on error and prevent infinite redelivery and lost messages

## 3.23.0
* CAAS-13144: Log exceptions during `BatchRequest` processing to `INFO` when the exception would map to a status code < 500 
* CAAS-13217: Whitelist environment variables that can be set from the `EnvironmentVariableInjector` in starter-test

## 3.22.2
* CAAS-12921: Fix jackson databind vulas vulnerabilities by upgrading to version 2.9.8
* CAAS-13064: Introduce the TooManyRequestException in starter-error-handling

## 3.22.1
Re-release of `3.22.0`: version `3.22.0` artifacts failed to be uploaded to repository.hybris.com

## 3.22.0
* CAAS-13214: Upgrade SAP java-container-library and xs-env versions
* CAAS-13201: Remove lombok from the caas-spring-boot-starters repository altogether
* CAAS-13200: Remove dependency on gson, apache-commons-lang3 & spring-boot-autoconfigure in starter-error-handling
* CAAS-13191: Remove dependency on spring-hatoas, lombok, & spring-boot-autoconfigure in starter-swagger
* CAAS-13198
  * Remove dependency on guava, lombok, commons-compress & spring-boot-autoconfigure in starter-web
  * CAAS-13198: Remove `BatchResponseRequestDto.builder` in favour of `BatchResponseRequestDto.of`


## 3.21.2
* CAAS-12898: Support error translation for exceptions thrown in servlet filters.
* CAAS-11750: Support PostgreSQL exception mapping to context relevant message (i.e. 409 - There is already a product with the same SKU.) 

## 3.20.1
* Deprecated `CaasKafkaProperties#binding`, use `CaasKafkaProperties#producer` and `CaasKafkaProperties#consumer` instead.  
* Configure dead letter publishing error handler for kafka consumer & listener
* Configurable properties for retry template for kafka consumer & listener

## 3.20.0
* Update `CaasKafkaProperties` to validate consumer/listener topic names.
* Alias `rollbackFor` in `ChainedKafkaTransactional` annotation. 

## 3.19.0
* CAAS-12361: Support path-based tenant regular expressions for extraction by `TenantService`.

## 3.18.0
BatchHelper processBatch overloaded to support functions to map headers and http status code, based on the processor return value.

## 3.17.6
* CAAS-12068: Rename Kafka topicSuffix property to environmentName

## 3.17.5
Downgrade swagger-ui to v3.4.4 for example $ref to work

## 3.17.4
* CAAS-11532: Bug fix to suppress audit logging errors when audit service is disabled.
* Add @ChainedKafkaTransactional annotation and corresponding aspect to close producers on un-recoverable exceptions.
* TransactionTenantSetterAspect supports meta annotations based on @Transactional.

## 3.17.3 / 3.17.2 / 3.17.1
Improvements and changes related to CAAS-11549.

## 3.17.0
CAAS-11549: Introduce the new `caas-spring-boot-starter-kafka` to auto configure the Kafka producer, consumer, topics and transaction manager.

## 3.16.1
Removed the constants for the custom Kafka headers (to be added in a separate kafka starter).

## 3.16.0
* Map HeuristicCompletionException (for chained transaction management) to the corresponding http response.
* Defined constants for the custom Kafka headers.

## 3.15.7
CAAS-11209: Release slack notification of our starters is moved from caas-polarbears channel to tech_alerts channel and following the release notification template.

## 3.15.6
* CAAS-10830 Enhance the application logs by appending Zipkin headers.
* CAAS-9807: Upgrade of swagger-ui from 3.4.4 to version v3.18.2.

## 3.15.5
This version should never be used as the swagger console is not working.

## 3.15.4
CAAS-10729: Fix sonar critical issues.

## 3.15.3
CAAS-10812: Additional converter to convert the RequestRejectedException to a 400.
CAAS-10580: Improve error message 400 and update converter in multitenant module.

## 3.15.2
CAAS-10920: Check why secured end-points cannot be accessed locally (401 returned)

## 3.15.1
CAAS-10574: Removed URL sanitization in TenantTracingPropagationFilter implemented in CAAS-9947
CAAS-10586: Allow special characters to pass on HTML validation

## 3.15.0
CAAS-10451: Source clear vulnerability issues.
CAAS-10645: Provide a mechanism for the service to override the TenantService/UserProvider beans.

## 3.14.1
CAAS-10573: Tenant extract from x-forwarded-host header is now being converted to lowercase using an english locale
CAAS-10417: [Fortify - Zip Upload - content-repository] Additional check is now done on the extension of the uploaded zip file to make sure it is acceptable for use by the application.

## 3.14.0
CAAS-10453: Extract common token generation code to test starter.

## 3.13.7
CAAS-10149: Fixes the bug where a previous tenant was being logged when a tenant was not provided in the x-forwarded-host header

## 3.13.6
* Add explicitily versions in modules for fixing source clear vulnerabilities.  

## 3.13.5
* CAAS-10118: Upgrade `com.sap.xs2.security:java-container-security` to version `0.30.3`

## 3.13.5
* CAAS-9947: Prevents log forging from tenant tracing filter.

## 3.13.4
* CAAS-10011: Enhanced `TenantService` to check if the active tenant is the configured PaaS tenant.

## 3.13.2
* CAAS-9857: CORS: Update default policy to expose Content-Disposition header

## 3.13.1
* CAAS-9774
  * Bugfix to delegate to `JacksonAnnotationIntrospector` when `AuditLoggingIntrospector` fails to process an attribute.
  * Bugfix to support any `Object` in `NonAuditableField`.
* CAAS-9806: Bugfix to catch and log missing/invalid tenant information exceptions in the `TenantTracingPropagationFilter`.

## 3.13.0
* CAAS-9350: End-to-end tracing via support for propagating original SAP `X-Correlation-Id` from AppRouter in tracing context and MDC context.

## 3.12.1
* CAAS-9736
  * Bugfix for tenant & user tracing context injector filters.
  * Remove required `@EnableAuditLogSupport` annotation and rely on auto-configuration.
  * Auto-configure `ServiceMultitenantConfig` and split it from the `JpaMultitenantConfig`, which still requires `@EnableMultitenantSupport` to activate.

## 3.12.0
* CAAS-8988: Introduced client IP into tracing context.
* CAAS-8989: Add simplified audit logging API `AuditLogger` for data access and data modifications.

## 3.11.1
* Added new exception converter `HttpMessageConversionExceptionConverter`, which returns 400 Status code when `HttpMessageConversionException` occurs.

## 3.11.0
* Inject client IP address into tracing context.
* Add extractor method for client IP to `UserProvider`, which retrieves it from the tracing context.
* Protect applications from deploying with unbound audit service in Cloud Foundry and logging audit message to the console. See `caas-spring-boot-starter-logging/README.md` for details.

## 3.10.0
* Introduce Spring Sleuth tracing
* Inject user account id into trace context
* Inject tenant into trace context
* Inject SAP logging context into MDC at trace integration points
* Enhanced audit logging support with specific audit annotations and sync/async logger
* Extract user account id and tenant from trace context for usage in audit logging 
* Add simplified audit logging API `AuditLogger` for configuration changes and security events

## 3.9.3
* CAAS-8992: Fixed Fortify issue.

## 3.9.2
* Increased testing code coverage.

## 3.9.1
* Added unit testing for GlobalExceptionHandler and fixed codes smells.

## 3.9.0
* Added support for less restrictive `JavaScript`-like output encoding via `QuoteEncoder`.

## 3.8.0
* Added new exception converter `MultipartExceptionConverter` for when multipart resolution fails to map to 400 - BAD REQUEST.

## 3.7.0
* Added new validator `ZipFile` for `MultipartFile`, which validates that the contents of the file represent a valid zip file.

## 3.6.1
* Preserve batch request order in argument resolver

## 3.6.0
* Added new validator `FilenameSize` for `MultipartFile`, which validates the length of the original filename.

## 3.5.0
* Added new exception converter `MaxUploadSizeExceededExceptionConverter` for file upload max size exceeded to map to 400 - BAD REQUEST.

## 3.4.0
* Added new custom validator and its annotation interface to validates the length of the json object `ObjectNodeMaxSize`.

## 3.3.0
* Added new exception class `PathSegmentConstraintViolationException` and its corresponding converter `PathSegmentConstraintViolationExceptionConverter`.

## 3.2.0
* Added support for output encoding Java `Properties` keys and values via `PropertiesEncoder`.

## 3.1.0
* Added support for configurable static paths in the swagger starter

## 3.0.0
* Moved to SpringBoot 2<br>
  Please refer to these guides to migrate your projects<br>
  https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide<br>
  https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-Security-2.0

# Version 2

## 2.6.0
* Add support for sort parameter argument resolver for injecting the spring data `Sort` object directly into Spring controllers.

## 2.5.0
* Ignore exceptions in managed beans' methods with `@IgnoreException` via the `IgnoreExceptionAspect`

## 2.4.4
* Fix bug in `BatchRequestArgumentResolver` to properly support validation groups.

## 2.4.3
* Minor enhancement to `@Identifier` validation annotation to support `min` and `max` attributes.

## 2.4.1
* Minor enhancement to `BatchRequestArgumentResolver` to support validation groups.

## 2.4.0
* Added support for Audit Logging to standard output via the `CaasAuditLogMessageFactory`.
* Fixed a bug with `BatchRequestArgumentResolver` which attempted to validate a null DTO after failing JSON conversion. 

## 2.3.3
* Make `BatchHelper` more generic by making the HTTP status a parameter. Some older methods have been deprecated in this class as well.

## 2.3.0
* Added support for tenant injection via `TenantHolder` and `@Tenant` argument resolvers for injecting tenant into Spring controllers.
* Enhanced tenant validation to ensure that tenant from JWT token and from `X-Forwarded-Host` header match when both are present.

## 2.1.13
* Added support for formatting 404 error message as per CaaS format from the standard spring format. 

## 2.1.3
* Added converters for `@CompletionException` and `@ExecutionException` concurrent classes.

## 2.0.0
* Remove deprecated `@EnableYaasErrorHandling` class from error-handling starter.
* Remove `Yaas` prefix from all classes that used it.
* Rename `@YaasException` to `@WebException`

## 1.0.0
* Updated logging starter to enable SCP logging and removed YaaS specific logging features.
 

**Important:** 
* Please note that if the web starter is not being used, configuration will need to be added within the service to disable content negotiation based on query parameter or path extension as done in the `WebConfig` class from the web starter to properly handle error cases when an URL such as `../dummy/dummy.html` it is being used.

**Exceptions:**
* There are no changes in the format when non-existing file/path is requested under `/api/` endpoint (standard spring error message it is still being used). 
* When Accept header contains a value that is not supported, than only the error status code is returned.

# Previous YaaS starters Release Notes

The YaaS starter projects were renamed to CaaS starters

## 2.0.0
* Clean up of the starters by removing code not required for SCP (i.e. Basic Authentication, YaaS Headers support). Please note that this release is not backwards compatible.

## 1.1.1

### caas-spring-boot-starter-error-handling

#### Tasks
* Change privilege to public for convert method of the `ConstraintViolationExceptionConverter` as an unrelated class needs to access it.
* Support for throwing handling some exception by examing the root cause.
* Add exception mapping for eclipse link `DatabaseException`
* Add exception mapping for solrj `RemoteSolrException`
* Add exceptions mapping for spring JPA `JpaOptimisticLockingFailureException`, `EmptyResultDataAccessException`
* Update the `PayloadMalformedException` to propagate the cause.
* Deprecated the `@EnableYaasErrorHandling` annotation which is enable via auto configuration.
* Add exception mapping for spring security `AccessDeniedException`.
* Introduce new `PayloadMalformedException`.

#### Bugs
* The constraint violation must be able to display the field name when the name of the field is at the root path element.

### caas-spring-boot-starter-security

#### Tasks
* Bug fix to for `@SafeHtmlPolicy` to support null string values.
* Add support for HTML sanitization policy based input validation.
* Add support for Jackson serialization based HTML sanitization for output data.
* Enable global method security by default. This is more consistent with spring-security default behaviour.
* Add support for multiple sets of basic auth credentials through properties.
* Add integration tests to verify security related configurations.
* Make global method security configurable based on property. This enables/disables controller authorization based on @PreAuthorize annotations.
* Remove Jackson output encoding serializers since output encoding should be done within context (workbench/mobile app).
* Externalize basic auth configuration through list of permitted URLs.
* Enable CORS for PATCH verb.
* Preconfigured spring security with Basic Auth enabled.
* Injection of YaaS scopes int osecurity context to enable declarative authorization.
* Injection of security related HTTP response headers.
* Automatic content-type negotiation to application/json.
* Enable hibernate-based input validators.
* Add basic jackson serializers for output encoding.

### caas-spring-boot-starter-swagger

#### Tasks

* Automatically redirect `/` to the swagger-ui console at `/api/index.html`.
* Externalize configuration of base-path and OAuth URLs.
* Document api-description configuration
* Document security configurations
* Document basic troubleshooting guide.
* Downgrade swagger-ui version to `3.0.7`, due to bug in latest version.
* Critical update to README.md file to document proper path to api-description path.
* Introduce swagger-ui version `3.0.12` as static resources for spring-boot applications.

## Vulas

The vulas tasks provided are:

`vulasA2C` Creates a method-level bill of material of an application and all its dependencies.<br>
`vulasApp` Builds a call graph (starting from app methods) and checks whether vulnerable code is reachable.<br>
`vulasClean` Cleans the analysis data of a single app in the backend.<br>
`vulasReport` Downloads analysis data from the backend to the client, produces a result report (Html, Xml, Json), and throws a build exception in order to break Jenkins jobs.<br>

The vulas tasks run in CI environment are as follow
    
    gradle vulasApp vulasA2C vulasReport -Pvulas.core.space.token=<VULAS_TOKEN>

The vulas frontend is 

    http://vulas.mo.sap.corp:8080/apps 
    
It's a shared resource in SAP. To filter only CaaS team's result, we can click the gear sign ⚙️ (close to bottom left corner) to filter only `CAAS20` space's scan.

## Configuration for a support branch.

In order to release from a support branch, you need to create two branches in git remote. These branches will become the new `master` and `develop` for that particular version. 

The support branch must be created from the last tag on the version that you want to support, and you have to modify the release plugin in `build.gradle` to refer the branches.
 
For example, if you want to support the major version 3, the last tag for the version `3` is `3.38.0`. 

* Create a branch to act as master` from the last tag:

```
git checkout 3.38.0 support/3.x
git push origin support/3.x
```

* Configure the release plugin `io.github.robwin:jgitflow-gradle-plugin` in `build.gradle`. This change must be commited.
```
initJGitflow 
{
    develop = '3.x' 
    master = 'support/3.x' 
}
```
* Create a branch that will act as `develop`
```
git checkout support/v3.x 3.x
git push origin 3.x
```
