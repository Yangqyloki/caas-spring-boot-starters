# SAP Hybris - CaaS Spring Boot Starter for Error Handling
---
Adds Spring MVC REST API exception handling support following the CaaS best practices to your Spring Boot application. Also provides annotation support and automatic conversion to JSON.

## Features
___
* Catch all exceptions and translate them to CaaS json response.
* Convert all exceptions to a canonical error response format.
* Log all errors along with the error response.
* Ignore exception in managed beans with @IgnoreException.
* Allow mapping of PSQLException to context relevant message.
___

### Enabling CaaS Error Handling
In order to enable the CaaS error handling, you simply need to add the `com.hybris.caas:caas-spring-boot-starter-error-handling` dependency to your build. All configurations will be done automatically via the spring auto-configuration process.


### Exception Conversion
In order for your custom exception type to be converted to the CaaS error format automatically, you can choose to do it in one of two ways:
1. Using the `@WebException` annotation.
2. Extending the `AbstractExceptionConverter`.

#### @WebException
For simple use cases where error message details are not required, you can simply add the annotation to your exception and provide the values that you want reflected in the error response.

 ##### Example
 ```java
@WebException(status = HttpStatus.NOT_FOUND, type = ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
public class PriceNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 7764817600176885202L;
	private static final String MESSAGE = "Price with id '%s' not found.";

	public PriceNotFoundException(final String priceId)
	{
		super(String.format(MESSAGE, priceId));
	}
}
 ```

 ##### Error Response
 ```json
{
	"message": "Price with id '7tYIr68tfgiuo' not found.",
	"type": "element_resource_non_existing",
	"status": 404,
	"moreInfo": "",
	"details": []
}
```

NOTES:
1. The exception's message will be mapped to the `message` attribute of the error response.
2. If you do not supply an `info` attribute, it will remain blank by default.

#### AbstractExceptionConverter
If you need more fine grained control over the final error response, you can write your own converter by extending the `AbstractExceptionConverter`. You simply need to return the `ErrorMessage` Java representation of your error response.

#### AbstractCauseExceptionConverter
Sometimes, a framework will catch all unexpected exceptions and rethrow a generic exception (like the hibernate validation framework). In this situation, you may want to handle the resulting exception differently depending on the actual cause of the generic exception. This is when the `AbstractCauseExceptionConverter` comes in handy.

You simply have to create an exception converter that extends this converter and supply a converter for your specific root cause exception. You also need to override the `useRootCause` method to indicate whether to root cause should be used or not. 
If this method returns `false`, then the direct cause will be used instead.

##### Example
```java
// Converter for generic ValidationException, which will wrap the custom InvalidIdentifierException.
public class ValidationExceptionConverter extends AbstractRootCauseExceptionConverter<ValidationException>
{
	@Override
	protected boolean useRootCause()
	{
		return true;
	}
}

// InvalidIdentifierException with build-in conversion via the @WebException
@WebException(status = HttpStatus.NOT_FOUND, type = ErrorConstants.TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING)
public class InvalidIdentifierException extends RuntimeException
{
	private static final String MESSAGE = "Resource not found with identifier: %s";

	public InvalidIdentifierException(final String identifier)
	{
		super(String.format(MESSAGE, identifier));
	}

}
```

When the exception conversion process finds an exception that maps to a converter that extends the `AbstractRootCauseExceptionConverter`, it will recursively perform the exception conversion process with the exception's root cause.

In the above example, the `InvalidIdentifierException` is thrown during the validation process. Hibernate will catch this exception and rethrow a `ValidationException`, whose root cause will be the original `InvalidIdentifierException`. When the conversion process determines that the `ValidationExceptionConverter` is actually of type `AbstractRootCauseExceptionConverter` it will extract the root cause (our `InvalidIdentifierException`) and retry the exception conversion process with this exception instead. This means that the exception conversion process will convert the top-level `ValidationException` with the information in the `@WebException` annotation found in the `InvalidIdentifierException`.

#### AbstractDatasourceExceptionConverter

If you want to handle a specific datasource provider exception, you can write your own converter by extending the `AbstractDatasourceExceptionConverter`. 
The abstract class currently identifies and converts, to canonical error response format, unique constraint violations as well as foreign key violations.
All other datasource exceptions are converted to a generic error response.

