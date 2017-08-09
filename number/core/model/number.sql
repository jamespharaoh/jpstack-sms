
---------------------------------------- FUNCTION number_id

CREATE OR REPLACE FUNCTION number_id (text)
RETURNS int AS $$

	SELECT id
	FROM number
	WHERE number = $1;

$$ LANGUAGE SQL STABLE;

---------------------------------------- FUNCTION number

CREATE OR REPLACE FUNCTION number (int)
RETURNS text AS $$

	SELECT number
	FROM number
	WHERE id = $1;

$$ LANGUAGE SQL STABLE;
