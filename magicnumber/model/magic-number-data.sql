
-- magic number set

SELECT object_type_insert (
	'magic_number_set',
	'magic number set',
	'root',
	1);

SELECT priv_type_insert (
	'magic_number_set',
	'manage',
	'Full control',
	'Full control of this magic number set',
	true);

-- magic number

SELECT object_type_insert (
	'magic_number',
	'magic number',
	'magic_number_set',
	3);

-- magic number use

SELECT object_type_insert (
	'magic_number_use',
	'magic number use',
	'magic_number',
	3);

-- root

SELECT command_type_insert (
	'root',
	'magic_number',
	'Magic numbers');
