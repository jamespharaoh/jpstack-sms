-- FUNCTION number_lookup_type_insert

CREATE OR REPLACE FUNCTION number_lookup_type_insert (
	text,
	text,
	text)
RETURNS integer AS $$

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
	FROM number_lookup_type
	WHERE parent_object_type_id = the_parent_object_type_id
		AND code = new_code;

	IF FOUND THEN

		UPDATE number_lookup_type
		SET description = new_description
		WHERE id = new_id;

		RETURN new_id;

	END IF;

	new_id :=
		nextval ('number_lookup_type_id_seq');

	INSERT INTO number_lookup_type (
		id,
		code,
		description,
		parent_object_type_id)
	VALUES (
		new_id,
		new_code,
		new_description,
		the_parent_object_type_id);

	RETURN new_id;

END;

$$ LANGUAGE 'plpgsql';

-- FUNCTION number_lookup_create_missing

CREATE OR REPLACE FUNCTION number_lookup_create_missing (
	text,
	text,
	bool,
	bool)
RETURNS void AS $$
	DECLARE

		the_object_type_code_like ALIAS FOR $1;
		the_number_lookup_type_code_like ALIAS FOR $2;
		the_actually_create ALIAS FOR $3;
		the_make_noise ALIAS FOR $4;

		the_object_type record;
		the_number_lookup_type record;

		has_shown_object_type bool;
		has_shown_number_lookup_type bool;

		the_record record;

		the_count int;

	BEGIN

		IF the_make_noise THEN

			RAISE NOTICE 'Creating missing number lookups';

		END IF;

		the_count := 0;

		FOR the_object_type IN

			SELECT * FROM object_type
			WHERE the_object_type_code_like IS NULL
				OR code LIKE the_object_type_code_like
			ORDER BY code

		LOOP

			has_shown_object_type := false;

			FOR the_number_lookup_type IN

				SELECT * FROM number_lookup_type
				WHERE parent_object_type_id = the_object_type.id
					AND (the_number_lookup_type_code_like IS NULL
						OR code LIKE the_number_lookup_type_code_like)
				ORDER BY code

			LOOP

				has_shown_number_lookup_type := false;

				FOR the_record IN EXECUTE
					'SELECT parent.* ' ||
					'FROM ' || quote_ident (the_object_type.code) ||
						' parent ' ||
					'LEFT JOIN number_lookup ' ||
						'ON number_lookup.number_lookup_type_id = ' || the_number_lookup_type.id || ' ' ||
							'AND number_lookup.code = ' ||
								quote_literal (the_number_lookup_type.code) || ' ' ||
							'AND number_lookup.parent_object_type_id = ' ||
								the_number_lookup_type.parent_object_type_id || ' ' ||
							'AND number_lookup.parent_object_id = parent.id ' ||
					'WHERE number_lookup.id IS NULL'
				LOOP

					IF NOT has_shown_object_type AND the_make_noise THEN

						RAISE NOTICE
							'- Object type %',
							the_object_type.code;

						has_shown_object_type := true;

					END IF;

					IF NOT has_shown_number_lookup_type AND the_make_noise THEN

						RAISE NOTICE
							'	- number lookup type %',
							the_number_lookup_type.code;

						has_shown_number_lookup_type := true;

					END IF;

					IF the_make_noise THEN

						RAISE NOTICE
							'		- Object % (%)',
							the_record.code,
							the_record.id;

					END IF;

					IF the_actually_create THEN

						INSERT INTO number_lookup (
							number_lookup_type_id,
							parent_object_type_id,
							parent_object_id,
							code)
						SELECT
							the_number_lookup_type.id,
							the_object_type.id,
							the_record.id,
							the_number_lookup_type.code;

					END IF;

					the_count := the_count + 1;

				END LOOP;

			END LOOP;

		END LOOP;

		IF the_make_noise THEN

			RAISE NOTICE
				'Created % missing number lookups',
				the_count;

		END IF;

	END;
$$ LANGUAGE plpgsql;
