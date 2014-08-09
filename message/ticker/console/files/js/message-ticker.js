var messageTicker = {

	addRow: function addRow (
			rowClass,
			threadColor,
			url,
			cells,
			mediaUrls,
			messageId,
			statusClass,
			statusChar) {

		var table =
			document.getElementById ('tickerTable');

		var row =
			table.insertRow (1);

		row.className =
			rowClass;

		try {

			row.addEventListener ('click', {
				url: url,

				handleEvent: function (evt) {

					top.frames ['main'].location =
						this.url;

				}

			}, false);

		} catch (exception) {

		}

		var cell =
			row.insertCell (0);

		cell.style.background =
			threadColor;

		var textNode =
			document.createTextNode (' ');

		cell.appendChild (textNode);

		for (var i = 0; i < cells.length; i++) {

			cell =
				row.insertCell (i + 1);

			if (i == cells.length - 1
					&& mediaUrls.length > 0) {

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

			var textNode =
				document.createTextNode (cells [i]);

			cell.appendChild (textNode);

		}

		cell =
			row.insertCell (i + 1);

		cell.id =
			'status-' + messageId;

		cell.className =
			statusClass;

		cell.align =
			'center';

		textNode =
			document.createTextNode (statusChar);

		cell.appendChild (textNode);

		if (table.rows.length
				> messageTickerParams.maxEntries + 1) {

			table.deleteRow (
				table.rows.length - 1);

		}

	},

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
