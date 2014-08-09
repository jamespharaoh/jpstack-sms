
SELECT object_type_insert (
	'sms_simple_tracker',
	'simple SMS tracker',
	'slice',
	1);

SELECT object_type_insert (
	'sms_simple_tracker_route',
	'sms simple tracker route',
	'sms_simple_tracker',
	4);

SELECT object_type_insert (
	'sms_simple_tracker_number',
	'sms simple tracker number',
	'sms_simple_tracker',
	3);
