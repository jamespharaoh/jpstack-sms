---------------------------------------- TABLE sender

CREATE OR REPLACE FUNCTION sender_id (
	text
) RETURNS int AS $$

	SELECT id
	FROM sender
	WHERE code = $1;

$$ LANGUAGE SQL;

---------------------------------------- FUNCTION sender_insert

CREATE OR REPLACE FUNCTION sender_insert (
	text,
	text
) RETURNS int AS $$

DECLARE

	new_code ALIAS FOR $1;
	new_description ALIAS FOR $2;

	new_sender_id int;

BEGIN

	new_sender_id :=
		nextval ('sender_id_seq');

	INSERT INTO sender (
		id,
		code,
		description)
	VALUES (
		new_sender_id,
		new_code,
		new_description);

	RETURN new_sender_id;

END;

$$ LANGUAGE 'plpgsql';
