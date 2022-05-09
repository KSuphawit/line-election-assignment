CREATE TABLE election
(
    id                bigint IDENTITY(1,1) NOT NULL,
    start_voting_date datetime2(7) NOT NULL,
    end_voting_date   datetime2(7) NOT NULL,
    status            varchar(50) NULL,
    created_date      datetime2(7) NULL,
    updated_date      datetime2(7) NULL,
    CONSTRAINT PK__election PRIMARY KEY (id)
);


DECLARE
@start_of_day datetime2;
SET
@start_of_day = CAST (CURRENT_TIMESTAMP as DATE);

DECLARE
@end_of_day datetime2
SET @end_of_day =  DATEADD(second,-1,datediff(dd,0,@start_of_day)+2)

INSERT INTO election(start_voting_date, end_voting_date, status, created_date, updated_date)
VALUES
(@start_of_day, @end_of_day, 'RUN_FOR_ELECTION', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

