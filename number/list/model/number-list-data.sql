SELECT event_type_insert (
	'number_list_numbers_added',
	'%0 added %1 numbers to %2');

SELECT event_type_insert (
	'number_list_numbers_removed',
	'%0 removed %1 numbers from %2');

SELECT number_lookup_type_insert (
	'number_list',
	'default',
	'Lookup number in list');
