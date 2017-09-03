/* Drop Tables */

DROP TABLE thx_file;
DROP TABLE thx_file_property;

/* Create Tables */
CREATE TABLE thx_file
(
	id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
	original_file_name nvarchar(255),
	stored_file_name varchar(64),
	entry_date timestamp
);

CREATE TABLE thx_file_property
(
	file_id bigint NOT NULL,
	property_key nvarchar(255),
	property_value nvarchar(1024),
	entry_date timestamp
);


ALTER TABLE thx_file_property
	ADD FOREIGN KEY (file_id)
	REFERENCES thx_file (id);
