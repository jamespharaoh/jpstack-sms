---------------------------------------- TABLE route_tester

CREATE OR REPLACE FUNCTION route_tester_insert (text, text, text, text, int)
RETURNS int AS $$
	DECLARE
		the_route_code ALIAS FOR $1;
		new_route_number ALIAS FOR $2;
		new_dest_number ALIAS FOR $3;
		new_dest_keyword ALIAS FOR $4;
		new_interval_secs ALIAS FOR $5;
		the_route_id int;
		new_id int;
	BEGIN

		SELECT INTO the_route_id routeid
		FROM route
		WHERE code = the_route_code;

		IF NOT FOUND THEN
			RAISE EXCEPTION 'Route not found with code: %', the_route_code;
		END IF;

		new_id := nextval ('route_tester_id_seq');

		INSERT INTO route_tester (id, route_id, route_number, dest_number, dest_keyword, interval_secs)
		VALUES (new_id, the_route_id, new_route_number, new_dest_number, new_dest_keyword, new_interval_secs);

		RETURN new_id;

	END;
$$ LANGUAGE 'plpgsql';

---------------------------------------- INSERT object_type

--SELECT object_type_insert (
--	'route_tester',
--	'Route tester',
--	'root',
--	1);