If the error message of the canonical error response needs to be context relevant, in the case of a datasource exceptions that have as root cause a `PSQLException`
this could be achieved either by creating a bean within the service that implements `ExceptionToMessageMapper<PSQLException>` interface named `psqlExceptionToMessageMapper`
to handle the mapping of the exception to a specific message or by defining the mappings of the constraints that require specific messages into the yml file of the service as following:
 ```yaml
 caas.psql.constraint-to-message-mappings:
     '[product_tenant_id_sku_unique]': There is already a product with the same SKU.
 ```

`psqlExceptionToMessageMapper` bean is being used by the abstract class to map the exception to a context relevant message.
The default implementation of the bean provided within the starter only handles the mapping of the `unique_violation` constraint using the yml mappings indicated above.

`PSQLException`, depending on the error, provides additional information about the exception. 
In the case of `unique_violation`, SQL state code 23505, the name of the constraint could be accessed allowing for a context relevant message to be included into the error response.


#### AbstractJpaDatasourceExceptionConverter  

If you want to handle a specific ORM provider exception, you can write your own converter by extending the `AbstractJpaDatasourceExceptionConverter`. Implement the method `getExceptionClass` by returning the specific exception.   

##### Example
```java
@Component
public class InvalidHttpRequestHeaderExceptionConverter extends AbstractExceptionConverter<InvalidHttpRequestHeaderException>
{
	@Override
	protected ErrorMessage convert(InvalidHttpRequestHeaderException ex)
	{
		final ErrorMessageDetail errorMessageDetail = ErrorMessageDetail.builder()
				.withField(ex.getHeader()).withType(ErrorConstants.SUB_TYPE_400_INVALID_HEADER).build();

		return ErrorMessage.builder() 
				.withMessage(ex.getMessage()).withStatus(HttpStatus.BAD_REQUEST.value())
				.withType(ErrorConstants.TYPE_400_VALIDATION_VIOLATION).withMoreInfo(ErrorConstants.INFO)
				.addDetails(errorMessageDetail).build();
	}
}
```

##### Error Response
```json
{
	"message": "Invalid HTTP request header 'hybris-tenant'.",
	"type": "validation_violation",
	"status": 400,
	"moreInfo": "",
	"details": [
		{
			"type": "invalid_header",
			"field": "hybris-tenant"
		}
	]
}
```

NOTES:
1. Exception converters will take precedence over exceptions annotated with `@WebException`.
2. If no converter exists for your exception type and you did not annotate it with `@WebException`, then a generic 500 error message will be returned.
3. You can use the `ErrorMessage` and `ErrorMessageDetail` builders to help you construct the error response.  
4. Remember to make the converter a spring bean and ensure that it is part of your application context.

## Contents
___
### Global Exception Handler
The `GlobalExceptionHandler` implements the `ErrorController` interface and is a bean that will catch all exceptions and convert them to the CaaS error response format and return it as a JSON payload to the consumer.

NOTE: If the consumer is expecting a non-JSON response payload, then the response will simply be empty since we cannot produce a response in the acceptable media type.

#### Logging
The exception handler will also log all exceptions after they have been converted to the CaaS error response format. All 4xx errors will be logged to INFO and all 5xx error will be logged to ERROR.

### CaaS Error Message DTOs
These DTOs are the Java POJO representation of the CaaS error format in the API guidelines.

#### Main Error Message 
```java
public class ErrorMessage
{
	private int status;
	private String type;
	private String message;
	private String moreInfo;
	private List<ErrorMessageDetail> details;

    // Getters and Setters 
}
```

#### Error Message Details
```java
public class ErrorMessageDetail
{
	private String field;
	private String type;
	private String message;
	private String moreInfo;

    // Getters and Setters 
}
```

### CaaS Error Message Constants
All known error types, sub-types and messages found in the CaaS API guidelines can be found in a single constants class.

