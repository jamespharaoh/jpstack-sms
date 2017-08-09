---------------------------------------- TABLE message_type

CREATE OR REPLACE FUNCTION message_type_insert (
	text,
	text)
RETURNS int AS $$

DECLARE
	new_code ALIAS FOR $1;
	new_description ALIAS FOR $2;
	new_id int;
BEGIN

	SELECT INTO new_id id
	FROM message_type
	WHERE code = new_code;

	IF FOUND THEN

		UPDATE message_type
		SET description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id :=
		nextval ('message_type_id_seq');

	INSERT INTO message_type (
		id,
		code,
		description)
	VALUES (
		new_id,
		new_code,
		new_description);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION message_type_id (
	text)
RETURNS int AS $$

DECLARE

	the_code ALIAS FOR $1;

	the_id int;

BEGIN

	SELECT INTO the_id id
	FROM message_type
	WHERE code = the_code;

	IF NOT FOUND THEN

		RAISE EXCEPTION
			'Unknown message type: %',
			the_code;

	END IF;

	RETURN the_id;

END;

$$ LANGUAGE 'plpgsql';

------------------------------------------------------------ INSERT message_type

SELECT message_type_insert (
	'sms',
	'SMS');

SELECT message_type_insert (
	'mms',
	'MMS');

SELECT message_type_insert (
	'wap_push',
	'WAP push');
