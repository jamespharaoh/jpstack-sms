
SELECT object_type_insert (
	'batch_type',
	'batch type',
	'object_type',
	2);

SELECT object_type_insert (
	'batch_subject',
	'batch subject',
	NULL,
	2);

SELECT object_type_insert (
	'batch',
	'batch',
	'batch_subject',
	3);

INSERT INTO batch_type (
	id,
	subject_object_type_id,
	code,
	description,
	batch_object_type_id)
VALUES (
	0,
	0,
	'system',
	'System',
	0);

INSERT INTO batch_subject (
	id,
	type_id,
	code,
	parent_object_id,
	parent_object_type_id)
VALUES (
	0,
	0,
	'system',
	0,
	0);

INSERT INTO batch (
	id,
	parent_object_type_id,
	parent_object_id,
	code,
	subject_id)
VALUES (
	0,
	0,
	0,
	'system',
	0);
