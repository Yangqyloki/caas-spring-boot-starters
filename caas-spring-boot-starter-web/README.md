# SAP Hybris - CaaS Spring Boot Starter for Web
---
Provides web related features

## Features
___
- JSON request mapping annotation
- Guid annotation
- KeyMultiValues annotation
- URI length validation interceptor
- Pagination model attributes
- Web validators
- Processing batch requests
- Processing Guid conversion
- Pageable parameter injection
- Locale resolver & message source
---

### Web Validators

The following validators belong to this project:

#### EnumValues
You can use this validator to ensure that a string value provided matches with a valid value from a given Enum.
This validator will append a constraint violation to the `ConstraintValidatorContext`.

Example:
```java
@EnumValues(ApplicationPlatform.class)
protected String platform;
```

#### Locale
You can use this validator to ensure that a string value provided represents a valid locale.
A valid locale string has 2 chars in lowercase for the language code, optional script with 4 chars with first char in uppercase and rest in lowercase, and 2 chars in uppercase for the country code separated by a dash (i.e. `en-US`, `sr-Latn-RS`).
Besides respecting the format indicated above, the locale string is checked to ensure that it indicates an available Java Locale.

This validator will append a constraint violation to the `ConstraintValidatorContext`.

Example:
```java
private Map<@Locale String, String> name;
```

#### Identifier
You can use this validator to ensure that a string value provided matches the UUID regex with lower-case letters only.
This validator will throw an `InvalidIdentifierException` if the pattern is not respected. You can also use this validator to ensure that the identifier has a specific length. This exception will be mapped to a 404 - NOT FOUND and is intended to be used to validate path parameters which are entity identifiers and skip database access if the pattern does not match the expected identifier pattern.

Example:
```java
@GetMapping(RESOURCE_PATH)
public ResponseEntity<Resource<EditionResponseDto>> getEdition(@PathVariable @Identifier(min = 2, max = 255, regex = Constants.MY_REGEX) final String id)
{
	...
```

#### FilenameSize
You can use this validator to ensure that the size of a `MultipartFile`'s original filename has a specific length. There are 2 attributes: `min` and `max`. These attributes will validate the length of the filename associated with the multipart file. The default value for `min` is `0` and for `max` is `Integer.MAX_VALUE`.

If the provided `min` or `max` value is less than `0`, or the `max` value is less than the `min` value, then an `IllegalArgumentException` will be raised.

Example:
```java
@PostMapping(headers = "content-type=multipart/form-data")
public ResponseEntity<Void> uploadFile(@RequestPart("file") @FilenameSize(min = 1, max = 255) MultipartFile file)
{
	...
```

#### ZipFile
You can use this validator to ensure that the contents of a `MultipartFile` represent a properly formatted binary zip file. This validator will indicate that a `null` `MultipartFile` is a valid representation. If you want to ensure that the `MultipartFile` is not null, then please use the `@NotNull` validation annotation.

Example:
```java
@PostMapping(headers = "content-type=multipart/form-data")
public ResponseEntity<Void> uploadFile(@RequestPart("file") @ZipFile MultipartFile file)
{
	...
```

#### FileExtension
You can use this validator to ensure that the uploaded zip file extension matches the regex with lower-case and upper-case letters. There is 1 attribute: 'name'. This attribute holds the value of the file extension name that is used to build the regex used during the validation.
This validator will throw an exception mapped to 400 - BAD REQUEST if the match is invalid.

Example:
```java
@PostMapping(headers = "content-type=multipart/form-data")
public ResponseEntity<Void> uploadFile(@RequestPart(name = "file", required = false) @FileExtension(name = "zip") final MultipartFile file)
{
	...
```

#### ContentLanguageHeader
You can use this validator to ensure that the content of the `Content-Language` header represents a valid locale.
A valid locale string has 2 chars in lowercase for the language code, optional script with 4 chars with first char in uppercase and rest in lowercase, and 2 chars in uppercase for the country code separated by a dash (i.e. `en-US`, `sr-Latn-RS`).
Besides respecting the format indicate above, the locale string is checked to ensure that it indicates an available Java Locale.

The validator will throw an `InvalidIdentifierException` if the content of the header does not represent a valid locale. 
This exception is mapped to a 400 - validation violation `invalid_header` response with a reference to the invalid header.

The validator is intended to be used together with `@RequestHeader` annotation which by default it ensures that `Content-Language` is present
and it allows to specify a default value if the header is optional.

