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
