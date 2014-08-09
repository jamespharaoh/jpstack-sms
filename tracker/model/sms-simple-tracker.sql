
CREATE UNIQUE INDEX sms_simple_tracker_route_secondary_key
ON sms_simple_tracker_route (sms_simple_tracker_id, route_id);

CREATE UNIQUE INDEX sms_simple_tracker_number_secondary_key
ON sms_simple_tracker_number (sms_simple_tracker_id, number_id);
