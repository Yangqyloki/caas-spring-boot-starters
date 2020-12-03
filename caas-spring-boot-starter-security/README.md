# SAP Hybris - CaaS Spring Boot Starter for Security
---
Provides security related features.

## Features
___
* Extensible `WebSecurityConfigurerAdapter`.
* Programmatic authorization verification.
* Global CORS support.
* Default application/json content type negotiation.
* Jsoup support for hibernate-based input validation.
* HTML sanitization support.
* Set X-Forwarded-Prefix from X-Forwarded-Path
* Custom encoders
* User injection into tracing context
___

### Enabling CaaS Security
In order to enable the CaaS security, you simply need to add the `com.hybris.caas:caas-spring-boot-starter-security` dependency to your build. All configurations will be done automatically via the spring auto-configuration process.

NOTE: By adding this starter to your classpath, it will also add the `spring-boot-starter-security` to your classpath. This will enable the spring security auto-configuration.

### Extensible `WebSecurityConfigurerAdapter`
With Spring Security 5, OAuth resource servers should be secured via the `WebSecurityConfigurerAdapter` class. This package defines a default class
to use that will set some basic configuration for you. The `CaasWebSecurityConfigurerAdapter` will do the following:
* Stateless session management
* OAuth2 resource server with JWT authentication provider
* Custom authentication entry point
* Custom access denied handler
* Sets frame options to "same origin" in HTTP response header
* Disable CSRF
* Register CORS request filter

To keep this default security configuration but extend it by providing your own `HttpSecurity` configuration, you must declare beans
of type `ServiceWebSecurityConfigurerAdapter`.
```java
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MySecurityConfig extends ServiceWebSecurityConfigurerAdapter
{
	@Override
	public void configure(HttpSecurity http) throws Exception
	{
		http.authorizeRequests()
                .antMatchers("/security/authenticated").fullyAuthenticated()
                .antMatchers("/security/authorized").hasAuthority("read_only")
                .antMatchers("/security/public").permitAll()
                .antMatchers("/**").denyAll();
	}
}
```
_NOTE_
With the new XSUAA JWT decoder and scope verification mechanism, the "client id" no longer has to be part of the scope name
for spring to recognize it.

#### HttpSecurity DSL Based
You can use the `HttpSecurity#authorizeRequests` DSL method to secure specific API paths.
```java
		http.authorizeRequests()
                .antMatchers("/security/authorized").hasAuthority("y.c2_myscope-r")
```

#### Annotation Based
You can use the `@PreAuthorize` annotation to verify if the incoming token has the required scope.
```java
	@PreAuthorize("hasAuthority('y.c2_myscope-r')")
	@GetMapping("/authorized")
	public String requiresAuthorization()
	{
		return "OK";
	}
```

#### Custom Configuration
If you do not want to use the default `CaasWebSecurityConfigurerAdapter` and want to completely roll your own security configuration,
you can disable it by setting

    caas.security.web.enabled=false

### Programmatic Authorization Verification
It is possible to verify if the active principal has specific granted authorities by using the `AuthorizationManager` bean.
```java
@Autowired
private AuthorizationManager authorizationManager;
...

if (authorizationManager.hasAuthority("y.c2_promo_read")) {
    LOG.info("User has promotion read authority");
    executeSecuredMethod();
}
```

### CORS Support
All endpoints and verbs will support cross-origin resource sharing by default:
```
URL Mapping:      /**
Allowed Methods:  GET, PUT, POST, PATCH, DELETE, HEAD, OPTIONS
```

### Content-Type Negotiation
If the request did not provide the `Content-Type` request header, then it will be assumed to be:
```
Content-Type: application/json;charset=UTF-8
```

In addition to defaulting the content type, it is a good practice to explicitly set the supported content type on the controllers:
```java
@RestController
@RequestMapping(path = "/categories", produces = "application/json;charset=UTF-8")
public class CategoryController extends AbstractController
{
  ...
```

### Input Validation
To perform input validation on DTO attributes, you can use the `javax.validation.constraints` annotations or the `org.hibernate.validator.constraints` annotations.

This means that you can now do more advanced validation for pre-existing patterns such as:
* @URL: Validates that the annotated string is a valid URL.
* @SafeHtml: Validate a rich text value provided by the user to ensure that it contains no malicious code, such as embedded `<script>` elements.
* @Email: Validates that the annotated string is a well-formed email address.

