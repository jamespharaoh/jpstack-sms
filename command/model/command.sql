---------------------------------------- INSERT command_type_insert

CREATE OR REPLACE FUNCTION command_type_insert (
	text,
	text,
	text)
RETURNS int AS $$

DECLARE

	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;

	the_parent_object_type_id int;
	new_id int;

BEGIN

	-- lookup parent object type

	SELECT INTO the_parent_object_type_id id
	FROM object_type
	WHERE code = the_parent_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Parent object type % not found',
			the_parent_object_type_code;

	END IF;

	-- update existing if found

	SELECT INTO new_id id
	FROM command_type
	WHERE parent_object_type_id = the_parent_object_type_id
		AND code = new_code;

	IF FOUND THEN

		UPDATE command_type
		SET code = new_code,
			description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id :=
		nextval ('command_type_id_seq');

	INSERT INTO command_type (
		id,
		code,
		description,
		parent_object_type_id,
		deleted)
	VALUES (
		new_id,
		new_code,
		new_description,
		the_parent_object_type_id,
		false);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';
