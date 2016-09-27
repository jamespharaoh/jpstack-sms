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

---------------------------------------- TABLE sms_outbox_multipart_link

CREATE INDEX sms_outbox_multipart_link_main_message
ON sms_outbox_multipart_link (main_message_id);

-- ex: noet ts=4 filetype=sql