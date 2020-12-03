# SAP Hybris - Polarbears Multitenant Spring Boot Starter
Adds multitenant support to a Spring Boot based application.

### Features
* **JPA**: EclipseLink based multitenant support by setting the **tenant.multiTenantSessionProperty** property, by default `eclipselink.tenant-id` on the EntityManager after the transaction has been started.
* **Service**: Defines strategy and implementation for getting the tenant for a given HTTP request either from JWT or X-Forwarded-Host header.
* **Spring Controller Tenant Injector**: Allows to inject a `TenantHolder` or simple tenant string into spring controllers.
* **Tracing Context Tenant Injector**: Allows to inject the `tenant` into the Spring Sleuth tracing context and propagated correctly. 
---

### Enabling Multitenant Support
The first thing to do for enabling multitenant support is to add `com.hybris.caas:polarbears-spring-boot-starter-multitenant` dependency to your build. All configurations will be done automatically via the spring auto-configuration process.

Once the dependency is in place, annotate the application class with `@EnableMultitenantSupport` annotation in order to take advantage of the JPA multitenancy support.

```java
@SpringBootApplication
@EnableMultitenantSupport
public class ProductContentApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(ProductContentApplication.class, args);
    }
}
```

#### Enabling JPA Multitenant Support
**Important** The JPA multitenant configuration in place enables transaction management for the application.

**Requirements**
* Implement `EntityManagerHolder` interface for the types that require multitenant support in order to provide access to the `EntityManager` that will have the **eclipselink.tenant-id** property set by default. Can be changed via the property `tenant.multiTenantSessionProperty`.
* Add `@TenantSetter` annotation to types or methods within types that requires the tenant to be set on the `EntityManager` associated with the current transaction.
* Add `TenantHolder` as the first parameter of the method that needs the tenant to be set on the `EntityManager`. Please note that such method needs to be transactional. Either the method has to have the `@Transactional` annotation applied or the type.

**Note** Please note that CRUD methods on Spring Data Repository instances are transactional by default. Please see [Transactionality][1] section from Spring Data documentation for more details.

```java
@Service
@TenantSetter
public class JpaCategoryService implements EntityManagerHolder
{
    private final EntityManager entityManager;

    @Autowired
    public JpaCategoryService(final EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    @Transactional
    public String create(final TenantHolder tenantHolder, final CategoryDto categoryDto)
    {
        final String id = UUID.randomUUID().toString();

        // create category

        return id;
    }
}
```

### Tenant Service Strategy
TenantService strategy and implementation attempts to acquire the tenant for a given HTTP request either from `JWT` or `X-Forwarded-Host` header.

If `Authorization` header is present, it is assumed that `JWT` is present and tries to extract the tenant. 
In case of processing failure of the `Authorization` header, `MissingTenantException` is thrown.
If the tenant is also present in the `X_FORWARDED_HOST` header, it is compared to that from the `Authorization` header to make sure they are a match.
If not, `InvalidTenantException` is thrown.

If `Authorization` header is not present, the tenant will be extracted from the `X-Forwarded-Host` header. 
In case of processing failure of the `X-Forwarded-Host` header, `MissingTenantException` is thrown.

*The tenant is always cast to lower case for string comparison and is therefore returned as lower case*

The expected format of the `X-Forwarded-Host` header, is defined through `tenant.forwarded-host-regex` configuration property defined in tenant-{env}.properties file.
The first group of the matching regex is represents the tenant.

Besides acquiring the tenant for a given HTTP request, the strategy allows to check if the acquired tenant is the PaaS tenant.

Default values are provided in starter for `tenant.forwarded-host-regex` and `tenant.paas-name` configuration properties for different environments but the properties could easily be overwritten within the service as needed.
**Important:** Please note that the default values for the configuration properties are coming from tenant-`environment`.properties files included in the starter.
The supported environment names are: **dev, test, stage, and prod**. If your profile names do not match than you will get an error at service startup indicating that these properties are missing.
It is recommended to use the same names for profiles in your service to avoid such issues. Otherwise, you will need to re-define the configuration properties in your profiles.  

**Important:** `TenantService` strategy does not perform any authentication & authorization checks. Please make sure the service properly applies security authentication & authorization rules for the supported paths.

#### Overriding Tenant Service Strategy
The default implementation of the `TenantService` strategy is provided by `SapJwtTenantService`.

This default strategy could be overridden in a service, if needed, by defining a bean of `TenantService` type.

```java
@Bean
public TenantService overridingTenantService(final TenantProperties tenantProperties)
{
  return new OverriddenTenantService(tenantProperties);
}
```

##### Tenant From JWT
The tenant will be parsed from the JWT token's `iss` claim using the SAP java security library.

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
**Note** When generating a token without using using the XSUAA, such as manually or programmatically for local development or testing purposes, please remember to populate the `iss` claim appropriately if you want the `tenant` to be populated in the audit logs.

