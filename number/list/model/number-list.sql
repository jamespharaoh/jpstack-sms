
CREATE TABLE number_list_update_number (

	number_list_update_id int
		NOT NULL
		REFERENCES number_list_update,

	number_id int
		NOT NULL
		REFERENCES number,

	PRIMARY KEY (
		number_list_update_id,
		number_id
	)

);
