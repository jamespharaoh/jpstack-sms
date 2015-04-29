SELECT priv_type_insert (
	'root',
	'message_notprocessed_view',
	'View not processed messages',
	'View received messages which were unable to be processed correctly',
	true);

SELECT priv_type_insert (
	'root',
	'messages',
	'View message history',
	'View message history for the entire system',
	true);

SELECT priv_type_insert (
	'root',
	'stats',
	'View message stats',
	'View message status for the entire system',
	true);

SELECT priv_type_insert (
	'slice',
	'messages',
	'View message history',
	'View message history for all services in this slice',
	true);

SELECT priv_type_insert (
	'slice',
	'stats',
	'View message stats',
	'View message status for all services in this slice',
	true);

SELECT event_type_insert (
	'message_manually_undelivered',
	'%0 manually undelivered %1');
