
CREATE OR REPLACE FUNCTION delivery_type_insert (text, text)
RETURNS int AS $$

DECLARE
	new_code ALIAS FOR $1;
	new_description ALIAS FOR $2;
	new_id int;
BEGIN

	SELECT INTO new_id id
	FROM delivery_type
	WHERE code = new_code;

	IF FOUND THEN

		UPDATE delivery_type
		SET description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id := nextval ('delivery_type_id_seq');

	INSERT INTO delivery_type (id, code, description)
	VALUES (new_id, new_code, new_description);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION delivery_type_id (text)
RETURNS int AS $$

	SELECT id
	FROM delivery_type
	WHERE code = $1;

$$ LANGUAGE sql STABLE;
