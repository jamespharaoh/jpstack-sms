---------------------------------------- INSERT object_type

SELECT object_type_insert (
	'keyword_set',
	'keyword set',
	'slice',
	1);

SELECT object_type_insert (
	'keyword',
	'keyword',
	'keyword_set',
	4);

SELECT object_type_insert (
	'keyword_set_fallback',
	'keyword set fallback',
	'keyword_set',
	3);

---------------------------------------- INSERT priv_type

SELECT priv_type_insert (
	'keyword_set',
	'manage',
	'Manage',
	'Full control of keyword set',
	true);

---------------------------------------- INSERT command_type

SELECT command_type_insert (
	'keyword_set',
	'default',
	'Keyword set');