The required data type for the validator is `String` and `com.hybris.caas.web.validator.utils.LocaleUtils` class provides
`toLocale` static method to convert the `String` into java `Locale`. Please note that `toLocale` method enforces the required
format but not that the locale string is an available Java locale (that is done by the validator).

Example:
- required `Content-Language` header 
```java
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;

@PutMapping
public ResponseEntity<Void> createProduct(@RequestHeader(CONTENT_LANGUAGE) @ContentLanguageHeader final String contentLanguageHeader)
{
	final Locale locale = com.hybris.caas.web.validator.utils.LocaleUtils.toLocale(contentLanguageHeader);
	...
```
- optional `Content-Language` header
```java
import static com.hybris.caas.web.Constants.US_LOCALE_TAG;
import static org.springframework.http.HttpHeaders.CONTENT_LANGUAGE;

@PutMapping
public ResponseEntity<Void> createProduct(@RequestHeader(value = CONTENT_LANGUAGE, required = false, defaultValue = US_LOCALE_TAG) @ContentLanguageHeader final String contentLanguageHeader)
{
	final Locale locale = com.hybris.caas.web.validator.utils.LocaleUtils.toLocale(contentLanguageHeader);
	...
```
 
### Batch Support

Support for batch requests in this starter is achieved by 2 means: controller injection & batch processing.

### BatchRequestArgumentResolver

The batch request argument resolver will allow Spring MVC controllers to inject a collection of `BatchRequest` objects directly into a controller's signature.

Example: Inject a batch of category to parent category relation DTOs in a top-level JSON array.
```
Request
[
    {
        "categoryId": "foo",
        "parentCategoryId": "bar"
    }    
]
```
```java
	@ResponseStatus(HttpStatus.MULTI_STATUS)
	@PostMapping
	public ResponseEntity<List<BatchResponseDto>> createBatch(final List<BatchRequest<CategoryParentCategoryAssociationDto>> batch)
	{
		...
```

Example: Inject a batch of category to parent category relation DTOs wrapped in a value property in a top-level JSON object.
```
Request
{
    "value": [
        {
            "categoryId": "foo",
            "parentCategoryId": "bar"
        }    
    ]
}
```
```java
	@ResponseStatus(HttpStatus.MULTI_STATUS)
	@PostMapping
	public ResponseEntity<List<BatchResponseDto>> createBatch(final WrappedCollection<BatchRequest<CategoryParentCategoryAssociationDto>> batch)
	{
		...
```
NOTE: Do not use `@RequestBody` when attempting to inject a list of `BatchRequest` into a controller.

The argument resolver will inspect the original JSON payload and attempt to convert it to a collection of Jackson `ObjectNode` elements. If this is successful, it will group all similar elements together into a single `BatchRequest`.

Then, each `BatchRequest` will undergo a transformation to convert the `ObjectNode` into the expected type as provided in the controller signature (`CategoryParentCategoryAssociationDto` in the example above). The DTO produced will be encapsulated in the `BatchRequest` and finally undergo JSR-303 annotation-based validation.

If a `BatchRequest` was incapable to convert the JSON payload or if the validation failed, then no DTO will be present in the `BatchRequest` and it will instead contain a `BatchResponseDto` with an `ErrorMessage` detailing what went wrong.

### BatchRequest Processing

The batch processing requires a `BiFunction<T, Collection<ObjectNode>, Stream<BatchResponseDto>>` function. This processor will indicate to the request what behaviour has to be applied to the item what response should be returned.

Example: Delete a category - parent category association to the database and return 204 for each original request.
```java
		categoryParentCategoryService.delete(getTenant(), categoryParentCategoryAssociationDto);	// call service to delete the association
		return requests.stream()
				.map(request -> BatchResponseDto.builder()										// map each successful original request to a batch response with 204
					.status(HttpStatus.NO_CONTENT.value())
					.request(BatchResponseRequestDto.builder().body(request).build())
					.build());
```

### Batch Processing Helper

The batch processing helper exposes methods to process batch requests and build the response. The method ```buildMultiStatusResponse``` builds the MULTI_STATUS response for the processed batch requests. The ```buildCreatedBatchResponses``` method builds CREATED response for each given request. The ```buildNoContentBatchResponses``` method builds NO_CONTENT response for each given request. The ```processBatch``` method builds the MULTI_STATUS and NO_CONTENT status. The method has two parameters: A list of batch request `List<BatchRequest<K>> batchRequests` and a consumer operation to be executed `Consumer<K> consumer`.

