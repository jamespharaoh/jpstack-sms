package wbs.web.misc;

public
class HttpStatus {

	private
	HttpStatus () {
		// never instantiated
	}

	public final static
	long

		httpContinue = 100,
		httpSwitchingProtocols = 101;

	public final static
	long

		httpOk = 200,
		httpCreated = 201,
		httpAccepted = 202,
		httpNonAuthoritativeInformation = 203,
		httpNoContent = 204,
		httpResetContent = 205,
		httpPartialContent = 206;

	public final static
	long

		httpMultipleChoices = 300,
		httpMovedPermanently = 301,
		httpFound = 302,
		httpSeeOther = 303,
		httpNotModified = 304,
		httpUseProxy = 305,
		httpTemporaryRedirect = 307;

	public final static
	long

		httpBadRequest = 400,
		httpUnauthorized = 401,
		httpPaymentRequired = 402,
		httpForbidden = 403,
		httpNotFound = 404,
		httpMethodNotAllowed = 405,
		httpNotAcceptable = 406,
		httpProxyAuthenticationRequired = 407,
		httpRequestTimeout = 408,
		httpConflict = 409,
		httpGone = 410,
		httpLengthRequired = 411,
		httpPreconditionFailed = 412,
		httpRequestEntityTooLarge = 413,
		httpRequestUriTooLong = 414,
		httpUnsupportedMediaType = 415,
		httpRequestedRangeNotSatisfiable = 416,
		httpExpectationFailed = 417,
		httpImATeapot = 418,
		httpMisdirectedRequest = 421,
		httpUnprocessableEntity = 422,
		httpLocked = 423,
		httpFailedDependency = 424,
		httpUpgradeRequired = 426,
		httpPreconditionRequired = 428,
		httpTooManyRequests = 429,
		httpRequestHeaderFieldsTooLarge = 431,
		httpUnavailableForLegalReasons = 451;

	public final static
	long

		httpInternalServerError = 500,
		httpNotImplemented = 501,
		httpBadGateway = 502,
		httpServiceUnavailable = 503;

}
