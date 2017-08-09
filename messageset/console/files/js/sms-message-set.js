function form_magic () {

	for (var i = 0; i < " + numMessages + "; i++) {

		var check = document.getElementById ('enabled_' + i);
		var route = document.getElementById ('route_' + i);
		var number = document.getElementById ('number_' + i);
		var message = document.getElementById ('message_' + i);

		route.disabled = ! check.checked;
		number.disabled = ! check.checked;
		message.disabled = ! check.checked;

	}

}

// ex: noet ts=4 filetype=javascript