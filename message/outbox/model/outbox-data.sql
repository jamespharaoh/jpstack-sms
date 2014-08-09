
SELECT object_type_insert (
	'outbox',
	'outbox',
	'message',
	4);

SELECT object_type_insert (
	'failed_message',
	'failed message',
	'message',
	2);

SELECT object_type_insert (
	'route_outbox_summary',
	'route outbox summary',
	'route',
	2);

SELECT object_type_insert (
	'sms_outbox_attempt',
	'sms outbox attempt',
	'message',
	4);

SELECT priv_type_insert (
	'root',
	'outbox_view',
	'View outbox',
	'View outbound messages waiting to be sent',
	true);

SELECT event_type_insert (
	'sms_outbox_cancelled',
	'%0 cancelled %1');

SELECT event_type_insert (
	'sms_outbox_retried',
	'%0 cancelled %1');
