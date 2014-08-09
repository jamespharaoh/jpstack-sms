---------------------------------------- TABLE outbox

CREATE INDEX outbox_order
ON outbox (pri, retry_time);

CREATE OR REPLACE VIEW route_outbox_summary AS
SELECT
	route_id,
	count (*) AS num_messages,
	min (created_time) AS oldest_time
FROM outbox
GROUP BY route_id;
