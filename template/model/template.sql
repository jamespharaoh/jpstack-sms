
CREATE OR REPLACE FUNCTION template_type_insert (text, text, text)
RETURNS int AS $$

DECLARE

	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;

	parent_object_type_id int;
	new_template_id int;

BEGIN

	SELECT INTO parent_object_type_id id
	FROM object_type
	WHERE code = the_parent_object_type_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Object type not found: %',
			the_parent_object_type_code;

	END IF;

	new_template_id := (
		SELECT id
		FROM template_type
		WHERE code = new_code
	);

	IF FOUND THEN

		UPDATE template_type
		SET description = new_description
		WHERE id = new_template_id;

	ELSE

		INSERT INTO template_type (id, code, description)
		VALUES (new_template_id, new_code, new_description);

	END IF;

	RETURN new_template_id;

END;

$$ LANGUAGE 'plpgsql';