Example: Create/Delete a selling tree - experience association to the database and return 204 for each original request.
```java
@PostMapping
public ResponseEntity<List<BatchResponseDto>> createSellingTreeExperienceBatch(
        @Size(max = 100) final List<BatchRequest<SellingTreeExperienceAssociationDto>> batchRequests)
{
    return BatchHelper.buildBatchResponse(batchRequests,
            dto -> sellingTreeExperienceService.create(getTenantHolder(), dto));
}

@DeleteMapping
public ResponseEntity<List<BatchResponseDto>> deleteExperienceEditionBatch(
        @Size(max = 100) final List<BatchRequest<SellingTreeExperienceAssociationDto>> batchRequests)
{
    return BatchHelper.buildBatchResponse(batchRequests,
            dto -> sellingTreeExperienceService.delete(getTenantHolder(), dto));
}
```

### Validating the BatchRequest DTO

By default, the `BatchRequestArgumentResolver` will perform the `JSR-303` validation on the converted DTO using no validation groups. If you want to provide validation groups to the underlying validation, then you must use them explicitly in the controller by adding them to the `@Validated` annotation in the method signature.

```java
@PostMapping
public ResponseEntity<List<BatchResponseDto>> createSellingTreeExperienceBatch(
        @Size(max = 100) @Validated(MyGroup.class) final List<BatchRequest<SellingTreeExperienceAssociationDto>> batchRequests)
```

In order to validate the `@Size(max = 100)` in the example above, the `@Validated` annotation must also be present at the class level.
```java
@Validated
@RestController
@RequestMapping("/sellingtreesproductscategoriesbatch")
public class SellingTreeProductCategoryControllerBatch extends AbstractController
{
```

### Error Handling

By default, the batch request will intercept all exceptions and use the `ExceptionConverterFactory` to convert the exception to the appropriate CaaS `ErrorMessage` exactly as documented in the `caas-spring-boot-starter-error-handling` module.

### Batch Responses

Each original raw JSON request is present in a `BatchRequest` and will produce a matching `BatchResponseDto`. All exceptions occurring during the batch processing will be automatically mapped to a `BatchResponseDto` with an `ErrorMessage` body as mentioned above.

All successful requests must be explicitly mapped to a `BatchResponseDto` in the function provided to the batch processor as shown in the example above.

The response status for the overall HTTP request should be 207 - MULTI STATUS and can be returned by the Spring controller like this:
```java
	return new ResponseEntity<>(batchResponses, HttpStatus.MULTI_STATUS);
```

### Support for X-Forwarded headers filters

Filter that extracts values from "Forwarded" and "X-Forwarded-*" headers, wrap the request
and response, and make they reflect the client-originated protocol and address.
For more details see `org.springframework.web.filter.ForwardedHeaderFilter`

This filter can be disabled by setting the `server.use-forward-prefix` property to `false` in you SpringBoot configuration:

    server.use-forwarded-header-filter: false

### Support for X-Forwarded-Prefix
The starter adds a filter by default, which adds the HTTP header `X-Forwarded-Prefix`. 
The value of the headers is obtained from the HTTP header `X-Forwarded-Path`, given by the AppRouter.
For example for the incoming header:

    X-Forwarded-Path: /public/product-content/products

The filter will extract the non-mapped path segments (the ones before `/products`) and create a header as:

    X-Forwarded-Prefix: /public/product-content

The X-Forwarded headers are used when using applications behind a proxy server, like AppRouter, in order to reply with external/proxied URLs
Specifically, `X-Forwarded-Prefix` helps Spring to build the external/proxied URLs for the missing path segments

This functionality can be disabled by setting the `server.use-forward-prefix` property to `false` in you SpringBoot configuration:

    server.use-forward-prefix: false

### Pageable Parameter Injection for Spring Controllers
You can inject the page number, page size and sort parameters into any Spring MVC controller by specifying the `Pageable` type in the method signature.

*Page Number Rules*
1. The name of the page number attribute is `pageNumber`.
2. The value of the `pageNumber` attribute is expected to be indexed starting at page 1.
3. If no value is provided for the `pageNumber` attribute, then the `@PageableDefault` will be used.
4. If no value is provided for the `pageNumber` attribute and no `@PageableDefault` is provided, the default value for `pageNumber` will be `0`.

Reminder: the `Pageable` page number will be zero-indexed when it will be used as part of the spring-data repositories.

