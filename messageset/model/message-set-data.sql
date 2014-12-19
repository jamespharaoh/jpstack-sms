SELECT event_type_insert (
	'messageset_message_created',
	'User %0 created message %1 in message-set %2 on route %3 with number %4, '
	|| 'value %5 and message %6');

SELECT event_type_insert (
	'messageset_message_removed',
	'User %0 removed message %1 in message-set %2');

SELECT event_type_insert (
	'messageset_message_route',
	'User %0 set route for message %1 in message-set %2 to %3');

SELECT event_type_insert (
	'messageset_message_number',
	'User %0 set number for message %1 in message-set %2 to %3');

SELECT event_type_insert (
	'messageset_message_value',
	'User %0 set value for message %1 in message-set %2 to %3');

SELECT event_type_insert (
	'messageset_message_message',
	'User %0 set message %1 in message-set %2 to %3');
