CREATE TABLE voter_ballot
(
    election_id bigint      NOT NULL,
    national_id varchar(13) NOT NULL,
    voted_date  datetime2(7) NULL,
    CONSTRAINT PK__voter_ballot PRIMARY KEY (election_id, national_id)
);