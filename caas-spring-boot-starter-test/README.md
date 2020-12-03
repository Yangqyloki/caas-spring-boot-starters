# SAP Hybris - CaaS Spring Boot Starter for Tests
---
Provides test related features

## Features
___
- JWT token generation
---

#### Running the service locally
In order to load the values for `xsuaa` properties, normally provided in the `VCAP_SERVICES` env var in Cloud Foundry.  
* We add the `caas-spring-boot-starter-test` as a dependency, this provides the `xsuaa.*` properties via the `src/main/resources/test.properties`, this would normally be the case for running integration tests.  
* For running the application locally (without the test class path):
  * When using IntelliJ, using the envfile plugin. Under Run/Debug configurations / EnvFile. Check `Enable EnvFile`, Click Add (+) button, then select `.env file`, and browse for the file `<absolute/path/to>/caas-spring-boot-starter-test/src/main/resources/test.properties`.  
  * If not using IntelliJ, or not having the EnvFile plugin. Copy the `xsuaa.*` properties and paste them in the `application-dev.yml` file temporarily.
 
This would allow a token generated using `TokenFactory` for an integration test to be used locally for manual testing as well.

### JWT Token Generation
There are a lot of ongoing changes to the underlying SAP Java Security Container that require changes to JWT tokens generated for tests. In order to encapsulate these changes,
the `CaasJwtToken` was created. It allows you to create signed and encoded tokens by simply providing a few required parameters and all of the JWT claims will be populated accordingly.

The generated token will also populate the `accountId` in the `xs.user.attributes` claim in order to ensure that a user is present in the tracing context.

### Creating Token Directly
You can use the `CaasJwtToken` class to generate a token as in the examples below.
 
Example: Use default token with all default values.
```java
    final String token = CaasJwtToken.DEFAULT;
```

Example: Create a token for specific tenant, but use default values for everything else.
```java
    final String token = CaasJwtToken.builder().tenant("my-tenant").build().encode();
```

### The Token Factory
As a convenience, the `TokenFactory` bean can be injected into integration tests and will generate tokens for you based on properties in your `application.yml` file.

#### Properties
The following properties can be set:
- accountId: the account is for the user being represented by the token. `Default`: caas-test
- tenant: the tenant that the token should be for. `Default`: caas
- identityZone: the PaaS tenant to be used as the identity zon. `Default`: caas
- privateKey: the private key used to sign the token. `Default`: -----BEGIN RSA PRIVATE KEY-----MIIBOgIBAAJBAJaEbyMsfpu+AI9HIUIIOecfTjaYtbgYrjEExVFiDGks0TqxKYupCJOIuA2BAJYvJTe+N+GFmhytzcDf/gvzeYkCAwEAAQJARzjlaextAH8YvrN2vkssMvwyQ01hd3peOp26TLcrjjxFBVrjKrSgus29/0cvMvcFRoFxOc+/sqEmo6laR26LTQIhAOrWe5sryGJMbjFCcXpmicXjTPaLJNNRevKXvbRDyiwbAiEApBS++7vREYh9GDDUFU/6wvLPrf9m8noPaAUEjb+JQysCIBbkw/YTpZxXoSJyPMfW+TVufFFIrVVgiyDdRCQvu6TjAiBy8Il85GunQZqH2KSorVc3O0qaU8B0+WZTcrhm55aq8wIhAK5H7nL472h7454nwwV+OdC1YXnL3MHUYhty6H6v4T9Y-----END RSA PRIVATE KEY-----'
- scopes: the scopes that are required for this token. `Default`: empty list

You can add multiple named tokens like this:
```yaml
sap.tokens:
  developer:
    tenant: sap_${now}
    scopes:
      - develop
      - test
  test-user:
    accountId: Alice Test ${uuid}
    tenant: test
    scopes:
      - read_only
```

#### Placeholders
You can use placeholders in the `accountId` and `tenant` properties to introduce some randomness or to guarantee that a different value will be used.
For this purpose you can use the `${now}` or `${uuid}` placeholders in order to inject the current time in millis or a random UUID respectively.

#### API
The `TokenFactory` provides 2 methods.
```java
CaasJwtToken getTokenObject(final String tokenName);

String getToken(final String tokenName);
```

The first allows you to retrieve the `CaasJwtToken` in order to inspect the `accountId`, `tenant`, `privateKey` and `scopes`. You will then have to call `encode()` to produce an encoded token.
The second allows you to retrieve an encoded token. You will have to decode the token in order to inspect the contents.
