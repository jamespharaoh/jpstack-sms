
SELECT object_type_insert (
	'inbox',
	'inbox',
	'message',
	4);

SELECT object_type_insert (
	'inbox_multipart_log',
	'inbox multipart log',
	'root',
	3);

SELECT object_type_insert (
	'inbox_multipart_buffer',
	'inbox multipart buffer',
	'root',
	4);

SELECT priv_type_insert (
	'root',
	'inbox_view',
	'View inbox',
	'View received messages waiting to be processed',
	true);

SELECT event_type_insert (
	'number_network_from_message',
	'Network for %0 changed from %1 to %2 due to %3');
