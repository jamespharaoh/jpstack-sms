function gsmlen (s) {
	s = s.replace(/[\]\[{}\\|^~\u20ac]/g, "__");
	return s.length;
}

function getParams (str) {
	var re = /{[^{}]+}/g, s;
	var params = new Array;
	while (s = re.exec (str)) {
		params.push (s [0].substring (1, s [0].length - 1));
	}
	return params;
}

function zapParams (str) {
	var re = /{[^{}]+}/g, s;
	return str.replace (re, "");
}

function gsmCharCount (control, container) {

	var max =
		arguments.length >= 3
			? arguments [2]
			: 160;

	var paramLengths =
		arguments.length >= 4
			? arguments [3]
			: [];

	var text = container.firstChild;

	// check its valid

	if (! isGsm (control.value)) {
		text.data = "ERR";
		return;
	}

	// check params are alright

	var params = getParams (control.value);
	var paramsLen = 0;
outerLoop:

	for (var i = 0; i < params.length; i++) {
		var param = params [i];

		for (var j = 0; j < paramLengths.length; j++) {
			var paramLength = paramLengths [j];

			if (paramLength.name == param) {
				paramsLen += paramLength.maxLen;
				continue outerLoop;
			}

		}

		text.data = "ERR";
		return;

	}

	// get string length

	var len = gsmlen (zapParams (control.value)) + paramsLen;

	// work out the text

	var str;
	if (max == 0) {
		str = String (len);
	} else {
		if (len <= max) {
			str = "-" + String (max - len);
		} else {
			str = "+" + String (len - max);
		}
	}

	// set the text

	text.data = str;

}

// TODO this is hideous, fix it
function gsmCharCountMultiple2 (control, container) {

	var max = arguments.length >= 3 ? arguments [2] : 130;
	var text = container.firstChild;

	// check its valid

	if (! isGsm (control.value)) {
		text.data = "ERR";
		return;
	}

	// get string length

	var len = gsmlen (zapParams (control.value));

	// work out the text

	var messages = 0;
	var count = len;

	while (count > 0) {
		messages = messages + 1;
		count -= 130;
	}

	// set the text

	text.data = len + " chars = " + messages + " messages";

}

var gsmRegExp = new RegExp (
	"^[" +

	// single-byte characters

	"@\\u00a3$\\u00a5\\u00e8\\u00e9\\u00f9\\u00ec\\u00f2\\u00e7\\n\\u00d8\\u00f8\\r\\u00c5\\u00e5" +
	"\\u0394_\\u03a6\\u0393\\u039b\\u03a9\\u03a0\\u03a8\\u03a3\\u0398\\u039e\\u00c6\\u00e6\\u00df\\u00c9" +
	" !\"#\\u00a4%&'()*+,\\-\\./" +
	"0123456789:;<=>?" +
	"\\u00a1ABCDEFGHIJKLMNO" +
	"PQRSTUVWXYZ\\u00c4\\u00d6\\u00d1\\u00dc\\u00a7" +
	"\\u00bfabcdefghijklmno" +
	"pqrstuvwxyz\\u00e4\\u00f6\\u00f1\\u00fc\\u00e0" +

	// double byte characters

	"\\u000c" + // form feed
	"^{|}" +
	"\\\\" + // backslash
	"\\[" + // left square brace
	"~" + // tilde
	"\\]" + // right square brace
	"\\u20ac" + // euro currency symbol

	"]*$");

function isGsm (s) {
	var a = gsmRegExp.test (s);
	return a;
}

$(function () {
	$(".gsmCharCount").each (function () {
		var control = $(this);
		var maxLength = Number (control.attr ("data-char-count-max"));
		var countId = control.attr ("data-char-count-id");
		var count = $("#" + countId);
		var updateFunc = function () {
			gsmCharCount (control [0], count [0], maxLength);
		};
		control.focus (updateFunc);
		control.keyup (updateFunc);
		updateFunc ();
	});
});

function gsmMessageSplit (templates, message) {

	function splitOne (template, message, page, pages) {

		template =
			template

			.replace (
				"{page}",
				String (page))

			.replace (
				"{pages}",
				String (pages));

		var spareLength =
			160 - gsmlen (
				template.replace (
					"{message}",
					""));

		// check if whole message will fit

		if (gsmlen (message) <= spareLength) {

			return {

				part: template.replace (
					"{message}",
					message),

				rest: "",

			};

		}

		// work out what will fit

		var maxSplit = spareLength;

		while (
			gsmlen (
				message.substring (0, maxSplit)
			) > spareLength
		) {

			maxSplit --;

		}

		// reduce further to a word boundary

		var minSplit =
			(maxSplit + 2) * 2 / 3;

		for (var d = maxSplit; d >= minSplit; d --) {

			if (message.charAt (d) != " ") {
				continue;
			}

			return {

				part:
					template.replace (
						"{message}",
						message.substring (0, d).trim ()),

				rest:
					message.substring (d).trim (),

			};

		}

		// fall back to a forced split

		return {

			part:
				template.replace (
					"{message}",
					message.substring (0, maxSplit)),

			rest:
				message.substring (maxSplit).trim (),

		};

	}

	// try and split message

	for (
		totalParts = 1;
		true;
		totalParts ++
	) {

		var messageParts = []
		var rest = message;

		for (
			thisPart = 0;
			thisPart < totalParts;
			thisPart ++
		) {

			var thisTemplate;

			if (totalParts == 1) {

				thisTemplate =
					templates.single;

			} else if (thisPart == 0) {

				thisTemplate =
					templates.first;

			} else if (thisPart == totalParts - 1) {

				thisTemplate =
					templates.last;

			} else {

				thisTemplate =
					templates.middle;

			}

			var result =
				splitOne (
					thisTemplate,
					rest,
					thisPart + 1,
					totalParts);

			messageParts.push (
				result.part);

			rest =
				result.rest;

		}

		if (rest == "") {
			return messageParts;
		}

	}

}
