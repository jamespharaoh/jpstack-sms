---------------------------------------- TABLE message_media

CREATE TABLE message_media (
	message_id int NOT NULL,
	i int NOT NULL,
	PRIMARY KEY (message_id, i),
	media_id int NOT NULL
);
