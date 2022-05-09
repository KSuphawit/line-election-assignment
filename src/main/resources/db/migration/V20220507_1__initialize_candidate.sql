CREATE TABLE candidate
(
    id         bigint IDENTITY(1,1) NOT NULL,
    name       varchar(255) NOT NULL,
    dob        datetime2(7) NULL,
    bio_link   varchar(255) NOT NULL,
    image_link varchar(255) NOT NULL,
    policy     varchar(MAX
) NOT NULL,
    voted_count bigint NULL,
    election_id bigint NULL ,
    created_date datetime2(7) NULL,
    updated_date datetime2(7) NULL,
    CONSTRAINT PK__candidate PRIMARY KEY (id)
);