*Page Size Rules*
1. The name of the page size attribute is `pageSize`.
2. If the value provided is larger than `1,000`, then the page size will be set to `1,000`.
2. If the value provided is larger than `1,000` and the `@MaxPageSize` is set to a value less than `1,000`, then the page size will be set to the value of `@MaxPageSize`.
2. If no value is provided for the `pageSize` attribute, then the `@PageableDefault` will be used.
2. If no value is provided for the `pageSize` attribute and the `@PageableDefault.size` is larger than `1,000`, then the page size will be set to `1,000`.
2. If no value is provided for the `pageSize` attribute and the `@PageableDefault.size` is larger than `1,000` and the `@MaxPageSize` is set to a value less than `1,000`, then the page size will be set to the value of `@MaxPageSize`.
3. If no value is provided for the `pageSize` attribute and no `@PageableDefault` is provided, the default value for `pageSize` will be `20`.

See the subsection below about `Page Size Limiting` for more information about the `@MaxPageSize` annotation.

*Sorting Rules*
1. The name of the sort attribute is `sort`.
2. If a value is provided for an attribute without a direction, then the default direction will be `ASC`.
3. If no value is provided for the `sort` attribute, then the `@PageableDefault` will be used for the default sort properties and sort direction. 
4. If no value is provided for the `sort` attribute and no `@PageableDefault` is provided, the default value for `sort` will be `Sort.unsorted()`.
5. If the `@SortProperties` is provided, then the sort properties will be filtered to only contain the ones that are present in the `@SortProperties` annotation's values.

Reminder: the valid sort parameter syntax is as follows: `sort=<attribute 1>:<asc|desc>,<attribute 2>:<asc|desc>...`.

See the subsection below about `Sort Parameter Whitelisting` for more information about the `@SortProperties` annotation.

Example 1 - No defined page number, page size, sort and no `@PageableDefault`
```java
@GetMapping("/products")
public String someGetMethod(Pageable pageable)
{
	...
}
```

```
GET /products

-> pageNumber   :   0
-> pageSize     :   20
-> sort         :   Sort.unsorted()
```

Example 2 - No defined page number, page size nor sort but with `@PageableDefault`
```java
@GetMapping("/products")
public String someGetMethod(
		@PageablDefault(page = 10, size = 100, sort = {"foo", "bar"}, direction = Sort.Direction.DESC) Pageable pageable)
{
	...
}
```

```
GET /products

-> pageNumber   :   10
-> pageSize     :   100
-> sort         :   ("foo":Sort.Direction.DESC),("bar":Sort.Direction.DESC) 
```

Example 3 - Use specific default page size and specific implicit sort on "foo":ASC
```java
@GetMapping("/products")
public String someGetMethod(@PageablDefault(size = 100, sort = {"foo"}) Pageable pageable)
{
	...
}
```

```
GET /products?pageNumber=2

-> pageNumber   :   2
-> pageSize     :   100
-> sort         :   ("foo":Sort.Direction.ASC)
```

Example 4 - Use specific default page size and specific implicit sort on "foo":ASC
```java
@GetMapping("/products")
public String someGetMethod(@PageablDefault(size = 100, sort = {"foo"}) Pageable pageable)
{
	...
}
```

```
GET /products?pageNumber=2

-> pageNumber   :   2
-> pageSize     :   100
-> sort         :   ("foo":Sort.Direction.ASC)
```


#### Page Size Limiting
You can limit the page size for each `Pageable` injected into a Spring MVC controller by using the `@MaxPageSize` annotation.
Simply set the desired value in the annotation and this will overwrite the page size in the injected `Pageable` if its page size is larger than the value provided in the `@MaxPageSize` annotation.

**NOTE**: Without the use of this annotation, the maximum page size limit will be 1,000.

Example - Without @MaxPageSize
```java
@GetMapping("/products")
public String someGetMethod(Pageable pageable)
{
	...
}
```

```
GET /products?pageSize=3000

-> pageSize : 1000
```

Example - With @MaxPageSize
```java
@GetMapping("/products")
public String someGetMethod(@MaxPageSize(50) Pageable pageable)
{
	...
}
```

```
GET /products?pageSize=3000

-> pageSize : 50
```


#### Sort Parameter Whitelisting
You can whitelist the properties that are allowed to be injected into the sort parameter into any Spring MVC controller by using the `@SortProperties` annotation.
This will work when injecting either the `org.springframework.data.domain.Sort` or the `org.springframework.data.domain.Pageable` parameter types.
The column attribute is allowing a mapping of the property name to its internal or database column name.
Example
```java
@GetMapping("/products")
public String someGetMethod(@SortProperties(value = { "name", "startAt" }, column = { "name", "start_at" }) Sort sort)
{
	...
}
```

