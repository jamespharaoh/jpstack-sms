INSERT INTO batch_type (
	id,
	subject_object_type_id,
	code,
	description,
	batch_object_type_id)
VALUES (
	0,
	object_type_id ('root'),
	'system',
	'System',
	object_type_id ('root'));

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
	object_type_id ('root'));

INSERT INTO batch (
	id,
	parent_object_type_id,
	parent_object_id,
	code,
	subject_id)
VALUES (
	0,
	object_type_id ('root'),
	0,
	'system',
	0);