##### Tenant From HTTP Header
If there is no token present, then the tenant will be extracted from the `X-Forwarded-Host` HTTP request header that will be populated by the AppRouter.

Example
```
GET http://product-content-caas2-sap.cfapps.us10.hana.ondemand.com/products/abc123 HTTP/1.1
Accept: application/json
X-Forwarded-Host: polarbears-approuter-caas2-sap.cfapps.us10.hana.ondemand.com
```

You can specify which portion of the header value will be used as the tenant value by specifying a capturing group in a regular expression in your `application.yml`
```yaml
tenant.forwarded-host-regex: "^(.*)\-approuter-caas2-sap\.cfapps\.us10\.hana\.ondemand\.com$"
```

Each environment may have a different regular expression and as such, it is recommended to define this property for each spring profile representing an environment.
```yaml

---
spring:
  profiles: dev
tenant.forwarded-host-regex: "^(.*)$"

---
spring:
  profiles: test
tenant.forwarded-host-regex: "^(.*)\-approuter-caas2-sap-test\.cfapps\.us10\.hana\.ondemand\.com$"

---
spring:
  profiles: stage
tenant.forwarded-host-regex: "^(.*)\-approuter-caas2-sap-stage\.cfapps\.us10\.hana\.ondemand\.com$"

---
spring:
  profiles: prod
tenant.forwarded-host-regex: "^(.*)\-approuter-caas2-sap\.cfapps\.us10\.hana\.ondemand\.com$"

---
spring:
  profiles: prod-euc1
tenant.forwarded-host-regex: "^(.*)\-approuter-caas2-sap\.cfapps\.eu10\.hana\.ondemand\.com$"

---
spring:
  profiles: prod-euw2
tenant.forwarded-host-regex: "^(.*)\-approuter-caas2-sap\.cfapps\.eu20\.hana\.ondemand\.com$"
```

*NOTE*
For the profiles named `dev`, `test`, `stage`, `prod`, `prod-euc1`, and `prod-euw2` the `tenant-forwarded-host-regex` is already defined for you using the values in the code snippet above. 
So if you use these profile names to specify your environment, then you do not have to define this proeprty yourself as you can rely on these default values.

##### Path-Based Tenant from HTTP Header
If you need more fine-grained control over the `forwarded-host-regex`, you can specify different rules for different paths in your services.
You can use ant-path matchers to specify which regular expression to use. You can also specify which capturing group of the regular expression is to represent the tenant.

Example
```
GET http://product-content-caas2-sap.cfapps.us10.hana.ondemand.com/kiosk-products/abc123 HTTP/1.1
Accept: application/json
X-Forwarded-Host: kiosk-polarbears-approuter-caas2-sap.cfapps.us10.hana.ondemand.com
```

In this scenario, the tenant (polarbears) follows some unknown prefix (kiosk in this example) for paths starting with `kiosk-products`.
To extract the tenant in this scenario, we can specify the following properties
```yaml
tenant:
  paths[0]:
    path-pattern: "/kiosk-products/**/*"
    forwarded-host-regex: "^(.*)-(.*)-approuter-caas2-sap-test\.cfapps\.us10\.hana\.ondemand\.com$"
    capturing-group: 2
```

*NOTE*
You can specify many path-based tenant properies and they will consumed in the same order as they are defined. As such, you should define the most specific paths first and the most generic paths last.

### Tenant Injection for Spring Controllers
You can inject the tenant into any Spring MVC controller in one of 2 ways:
* Using the `TenantHolder` type
* Using the `@Tenant` marker annotation

#### Injecting Tenant via TenantHolder
This injection technique allows you to inject a `TenantHolder` into the controller simply by adding this parameter in the controller's signature.

Example
```java
@GetMapping
public String someGetMethod(TenantHolder tenant)
```

#### Injecting Tenant via @Tenant
This injection technique allows you to inject the tenant string into the controller simply by adding a string parameter annotated with `@Tenant` in the controller's signature.

Example
```java
@GetMapping
public String someGetMethod(@Tenant String tenant)
```

### Tenant Injection for Tracing Context
The tenant injection into the tracing context will occur automatically via a servlet filter. In order to register the filter, you simply have to ensure that you have the `spring-cloud-starter-sleuth` dependency in your `build.gradle`.

If you are already using the `caas-spring-boot-starter-logging`, then the sleuth dependency will be added transitively and there is nothing let to do to enable this functionality.

#### tenant
The tenant information will be added to the tracing context as an extra field with the key `tenant`. Currently, the tenant information is extracted by using the `TenantService` strategy found in this starter.
The tenant will either be extracted by the JWT token or the `X-Forwarded-Host` HTTP request header.

## Changelog

[1]:https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
