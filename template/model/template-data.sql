
SELECT object_type_insert (
	'template_type',
	'template type',
	'object_type',
	2);

SELECT object_type_insert (
	'template',
	'template',
	NULL,
	2);

SELECT object_type_insert (
	'template_version',
	'template version',
	'template',
	3);

SELECT object_type_insert (
	'template_part',
	'template part',
	'template_version',
	2);