Example
```java
public class CategoryDto
{
    @NotNull
    @Size(min = 1, max = 255)
    protected String name;

    @Size(min = 1, max = 255)
    @SafeHtml
    protected String description;
    
    @Size(min = 1, max = 2000)
    @URL
    protected String media;
    ...
```
It is encouraged to create your own hibernate validators to perform customized input validation. For more information on how to do this, see [creating custom constraints](https://docs.jboss.org/hibernate/validator/5.0/reference/en-US/html/validator-customconstraints.html).

### HTML Sanitization
This starter adds HTML sanitization support via the registration of _named_ spring beans that provide OWASP `PolicyFactory` instances. All `PolicyFactory` beans will be injected into the `HtmlPolicyFactory`. If you need a reference to the policy, you can request from the factory by its name. For more information about OWASP HTML sanitizer, see [the OWASP java-html-sanitizer github project](https://github.com/OWASP/java-html-sanitizer).

HTML sanitization can be applied at 2 levels:
- Input Validation
- Output Sanitization

Both of these strategies require that you declare your HTML sanitization policy as spring beans.
```java
@Configuration
public class SanitizationConfig
{
	public static PolicyFactory BASIC_STYLING_POLICY = new HtmlPolicyBuilder()
	    .allowElements("p", "i", "b", "em", "strong", "br")
		  .toFactory();

	@Bean(name = "BasicStylingPolicy")
	public PolicyFactory articleTextPolicy()
	{
		return BASIC_STYLING_POLICY;
	}
}
```

#### Input Validation
If you want to perform input validation on a field that contains HTML and want to ensure that the contents comply with a given sanitization policy, then you can use the `SafeHtmlPolicy` annotation in your DTO and supply the name of the policy to use for validating the contents.

```java
public class ContentDto
{
	@SafeHtmlPolicy(policy = "BasicStylingPolicy")
	private String content;

  ...
}
```

In the above example, the `content` attribute will be validated against the `BasicStylingPolicy` declared above. 
* If the policy provided in the `SafeHtmlPolicy` does not exist, then a `IllegalArgumentException` will be thrown.
* If the user provided content is not compliant with the given policy, then a standard spring validation exception will be thrown after all of the validation rules have been verified.
* The validator only throws validation exception if attributes and/or tags are removed from the provided content. The content is valid if there are special characters that can be encoded.

__NOTE__: Validation-based HTML sanitization should not be your first line of defense for HTML sanitization.

#### Output Sanitization
Your first line of defense for HTML sanitization should be output sanitization. In this strategy, HTML content is sanitized upon reading the data and before sending to the API consumer. You can do this extending the `HtmlSanitizationSerializer` and implementing the `getPolicy` method, which represents the OWASP HTML sanitization policy to use for sanitizing the HTML content.
```java
public class BasicStylingPolicySerializer extends HtmlSanitizationSerializer
{
	@Override
	public PolicyFactory getPolicy()
	{
		return SanitizationConfig.BASIC_STYLING_POLICY;
	}
}
```

You can then leverage your new `HtmlSanitizationSerializer` in the `JsonSerialize` annotation on your DTO's attribute.
```java
public class ContentResponseDto
{
	@JsonSerialize(using = BasicStylingPolicySerializer.class)
	private String content;

  ...
}
```

### Custom Encoders
Some custom encoders are provided to perform output encoding for non standard types.

#### PropertiesEncoder
The `PropertiesEncoder` will allow to output encode the Java `Properties` keys and values by using the `PropertiesEncoder.encodeKey` and `PropertiesEncoder.encodeValue` respectively.

Example
```java
PropertiesEncoder.encodeKey("hello world = 123"); // encoded to "hello\\ world\\ \\=\\ 123" to escape spaces and equals
PropertiesEncoder.encodeValue("foo = bar : baz"); // encoded to "foo \\= bar \\: baz" to escape control characters
```

#### QuoteEncoder
The `QuoteEncoder` will allow to output encode single quotes, double quotes and control characters. It offers loosened encoding to be used in place of JS encoders to avoid encoding common characters like hyphens `-`. This encoder should be used along with a strong input validation strategy.

Example
```java
QuoteEncoder.encodeKey("hello' world"); // encoded to "hello\' world" to escape the single quote and prevent string termination
```

### User Injection into Tracing Context
The `user_id`, `subaccount_id`, and `client_ip` injection into the tracing context will occur automatically via a servlet filter. In order to register the filter, you simply have to ensure that you have the `spring-cloud-starter-sleuth` dependency in your `build.gradle`.

If you are already using the `caas-spring-boot-starter-logging`, then the sleuth dependency will be added transitively and there is nothing let to do to enable this functionality.

#### user_id
The user identifier will be added to the tracing context as an extra field with the key `user_id`. Currently, the user identifier is extracted from the JWT token's `xs.user.attributes` by retrieving the first entry in the `accountId` array.

JWT Sample Token Payload
```
{
  "client_id": "testclientid",
  "exp": 2147483647,
  "user_name": "PolarBears",
  "user_id": "polarbears",
  "email": "polarbears@sap.com",
  "zid": "polarbears",
  "iss": "http://polarbears.localhost:8080/uaa/oauth/token",
  "grant_type": "authorization_code",
  "scope": [
    "test"
  ],
  "xs.user.attributes": {
    "accountId": [
      "my-user-id"
    ]
  }
}
```
**Note**
When generating a token without using the XSUAA, such as manually or programmatically for local development or testing purposes, please remember to populate the `xs.user.attributes` if you want the `user` to be populated in the audit logs.

#### subaccount_id
The sub-account identifier will be added to the tracing context as an extra field with the key `subaccount_id`. Currently, the sub-account identifier is extracted from the JWT token's `zid` by using the `com.sap.xs2.security.container.UserInfo@getSubaccountId` method.
If the token is not available, the value is extracted from the `sap-upscale-subaccountid` header added by the approuter as long as it is an UUID, otherwise `subaccount_id` will not be added to the tracing context.

**Note**
When generating a token without using the XSUAA, such as manually or programmatically for local development or testing purposes, please remember to populate the `zid` attribute in order for the `subaccount_id` to be populated in the audit logs.

#### client_ip
The user's client IP address will be added to the tracing context as an extra field with the key `client_ip`. It is being extracted from the first value in a comma seperated list found in the `X-Forwarded-For` HTTP request header.

Example
```
GET http://localhost:8080/products/abc123 HTTP/1.1
Accept: application/json
X-Forwarded-For: 10.10.10.10
```
**Note**
For local development or testing purposes, you can inject the `X-Forwarded-For` HTTP request header if you want the `ip` to be populated in the audit logs.
