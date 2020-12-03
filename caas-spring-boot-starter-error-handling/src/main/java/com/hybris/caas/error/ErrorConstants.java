package com.hybris.caas.error;

/**
 * Simple container to centralize all standardized CaaS error-related text.
 */
public final class ErrorConstants
{
	public static final String INFO = "";
	public static final String URL = "URL";
	public static final String TYPE_400_VALIDATION_VIOLATION = "validation_violation";
	public static final String TYPE_400_BAD_PAYLOAD_SYNTAX = "bad_payload_syntax";
	public static final String TYPE_400_MULTIPART_RESOLUTION_ERROR = "multipart_resolution_error";
	public static final String TYPE_400_BUSINESS_ERROR = "business_error";
	public static final String TYPE_401_INSUFFICIENT_CREDENTIALS = "insufficient_credentials";
	public static final String TYPE_403_INSUFFICIENT_PERMISSIONS = "insufficient_permissions";
	public static final String TYPE_403_IP_BLOCKED = "ip_blocked";
	public static final String TYPE_404_ELEMENT_RESOURCE_NOT_EXISTING = "element_resource_non_existing";
	public static final String TYPE_405_UNSUPPORTED_METHOD = "unsupported_method";
	public static final String TYPE_406_UNSUPPORTED_RESPONSE_CONTENT_TYPE = "unsupported_response_content_type";
	public static final String TYPE_409_CONFLICT_RESOURCE = "conflict_resource";
	public static final String TYPE_412_PRECONDITION_FAILED = "precondition_failed";
	public static final String TYPE_413_BAD_PAYLOAD_SIZE = "bad_payload_size";
	public static final String TYPE_414_URI_TOO_LONG = "uri_too_long";
	public static final String TYPE_415_UNSUPPORTED_REQUEST_CONTENT_TYPE = "unsupported_request_content_type";
	public static final String TYPE_429_TOO_MANY_REQUESTS = "too_many_requests";
	public static final String TYPE_500_INTERNAL_SERVER_ERROR = "internal_server_error";
	public static final String TYPE_500_BACKING_SERVICE_UNAVAILABLE = "backing_service_unavailable";
	public static final String TYPE_503_SERVICE_TEMPORARILY_UNAVAILABLE = "service_temporarily_unavailable";

	public static final String SUB_TYPE_400_INVALID_HEADER = "invalid_header";
	public static final String SUB_TYPE_400_MISSING_HEADER = "missing_header";
	public static final String SUB_TYPE_400_INVALID_FIELD = "invalid_field";
	public static final String SUB_TYPE_400_MISSING_FIELD = "missing_field";
	public static final String SUB_TYPE_400_MISSING_REQUEST_PARAMETER = "missing_query_parameter";
	public static final String SUB_TYPE_400_INVALID_QUERY_PARAMETER = "invalid_query_parameter";
	public static final String SUB_TYPE_400_MISSING_QUERY_PARAMETER = "missing_query_parameter";
	public static final String SUB_TYPE_400_INVALID_URI_PARAMETER = "invalid_uri_parameter";
	public static final String SUB_TYPE_400_INVALID_PATH_SEGMENT = "invalid_path_segment";
	public static final String SUB_TYPE_400_OUT_OF_RANGE_PARAMETER = "out_of_range_parameter";
	public static final String SUB_TYPE_409_UNIQUE_INDEX_VIOLATION = "unique_index_violation";

	public static final String MESSAGE_400 = "Bad request - The request failed due to one of the following reasons: 1) the request format is incorrect; 2) the defined validation constraints are violated; or 3) the business rule is not respected. Do not repeat the call without modifying the request.";
	public static final String MESSAGE_401 = "Full authentication is required to access this resource";
	public static final String MESSAGE_403 = "Insufficient scope for this resource";
	public static final String MESSAGE_404 = "Invalid path segment(s).";
	public static final String MESSAGE_500 = "A server-side exception occurred that prevented the system from correctly returning the result.";

	public static final String EXCEPTION_MESSAGE_BODY_INVALID = "Request body is missing or malformed.";
	public static final String EXCEPTION_MESSAGE_JSON_PARSE_FAILED = "Payload parsing failed. Please ensure payload conforms to JSON specification.";


	private ErrorConstants()
	{
		// private constructor
	}
}
