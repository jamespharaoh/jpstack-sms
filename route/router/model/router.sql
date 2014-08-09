---------------------------------------- FUNCTION router_type_insert

CREATE OR REPLACE FUNCTION router_type_insert (text, text, text)
RETURNS int AS $$

DECLARE

	the_parent_object_type_code ALIAS FOR $1;
	new_code ALIAS FOR $2;
	new_description ALIAS FOR $3;

	new_id int;
	the_parent_object_type_id int;

BEGIN

	new_id := nextval ('router_type_id_seq');

	INSERT INTO router_type (
		id,
		parent_object_type_id,
		code,
		description)
	VALUES (
		new_id,
		object_type_id (the_parent_object_type_code),
		new_code,
		new_description);

	RETURN new_id;

END;

$$ LANGUAGE plpgsql;
