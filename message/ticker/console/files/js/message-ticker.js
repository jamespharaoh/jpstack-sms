var messageTicker = {
};

messageTicker._init =
function messageTickerInit () {

	async.onConnect (
		messageTicker._handleConnect);

	async.onDisconnect (
		messageTicker._handleError);

	async._keepaliveLoop ();

};

messageTicker._handleConnect =
function messageTickerHandleConnect () {

	async.subscribe (
		"/sms-message-ticker/update",
		messageTicker._handleUpdate);

};

messageTicker._handleError =
function messageTickerHandleConnect () {

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
			.addClass ("message-id-" + messageData.messageId)
			.addClass (messageData.rowClass);

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-colour")
			.css ("background-color", messageData.colour));

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-timestamp")
			.text (messageData.timestamp));

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-number-from")
			.text (messageData.numberFrom));

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-number-to")
			.text (messageData.numberTo));

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-body")
			.text (messageData.body));

	tableRow.append (
		$("<td>")
			.addClass ("message-ticker-status")
			.addClass (messageData.statusClass)
			.css ("text-align", "center")
			.text (messageData.statusCharacter));

	// TODO media

	tableRow.click (function () {
		top.frames.main.location = messageData.link;
	});

	tableBody.prepend (tableRow);

	tableBody.slice (100, 0).remove ();

};

messageTicker._updateStatus =
function messageTickerUpdateStatus (statusData) {

	var statusCell =
		$("#tickerTable tbody")
			.find ("tr.message-id-" + statusData.messageId)
			.find ("td.message-ticker-status");

	statusCell
		.removeClass ()
		.addClass (statusData.statusClass)
		.text (statusData.statusCharacter);

};

/*
				var floatDiv =
					document.createElement ('div');

				floatDiv.className =
					'floatRightThumb';

				cell.appendChild (floatDiv);

				for (var j = 0; j < mediaUrls.length; j++) {

					floatDiv.innerHTML +=
						mediaUrls [j];

				}

			}

	updateStatus: function updateStatus (
			messageId,
			statusClass,
			statusChar) {

		var cell =
			document.getElementById ('status-' + messageId);

		if (! cell)
			return;

		cell.className =
			statusClass;

		textNode =
			document.createTextNode (statusChar);

		cell.replaceChild (
			textNode,
			cell.firstChild);

	},

	handler: {

		onSuccess: function onSuccess (req) {

			eval (req.responseText);

			window.setTimeout (
				messageTicker.doUpdate,
				messageTickerParams.reloadMs);

		},

		onFailure: function onFailure (req) {

			window.setTimeout (
				messageTicker.doUpdate,
				messageTickerParams.reloadMs);

		}

	},

	generation: 0,

	doUpdate: function doUpdate () {

		rpc_simpleGet (
			'messageTicker.update' +
			'?gen=' + messageTicker.generation,
			messageTicker.handler);

	}

}
*/

$(messageTicker._init);

// ex: noet ts=4 filetype=javascript