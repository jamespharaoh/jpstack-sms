var wbsSmsMessageStatus = {};

$(function () {

	wbsStatus.handlerRegister ("sms-messages",
	function (data) {

		// inbox

		if (data.inbox) {

			$("#inboxCell").text (
				String (data.inbox) + " items in inbox");

			$("#inboxRow").show ();

		} else {

			$("#inboxRow").hide ();

		}

		// inbox

		if (data.outbox) {

			$("#outboxCell").text (
				String (data.outbox) + " items in outbox");

			$("#outboxRow").show ();

		} else {

			$("#outboxRow").hide ();

		}

	});

});

// ex: noet ts=4 filetype=javascript