# SAP Hybris - CaaS Spring Boot Starter for Spring WebClient
---
Provides webclient features

## Features
___
* Build CaasWebClient based on Spring WebClient for making REST calls to other web services.
* Configurable `HttpClient`, used  underneath the `CaasWebClient`, with a `defaultHttpClient`. 
* Added `HttpClientCustomizer` for customizing the `httpClient`.

### Note:
* CaasWebClient supports tracing context propagation, e.g. X-CorrelationID, tenant_id
* CaasWebClient supports OAuth token authorization. OAuth tokens are fetched from XSUAA service using Basic Auth and cached using Caffeine.  
  The XSUAA credential(clientId and clientSecret) for calling UAA service are loaded from properties file and can be override. The Caffeine cache size and the OAuth token expiration time can be customized by overriding the default values in properties file.
* CaasWebClient provides two filters to log web service request and response. To mask request/response headers which contain sensitive information, the header names should be added in the obfuscate header list in properties file.

___

### Enabling CaaS WebClient
In order to build a CaaS WebClient, you simply need to add the `com.hybris.caas:caas-spring-boot-starter-client` dependency to your build. All configurations will be done automatically via the spring auto-configuration process.

### Build CaaS Client
In order to make REST calls to other web services, you will have to get an instance of CaasWebClient bean.

##### Example
```java
   /**
    * Represents the client for communicating with order-broker service.
    */
    @Slf4j
    @Component
    public class OrderBrokerServiceClient
    {
    	protected static final String ORDER_CLIENT_NAME = "order";
    	private static final String SOC_ORDERS_QUERY_PATH = "/analytics/socordersquery";
    	private final CaasWebClient caasWebClient;
    	private final URI orderBrokerUri;
    
    	public OrderBrokerServiceClient(final CaasWebClient caasWebClient)
    	{
    		this.caasWebClient = caasWebClient;
    		// initialize other attributes
    	}
        
        // other methods
    }
```

### Overriding default WebClient settings
The default settings of the Spring WebClient is provided in properties file in caas starters.

This default settings could be overridden in a service, if needed, by providing the properties in the example below.

```yaml
caas.web.client:
  properties:
    connect-timeout-ms: 5000
    read-timeout-ms: 5000
    log-level: WARN
    retry:
      max-attempts: 1
    obfuscate:
      headers:
        - Authorization
  token-cache:
    max-size: 100
    expire-after-write: 1
  uaa:
    client-id: dummy
    client-secret: dummy
    token-uri-template: http://localhost:8181/%s/uaa/oauth/token
```
