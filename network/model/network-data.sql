
SELECT object_type_insert (
	'network',
	'network',
	'root',
	1);

SELECT object_type_insert (
	'network_prefix',
	'network prefix',
	'root',
	3);

SELECT priv_type_insert (
	'network',
	'manage',
	'Full control of this network',
	'',
	true);
