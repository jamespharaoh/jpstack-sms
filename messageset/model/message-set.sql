CREATE OR REPLACE FUNCTION message_set_type_id (text)
RETURNS integer AS $$

	SELECT id
	FROM message_set_type
	WHERE code = $1;

$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION message_set_type_insert (text, text, text)
RETURNS int AS $$

DECLARE

	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;

	the_parent_object_type_id int;
	new_id int;

BEGIN

	SELECT INTO the_parent_object_type_id id
	FROM object_type
	WHERE code = the_parent_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Object type not found: %',
			the_parent_object_type_code;

	END IF;

	new_id :=
		nextval ('message_set_type_id_seq');

	INSERT INTO message_set_type (
		id,
		parent_object_type_id,
		code,
		description
	) VALUES (
		new_id,
		the_parent_object_type_id,
		new_code,
		new_description
	);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';
