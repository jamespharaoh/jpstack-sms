-- route

CREATE OR REPLACE FUNCTION route_id (text) RETURNS integer AS '
	DECLARE
		the_code ALIAS FOR $1;
		the_id int;
	BEGIN
		SELECT INTO the_id id FROM route WHERE code = the_code;
		IF NOT FOUND THEN
			RAISE EXCEPTION ''Unknown route: %'', the_code;
		END IF;
		RETURN the_id;
	END;
' LANGUAGE 'plpgsql' STABLE;
