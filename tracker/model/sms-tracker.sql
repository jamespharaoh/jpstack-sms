CREATE OR REPLACE FUNCTION sms_tracker_type_insert (text, text, text)
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
			'Parent object type % not found',
			the_parent_object_type_code;

	END IF;

	SELECT INTO new_id id
	FROM sms_tracker_type
	WHERE parent_object_type_id = the_parent_object_type_id
		AND code = new_code;

	IF FOUND THEN

		UPDATE sms_tracker_type
		SET description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id :=
		nextval ('sms_tracker_type_id_seq');

	INSERT INTO sms_tracker_type (id, code, description, parent_object_type_id)
	VALUES (new_id, new_code, new_description, the_parent_object_type_id);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';
