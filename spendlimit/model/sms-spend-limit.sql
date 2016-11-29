CREATE TABLE sms_spend_limiter_number_spend_message (
	sms_spend_limiter_number_id int REFERENCES sms_spend_limiter_number,
	message_id int REFERENCES message,
	PRIMARY KEY (sms_spend_limiter_number_id, message_id)
);

CREATE TABLE sms_spend_limiter_number_advice_message (
	sms_spend_limiter_number_id int REFERENCES sms_spend_limiter_number,
	message_id int REFERENCES message,
	PRIMARY KEY (sms_spend_limiter_number_id, message_id)
);

CREATE TABLE sms_spend_limiter_number_day_spend_message (
	sms_spend_limiter_number_day_id int REFERENCES sms_spend_limiter_number,
	message_id int REFERENCES message,
	PRIMARY KEY (sms_spend_limiter_number_day_id, message_id)
);

CREATE TABLE sms_spend_limiter_number_day_advice_message (
	sms_spend_limiter_number_day_id int REFERENCES sms_spend_limiter_number,
	message_id int REFERENCES message,
	PRIMARY KEY (sms_spend_limiter_number_day_id, message_id)
);
