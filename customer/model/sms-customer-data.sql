SELECT priv_type_insert (
	'sms_customer_manager',
	'manage',
	'Full control',
	'Full control of this SMS customer manager',
	true);

SELECT service_type_insert (
	'sms_customer_manager',
	'welcome',
	'Welcome message');

SELECT service_type_insert (
	'sms_customer_manager',
	'warning',
	'Warning message');

SELECT priv_type_insert (
	'sms_customer_manager',
	'stats',
	'View message stats',
	'View message stats for this SMS customer manager',
	true);

SELECT priv_type_insert (
	'sms_customer_manager',
	'messages',
	'View message history',
	'View message history for this SMS customer manager',
	true);

SELECT command_type_insert (
	'sms_customer_manager',
	'stop',
	'Stop');
