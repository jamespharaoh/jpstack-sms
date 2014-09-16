
SELECT object_type_insert (
	'number_list',
	'number list',
	'slice',
	1);

SELECT object_type_insert (
	'number_list_number',
	'number list number',
	'number_list',
	3);

SELECT object_type_insert (
	'number_list_update',
	'number list update',
	'number_list',
	3);

SELECT priv_type_insert (
	'slice',
	'number_list_create',
	'Create number lists',
	'Create number lists in this slice',
	true);

SELECT priv_type_insert (
	'number_list',
	'manage',
	'Full control',
	'Full control of this number list',
	true);

SELECT priv_type_insert (
	'number_list',
	'number_list_add',
	'Add numbers',
	'Add numbers to this number list',
	true);

SELECT priv_type_insert (
	'number_list',
	'number_list_remove',
	'Remove numbers',
	'Remove numbers from this number list',
	true);

SELECT priv_type_insert (
	'number_list',
	'number_list_search',
	'Search numbers',
	'Search for specific numbers in this number list',
	true);

SELECT priv_type_insert (
	'number_list',
	'number_list_browse',
	'Browse numbers',
	'Browse all numbers in this number list',
	true);

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