```java
public final class ErrorConstants
{
	public static final String INFO = "";

	public static final String TYPE_400_VALIDATION_VIOLATION = "validation_violation";
	public static final String TYPE_400_BAD_PAYLOAD_SYNTAX = "bad_payload_syntax";
	public static final String TYPE_400_BUSINESS_ERROR = "business_error";
	public static final String TYPE_401_INSUFFICIENT_CREDENTIALS = "insufficient_credentials";
	public static final String TYPE_403_INSUFFICIENT_PERMISSIONS = "insufficient_permissions";

	// more types ...

	public static final String SUB_TYPE_400_INVALID_HEADER = "invalid_header";
	public static final String SUB_TYPE_400_MISSING_HEADER = "missing_header";
	public static final String SUB_TYPE_400_INVALID_FIELD = "invalid_field";

	// more sub-types ...

	public static final String MESSAGE_500 = "A server-side exception occurred that prevented the system from correctly returning the result.";

	// more messages ...

}
```

### Typical Exceptions
Some exceptions for standard use cases are provided by the starter.

#### MissingHttpRequestHeaderException
Exception thrown when a request is missing an HTTP request header parameter. You simply pass the name of the missing header in the exception constructor and the exception will be mapped to a 400 error.

##### Example
```java
throw new MissingHttpRequestHeaderException("hybris-tenant");
```

##### Response
```json
{
	"message": "Missing HTTP request header 'hybris-tenant'.",
	"type": "validation_violation",
	"status": 400,
	"moreInfo": "",
	"details": [
		{
			"type": "missing_header",
			"field": "hybris-tenant"
		}
	]
}
```

#### InvalidHttpRequestHeaderException
Exception thrown when a request has an invalid HTTP request header parameter. You simply pass the name of the invalid header in the exception constructor and the exception will be mapped to a 400 error.

##### Example
```java
throw new InvalidHttpRequestHeaderException("hybris-tenant");
```

##### Response
```json
{
	"message": "Invalid HTTP request header 'hybris-tenant'.",
	"type": "validation_violation",
	"status": 400,
	"moreInfo": "",
	"details": [
		{
			"type": "invalid_header",
			"field": "hybris-tenant"
		}
	]
}
```

#### PayloadMalformedException
Exception thrown when a request has an invalid HTTP request payload and it cannot be converted to a service Dto. The exception will be mapped to a 400 error.

##### Example
```java
throw new PayloadMalformedException(e);
```

##### Response
```json
{
	"message": "Request body is malformed or cannot be parsed.",
	"type": "bad_payload_syntax",
	"status": 400,
	"moreInfo": ""
}
```

### Ignore exceptions

Spring bean methods annotated with `@IgnoreException` can have the exceptions ignored.
This is useful when we want to ignore specific exceptions that get converted into specific error messages
For example, if we want to ignore database constraint violations (like trying to delete non existent records), and we don't want our
code to handle the exception, but simply ignore it.

```java
    @IgnoreException(EmptyResultDataAccessException.class)
    void methodThatCouldPossiblyThrowException() {
	    // do something interesting here
    }
```

We can ignore more than one exception like this:

```java
    @IgnoreException(value = { TransactionSystemException.class, JpaSystemException.class })
    void methodThatCouldPossiblyThrowException() {
	    // do something interesting here
    }
```

We can even be more fine grained by ignoring some general exceptions, but making sure the root cause exception matches a set of 
given strings. For example a TransactionSystemException might be throw for different reasons, so we need to be more specific into which
case we want to ignore by matching the cause error message.

```java
    @IgnoreException(TransactionSystemException.class, rootCauseMessageFilter = { "duplicate key", "primary key violation" })
    void methodThatCouldPossiblyThrowException() {
	    // do something interesting here
    }
```

Moreover, the @IgnoreException annotation can be applied to other annotations to avoid repeating the same annotation with same
configuration in multiple places.
One preconfigured annotation provided is the @IgnoreUniqueConstraint annotation, which looks something likes this

```java
@IgnoreException(value = { TransactionSystemException.class, JpaSystemException.class }, rootCauseMessageFilter = { "unique",
		"duplicate key", "primary key violation" })
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreUniqueConstraint
{
}
```

## Improvements

* Enhance the exception conversion mechanism to handle the conversion of a child exception through an AbstractExceptionConverter targeting the parent exception within the inheritance tree. Also, having the possibility of handling more than one exception through a single AbstractExceptionConverter implementation would be useful.
* Consistent setting of moreInfo field between @WebException (sets default value when no value provided) and AbstractExceptionConverter (does not support default value).
* Have a static version of the default CaaS error message for 500 http status code.
* Have AbstractExceptionConverter provide methods for analyzing the exception chain and getting the root cause exception.
