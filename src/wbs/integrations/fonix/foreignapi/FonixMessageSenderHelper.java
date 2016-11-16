package wbs.integrations.fonix.foreignapi;

import static wbs.utils.etc.DebugUtils.debugFormat;
import static wbs.utils.etc.LogicUtils.booleanToYesNo;
import static wbs.utils.etc.NumberUtils.integerToDecimalString;
import static wbs.utils.etc.PropertyUtils.checkRequiredProperties;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import wbs.framework.apiclient.GenericHttpSenderHelper;
import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.tools.DataFromJson;
import wbs.framework.web.UrlParams;
import wbs.utils.etc.PropertyUtils.RequiredProperty;

@Accessors (fluent = true)
@PrototypeComponent ("fonixMessageSenderHelper")
public
class FonixMessageSenderHelper
	implements GenericHttpSenderHelper <
		FonixMessageSendRequest,
		FonixMessageSendResponse
	> {

	// request properties

	@Setter
	FonixMessageSendRequest request;

	@Getter
	Map <String, String> requestHeaders;

	@Getter
	Map <String, List <String>> requestParameters;

	@Getter
	String requestBody;

	// response properties

	@Setter
	Long responseStatusCode;

	@Setter
	String responseStatusReason;

	@Setter
	Map <String, String> responseHeaders;

	@Setter
	String responseBody;

	@Getter
	FonixMessageSendResponse response;

	// property accessors

	@Override
	public
	String url () {
		return request.url ();
	}

	// implementation

	@Override
	public
	void verify () {

		checkRequiredProperties (
			requiredProperties,
			request);

	}

	@Override
	public
	void encode () {

		// construct request parameter map

		requestHeaders =
			ImmutableMap.<String, String> builder ()

			.put (
				"Content-Type",
				"application/x-www-form-urlencoded")

			.put (
				"X-Api-Key",
				request.apiKey ())

			.build ();

		requestParameters =
			ImmutableMap.<String, List <String>> builder ()

			.put (
				"REQUESTID",
				ImmutableList.of (
					integerToDecimalString (
						request.id ())))

			.put (
				"ORIGINATOR",
				ImmutableList.of (
					request.originator ()))

			.put (
				"NUMBERS",
				ImmutableList.copyOf (
					request.numbers ()))

			.put (
				"BODY",
				ImmutableList.of (
					request.body ()))

			.put (
				"DUMMY",
				ImmutableList.of (
					booleanToYesNo (
						request.dummy ())))

			.build ();

		requestBody =
			UrlParams.mapToString (
				requestParameters);

	}

	@Override
	public
	void decode () {

		JSONObject jsonObject =
			(JSONObject)
			JSONValue.parse (
				responseBody);

		DataFromJson dataFromJson =
			new DataFromJson ();

		response =
			dataFromJson.fromJson (
				FonixMessageSendResponse.class,
				jsonObject);

	}

	private final static
	List <RequiredProperty> requiredProperties =
		ImmutableList.of (

		RequiredProperty.notNull (
			"url"),

		RequiredProperty.notNull (
			"apiKey"),

		RequiredProperty.notNull (
			"originator"),

		RequiredProperty.notEmptyCollection (
			"numbers"),

		RequiredProperty.notEmptyString (
			"body")

	);

}