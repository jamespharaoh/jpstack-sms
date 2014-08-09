
-- message_stats

CREATE INDEX message_stats_date
ON message_stats (date);

ALTER TABLE message_stats ALTER in_total SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_total SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_pending SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_cancelled SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_failed SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_sent SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_delivered SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_undelivered SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_submitted SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_report_timed_out SET DEFAULT 0;
ALTER TABLE message_stats ALTER out_held SET DEFAULT 0;

---------------------------------------- TABLE message_stats_queue

CREATE SEQUENCE message_stats_queue_id_seq;

CREATE TABLE message_stats_queue (

	id int
		PRIMARY KEY
		DEFAULT nextval ('message_stats_queue_id_seq'),

	service_id int
		NOT NULL,

	route_id int
		NOT NULL,

	affiliate_id int
		NOT NULL,

	batch_id int
		NOT NULL,

	network_id int
		NOT NULL,

	date date
		NOT NULL,

	direction int
		NOT NULL,

	status int
		NOT NULL,

	diff int
		NOT NULL

);

-- this function must be called periodically to ensure message_stats is up to date

CREATE OR REPLACE FUNCTION message_stats_queue_process ()
RETURNS void AS $$

DECLARE
	row RECORD;
	update_counter int;
	row_counter int;
BEGIN

	row_counter := (
		SELECT count (*)
		FROM message_stats_queue
	);

	update_counter := 0;

	FOR row
	IN

		SELECT service_id,
			route_id,
			affiliate_id,
			batch_id,
			network_id,
			date,
			direction,
			status,
			sum (diff) AS diff

		FROM message_stats_queue

		GROUP BY
			service_id,
			route_id,
			affiliate_id,
			batch_id,
			network_id,
			date,
			direction,
			status

	LOOP

		PERFORM message_stats_update (
			row.service_id,
			row.route_id,
			row.affiliate_id,
			row.batch_id,
			row.network_id,
			row.date,
			row.direction,
			row.status,
			row.diff::integer);

		update_counter := update_counter + 1;

	END LOOP;

	DELETE FROM message_stats_queue;

	RAISE NOTICE
		'Processed % rows in % updates',
		row_counter,
		update_counter;

	RETURN;

END;

$$ LANGUAGE 'plpgsql';

---------------------------------------- FUNCTION message_stats_lookup_or_create

CREATE OR REPLACE FUNCTION message_stats_lookup_or_create (
	int,
	int,
	int,
	int,
	int,
	date)
RETURNS int AS $$

DECLARE

	the_service_id ALIAS FOR $1;
	the_route_id ALIAS FOR $2;
	the_affiliate_id ALIAS FOR $3;
	the_batch_id ALIAS FOR $4;
	the_network_id ALIAS FOR $5;
	the_date ALIAS FOR $6;

	the_message_stats_id int;

BEGIN

	SELECT INTO the_message_stats_id id
	FROM message_stats
	WHERE service_id = the_service_id
		AND route_id = the_route_id
		AND affiliate_id = the_affiliate_id
		AND batch_id = the_batch_id
		AND network_id = the_network_id
		AND date = the_date;

	IF FOUND THEN
		RETURN the_message_stats_id;
	END IF;

	the_message_stats_id :=
		nextval ('message_stats_id_seq');

	INSERT INTO message_stats (
		id,
		service_id,
		route_id,
		affiliate_id,
		batch_id,
		network_id,
		date)
	VALUES (
		the_message_stats_id,
		the_service_id,
		the_route_id,
		the_affiliate_id,
		the_batch_id,
		the_network_id,
		the_date);

	RETURN the_message_stats_id;

END;

$$ LANGUAGE 'plpgsql';

---------------------------------------- FUNCTION message_stats_update

CREATE OR REPLACE FUNCTION message_stats_update (
	int,
	int,
	int,
	int,
	int,
	date,
	int,
	int,
	int)
RETURNS void AS $$

DECLARE

	the_service_id ALIAS FOR $1;
	the_route_id ALIAS FOR $2;
	the_affiliate_id ALIAS FOR $3;
	the_batch_id ALIAS FOR $4;
	the_network_id ALIAS FOR $5;
	the_date ALIAS FOR $6;
	the_direction ALIAS FOR $7;
	the_status ALIAS FOR $8;
	the_diff ALIAS FOR $9;

	the_message_stats_id int;

BEGIN

	IF the_service_id IS NULL THEN
		RETURN;
	END IF;

	the_message_stats_id :=
		message_stats_lookup_or_create (
			the_service_id,
			the_route_id,
			the_affiliate_id,
			the_batch_id,
			the_network_id,
			the_date);

	IF the_direction = 0 THEN

		UPDATE message_stats
		SET in_total = in_total + the_diff
		WHERE id = the_message_stats_id;

	ELSE

		IF the_status = 0 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_pending = out_pending + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 2 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_cancelled = out_cancelled + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 3 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_failed = out_failed + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 4 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_sent = out_sent + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 5 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_delivered = out_delivered + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 6 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_undelivered = out_undelivered + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 10 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_submitted = out_submitted + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 11 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_report_timed_out = out_report_timed_out + the_diff
			WHERE id = the_message_stats_id;

		ELSIF the_status = 12 THEN

			UPDATE message_stats
			SET out_total = out_total + the_diff,
				out_held = out_held + the_diff
			WHERE id = the_message_stats_id;

		ELSE

			UPDATE message_stats
			SET out_total = out_total + the_diff
			WHERE id = the_message_stats_id;

		END IF;

	END IF;

	RETURN;

	END;

$$ LANGUAGE 'plpgsql';

/*

-- recreate message_stats table from scratch

BEGIN;

LOCK TABLE message_stats;
LOCK TABLE message_stats_queue;

DELETE FROM message_stats;
DELETE FROM message_stats_queue;

INSERT INTO message_stats (
	service_id, route_id, affiliate_id, batch_id, network_id, date,
	in_total, out_total,
	out_pending, out_cancelled, out_failed,
	out_sent, out_submitted, out_delivered, out_undelivered, out_report_timed_out, out_held)
SELECT
	service_id, m.routeid, m.affiliate_id, m.batch_id, m.networkid, m.date,
	count (CASE WHEN m.direction = 0 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 0 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 2 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 3 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 4 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 10 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 5 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 6 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 11 THEN 1 ELSE NULL END),
	count (CASE WHEN m.direction = 1 AND m.status = 12 THEN 1 ELSE NULL END)
FROM message AS m
WHERE service_id IS NOT NULL
GROUP BY m.service_id, m.routeid, m.affiliate_id, m.batch_id, m.networkid, m.date;

COMMIT;

*/