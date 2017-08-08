// message template database javascript

function generateString(length) {
	var str = "";

	for( var i=0; i < length; i++ )
		str += 'a';

	return str;
}

$(function () {

	var bloqued = 0;

	$(".details > tbody > tr").each (function () {

		var parameterInfo = $("span.parameters-length-list", this).text();

		if (parameterInfo != '') {

			var row = this;
			var thisBloqued = false;

			var updateCharCount = function () {

				var templateText = $("textarea", row).val ();

				// parameters data

				var tokens =
					parameterInfo.split("&");

				parametersObject = {};

				for (var i = 0; i < tokens.length; i++) {

					var token =
						tokens[i].split("=");

					parametersObject[token[0]] = token[1];

				}

				// length of the message

				var minLength = parametersObject["minimumTemplateLength"];
				var maxLength = parametersObject["maximumTemplateLength"]

				var regExp = /{(.*?)}/g;
				var parts = templateText.match(regExp);
				var templateTextReplaced = templateText;

				if (parts != null) {
					for (var i = 0; i < parts.length; i++) {

						parameterLength =
							parametersObject[parts[i]
								.substring(1, parts[i].length - 1)];

						if (parameterLength == undefined) {

							continue;

						}
						else {

							templateTextReplaced =
								templateTextReplaced.replace(parts[i], generateString(parameterLength));

						}
					}
				}

				var messageLength = 0;

				// length of special chars if gsm encoding

				if (parametersObject["charset"] == "gsm") {

					if (isGsm(templateTextReplaced)) {
						messageLength = gsmlen (templateTextReplaced);
					}
					else {
						messageLength = "--";
					}

				}
				else {
					messageLength = templateTextReplaced.length;
				}

				$("textarea", row).text (templateText);

				$(row).find ("td > span.templatechars").get(0).innerText =
					"Your template has " +
					messageLength +
					" characters. (Min. Length: " +
					minLength +
					" - Max. Length: " +
					maxLength +
					")";

				var aux = $(document).find ('input[type="submit"]');

				if (messageLength < parseInt(minLength) ||
					messageLength > parseInt(maxLength)) {

					$(document).find ('input[type="submit"]').prop ("disabled", true);
					$(row).find ("td > span.templatechars").css("background-color", "red");

					if (!thisBloqued) {
						bloqued = bloqued + 1;
						thisBloqued = true;
					}
				}
				else {

					$(row).find ("td > span.templatechars").css("background-color", "whitesmoke");

					if (thisBloqued) {
						thisBloqued = false;
						bloqued = bloqued - 1;
					}

					if (bloqued == 0) {
						$(document).find ('input[type="submit"]').prop ("disabled", false);
					}

				}

			}

			$(row).find ("textarea").keyup (updateCharCount);
			$(row).find ("textarea").keydown (updateCharCount);
			$(row).find ("textarea").change (updateCharCount);
		}

	});

});