CREATE INDEX inbox_pending_next
ON inbox (next_attempt, message_id)
WHERE state = 'pending';

CREATE INDEX inbox_pending_created
ON inbox (created_time, message_id)
WHERE state = 'pending';
