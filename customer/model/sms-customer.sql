
ALTER TABLE sms_customer
ADD UNIQUE (sms_customer_manager_id, number_id);
