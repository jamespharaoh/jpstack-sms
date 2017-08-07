var messageTicker = {
};

messageTicker._init =
function messageTickerInit () {

	async.onConnect (
		messageTicker._handleConnect);

	async.onDisconnect (
		messageTicker._handleError);

};

messageTicker._handleConnect =
function messageTickerHandleConnect () {

	async.subscribe (
		"/sms-message-ticker/update",
		messageTicker._handleUpdate);

};

messageTicker._handleError =
function messageTickerHandleError () {

	var tableBody =
		$("#tickerTable tbody");

	tableBody.find ("tr").remove ();

	var loadingRow =
		$("<tr>")
			.addClass ("message-ticker-loading");

	tableBody.append (
		loadingRow);

	var loadingCell =
		$("<td>")
			.attr ("colspan", "6")
			.text ("Loading, please wait...");

	loadingRow.append (
		loadingCell);

};

messageTicker._handleUpdate =
function messageTickerHandleUpdate (data) {

	$("#tickerTable tbody tr.message-ticker-loading").remove ();

	data.messages.forEach (
		messageTicker._addMessage);

	data.statuses.forEach (
		messageTicker._updateStatus);

};

messageTicker._addMessage =
function messageTickerAddMessage (messageData) {

	var tableBody =
		$("#tickerTable tbody");

	var tableRow =
		$("<tr>")
			.css ("cursor", "pointer")
			.addClass ("message-ticker-message")
			.addClass ("message-id-" + messageData.messageId)
			.addClass (messageData.rowClass);

	// colour

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-colour")
			.css ("background-color", messageData.colour));

	// timestamp

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-timestamp")
			.text (messageData.timestamp));

	// number from

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-number-from")
			.text (messageData.numberFrom));

	// number to

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-number-to")
			.text (messageData.numberTo));

	// body

	var bodyCell =
		$("<td>")
			.addClass ("message-ticker-body")
			.text (messageData.body);

	if (messageData.media.length) {

		var bodyMediaDiv =
			$("<div>")
				.addClass ("floatRightThumb");

		messageData.media.forEach (function (media) {

			bodyMediaDiv.append (
				$("<span>")
					.html (media));

		});

		bodyCell.append (
			bodyMediaDiv);

	}

	tableRow.append (
		bodyCell);

	// status

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-status")
			.addClass (messageData.statusClass)
			.css ("text-align", "center")
			.text (messageData.statusCharacter));

	tableRow.click (function () {
		top.frames.main.location = messageData.link;
	});

	tableBody.prepend (tableRow);

	tableBody.slice (100, 0).remove ();

};

messageTicker._updateStatus =
function messageTickerUpdateStatus (statusData) {

	console.log ([
		"Status update message ",
		String (statusData.messageId),
		" to ",
		statusData.statusClass,
		" (",
		statusData.statusClass,
		")",
	].join (""));

	var statusCell =
		$("#tickerTable tbody")
			.find ("tr.message-id-" + statusData.messageId)
			.find ("td.message-ticker-status");

	statusCell
		.removeClass ()
		.addClass ("message-ticker-status")
		.addClass (statusData.statusClass)
		.text (statusData.statusCharacter);

};

$(messageTicker._init);

// ex: noet ts=4 filetype=javascript