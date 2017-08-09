----------------------------------------------------------------- TABLE network

CREATE OR REPLACE FUNCTION network_insert (int, text, text)
RETURNS void AS $$

DECLARE
	the_network_id ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;
BEGIN

	PERFORM *
	FROM network
	WHERE id = $1;

	IF FOUND THEN

		UPDATE network
		SET code = new_code,
			description = new_description
		WHERE id = the_network_id;

	ELSE

		INSERT INTO network (id, code, description)
		VALUES (the_network_id, new_code, new_description);

	END IF;

END;
$$ LANGUAGE 'plpgsql';

---------------------------------------- TABLE network_prefix

CREATE OR REPLACE FUNCTION network_prefix (text) RETURNS int AS '
	SELECT network_id FROM network_prefix WHERE prefix = $1;
' LANGUAGE SQL STABLE;
