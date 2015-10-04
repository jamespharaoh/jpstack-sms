SELECT event_type_insert (
	'sms_outbox_cancelled',
	'%0 cancelled %1');

SELECT event_type_insert (
	'sms_outbox_retried',
	'%0 cancelled %1');
