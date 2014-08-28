
SELECT object_type_insert (
	'sms_customer_manager',
	'sms customer manager',
	'slice',
	1);

SELECT priv_type_insert (
	'sms_customer_manager',
	'manage',
	'Full control',
	'Full control of this SMS customer manager',
	true);

SELECT object_type_insert (
	'sms_customer_template',
	'sms customer template',
	'sms_customer_manager',
	2);

SELECT object_type_insert (
	'sms_customer',
	'sms customer',
	'sms_customer_manager',
	3);

SELECT object_type_insert (
	'sms_customer_session',
	'sms customer session',
	'sms_customer',
	3);

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
