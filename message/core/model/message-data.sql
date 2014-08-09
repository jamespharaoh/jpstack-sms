
SELECT object_type_insert (
	'message_type',
	'message type',
	'root',
	1);

SELECT object_type_insert (
	'message_report_code',
	'message report code',
	'root',
	1);

SELECT object_type_insert (
	'message_report',
	'message report',
	'message',
	3);

SELECT object_type_insert (
	'message_expiry',
	'message expiry',
	'message',
	4);

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
