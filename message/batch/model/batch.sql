--------------------------------------------------------- TABLE batch_type

CREATE OR REPLACE FUNCTION batch_type_insert (
	text,
	text,
	text,
	text)
RETURNS int AS $$

DECLARE

	the_subject_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;
	the_batch_object_type_code ALIAS FOR $4;

	the_subject_object_type_id int;
	new_id int;
	the_batch_object_type_id int;

BEGIN

	SELECT INTO the_subject_object_type_id id
	FROM object_type
	WHERE code = the_subject_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Object type not found: %',
			the_subject_object_type_code;

	END IF;

	SELECT INTO the_batch_object_type_id id
	FROM object_type
	WHERE code = the_batch_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Object type not found: %',
			the_batch_object_type_code;

	END IF;

	new_id :=
		nextval ('batch_type_id_seq');

	INSERT INTO batch_type (
		id,
		subject_object_type_id,
		code,
		description,
		batch_object_type_id)
	VALUES (
		new_id,
		the_subject_object_type_id,
		new_code,
		new_description,
		the_batch_object_type_id);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';
