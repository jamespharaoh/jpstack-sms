SELECT priv_type_insert (
	'root',
	'inbox_view',
	'View inbox',
	'View received messages waiting to be processed',
	true);

SELECT event_type_insert (
	'number_network_from_message',
	'Network for %0 changed from %1 to %2 due to %3');