```
GET /products?sort=name,startAt,invalid

Sort object contents: {["name": ASC], ["start_at": ASC]}
-> attribute "invalid" is dropped
```


#### Guid conversion
You can use Guid annotation to convert the incoming resource id to lowercase.

Example 1 - annotation applied on request parameter or path variable in Spring controller.
```java
@GetMapping("/promotions/{id}")
public String someGetMethod(@PathVariable("id") @Guid final String id)
{
	...
}
```

Example 2 - annotation applied inside DTOs' property.
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotionDto
{
	@NotNull
	@Size(min = 1, max = 255)
	@Guid
	protected String experienceId;

	....
}
```


#### KeyMultiValues conversion
You can use KeyMultiValues annotation to disable the conversion of request parameter from comma delimited string to a collection.

**NOTE**: A comma-delimited string is converted to a collection by default in Spring, refer to StringToCollectionConverter.java.
If a request parameter is annotated with this annotation, then the value of this parameter will be set in the list as one object.


Example 1 - annotation applied on request parameter in Spring controller.
```java
@GetMapping("/products")
public String someGetMethod(@RequestParam @KeyMultiValues final List<String> customAttributes)
{
	...
}
```

### Locale resolver
A default `localeResolver` bean is defined if not provided by the service. The implementation of the custom `localeResolver` is available in `CaasLocaleResolver` class.
The purpose of this custom locale resolver is to ensure consistency across different services in regards to handling of the `Accept-Language` header.

If a service does not require the `localeResolver` bean to be defined, `caas.i18n.locale-resolver.enabled` configuration property needs to be set to `false`. 
```yaml
caas.i18n:
    locale-resolver.enabled: false # disables the creation of the localeResolver bean
```

Please find below the rules supported and the validations performed by the custom locale resolver:
* when `Accept-Language` header is not provided the returned locale is `Locale.US`
* when `Accept-Language` header is set to `*`, the returned locale is a constant to be used as all languages indicator `com.hybris.caas.web.Constants.ALL_LANGUAGES_LOCALE`
* validate that language of the `Locale` retrieved from `HttpServletRequest` is 2 chars in lowercase
* validate that optional script of the `Locale` retrieved from `HttpServletRequest` is 4 chars with first char in uppercase and rest in lowercase
* validate that country of the `Locale` retrieved from `HttpServletRequest` is 2 chars in uppercase
* validate that the `Locale` retrieved from `HttpServletRequest` represents an available Java `Locale`

If validation fails an `InvalidHttpRequestHeaderException` is thrown. 
This exception is mapped to a 400 - validation violation `invalid_header` response with a reference to the invalid header.

Please note that multiple values can be set in the `Accept-Language` header. However, only the preferred `Locale` that the client will accept will be used. 

Example of how to access the `Locale` extracted by the `localeResolver` from the `Accept-Language` header:
```java
@GetMapping
public ResponseEntity<Map<String, String>> getLabels(final TenantHolder tenantHolder, final Locale locale)
{
	...
```

### Message source
A `CaasResourceBundleMessageSource` bean is available to provide the translated messages for a particular locale as well as the list of
locales for which translations are available. This bean caches all the label translations provided by the translation team at service start-up.

As per our standard configuration, a `messages.properties` file located in `resources/i18n` folder is provided for translation.
The translated files are then pushed into `resources/i18n/translations` folder by translation team using the following naming pattern:
`messages_<locale>.properties`. The locale adheres to the following pattern: 2 chars in lowercase for language code, 
optional script with 4 chars with first char in uppercase and rest in lowercase, and 2 chars in uppercase for country code separated by an underscore.

This bean is created only if the i18n related properties described below are defined for a service. If a service does not hold any
translatable static labels, the i18n related properties should not defined.

```yaml
caas.i18n:
    properties-file-base-name: messages # the name of the file provided for translation, the only extension supported at the moment is ".properties"
    translations-path: i18n/translations # the resources folder where translated files are pushed by translation team
```

All the files found under `caas.i18n.translations-path` starting with `caas.i18n.properties-file-base-name` + underscore are being
processed and if they embed in their name a valid locale, their content is loaded and stored into the bean keyed by extracted `Locale`.

If any error occurs during processing of the translated files, an exception is thrown and the service is prevented from starting.

Example of using the `CaasResourceBundleMessageSource` bean:
```java
public DefaultInternationalizationService(final CaasResourceBundleMessageSource caasMessageSource, ...
```   