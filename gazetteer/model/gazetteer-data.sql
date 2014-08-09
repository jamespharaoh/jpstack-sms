SELECT object_type_insert (
	'gazetteer',
	'gazetteer',
	'slice',
	1);

SELECT object_type_insert (
	'gazetteer_entry',
	'gazetteer entry',
	'gazetteer',
	3);

SELECT priv_type_insert (
	'gazetteer',
	'manage',
	'Manage',
	'Full control',
	true);
