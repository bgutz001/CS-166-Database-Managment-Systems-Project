-- DROP STATEMENTS
DROP TABLE IF EXISTS Airline CASCADE;
DROP TABLE IF EXISTS Passenger CASCADE;
DROP TABLE IF EXISTS Flight CASCADE;
DROP TABLE IF EXISTS Ratings CASCADE;
DROP TABLE IF EXISTS Booking CASCADE;

DROP DOMAIN IF EXISTS _YEAR CASCADE;
DROP DOMAIN IF EXISTS _HOURS CASCADE;
DROP DOMAIN IF EXISTS _SEATS CASCADE;
DROP DOMAIN IF EXISTS _SCORE CASCADE;

-- CREATE DOMAINS
CREATE DOMAIN _YEAR AS int4 CHECK(VALUE >= 1900);--YEAR ONLY GREATER THAN 1900
CREATE DOMAIN _HOURS AS int4 CHECK(VALUE > 0 AND VALUE < 24);--At most 24 hours duration
CREATE DOMAIN _SEATS AS int4 CHECK(VALUE > 0 AND VALUE < 500);--Plane Seats
CREATE DOMAIN _SCORE AS int4 CHECK(VALUE >= 0 AND VALUE <= 5);--Zero to five stars rating

-- CREATE TABLES
CREATE TABLE Airline(
	airId INTEGER NOT NULL,
	name CHAR(24) NOT NULL,
	founded _YEAR NOT NULL,
	country CHAR(24) NOT NULL,
	hub CHAR(24) NOT NULL,
	PRIMARY KEY(airId)
);

CREATE TABLE Passenger(
	pID INTEGER NOT NULL,
	passNum CHAR(10) NOT NULL,
	fullName CHAR(24) NOT NULL,
	bdate DATE NOT NULL,
	country CHAR(24) NOT NULL,
	PRIMARY KEY(pID),
	UNIQUE(passNum)
);

CREATE TABLE Flight(
	airId INTEGER NOT NULL,
	flightNum CHAR(8) NOT NULL,
	origin CHAR(16) NOT NULL,
	destination CHAR(16) NOT NULL,
	plane CHAR(16) NOT NULL,
	seats _SEATS NOT NULL,
	duration _HOURS NOT NULL,
	PRIMARY KEY(flightNum),
	FOREIGN KEY (airId) REFERENCES Airline(airId)
);

CREATE TABLE Ratings(
	rID INTEGER NOT NULL,
	pID INTEGER NOT NULL,
	flightNum CHAR(8) NOT NULL,
	score _SCORE NOT NULL,
	comment TEXT,
	PRIMARY KEY (rID),
	FOREIGN KEY (pID) REFERENCES Passenger(pID),
	FOREIGN KEY (flightNum) REFERENCES Flight(flightNum)
);

CREATE TABLE Booking(
	bookRef CHAR(10) NOT NULL,
	departure DATE NOT NULL,
	flightNum CHAR(8) NOT NULL,
	pID INTEGER NOT NULL,
	PRIMARY KEY(bookRef),
	FOREIGN KEY (flightNum) REFERENCES Flight(flightNum),
	FOREIGN KEY (pID) REFERENCES Passenger(pID),
	UNIQUE(departure,flightNum,pID)
);

--CREATE USER WITH PASSWORD TO CONNECT TO DATABASE--CHANGE username accordingly
DROP USER IF EXISTS bgutz;
CREATE USER bgutz WITH PASSWORD '123456';
--GRANT USER PRIVELEGES TO ACCESS THE TABLES
GRANT ALL PRIVILEGES ON TABLE Airline TO bgutz;
GRANT ALL PRIVILEGES ON TABLE Passenger TO bgutz;
GRANT ALL PRIVILEGES ON TABLE Flight TO bgutz;
GRANT ALL PRIVILEGES ON TABLE Ratings TO bgutz;
GRANT ALL PRIVILEGES ON TABLE Booking TO bgutz;
------------------------------------------------------------------------------------



--Copy in Data
COPY Airline (
	airId,
	name,
	founded,
	country,
	hub)
FROM 'airline.csv'
WITH DELIMITER ',';
--SELECT * FROM Airline;

COPY Passenger (
	pID,
	passNum,
	fullName,
	bdate,
	country)
FROM 'passenger.csv'
WITH DELIMITER ',';
--SELECT * FROM Passenger;

COPY Flight (
	airId,
	flightNum,
	origin,
	destination,
	plane,
	seats,
	duration)
FROM 'flights.csv'
WITH DELIMITER ',';
--SELECT * FROM Flight;

COPY Ratings (
	rID,
	pID,
	flightNum,
	score,
	comment)
FROM 'ratings.csv'
WITH DELIMITER ',';
--SELECT * FROM Ratings;

COPY Booking (
	bookRef,
	departure,
	flightNum,
	pID)
FROM 'bookings.csv'
WITH DELIMITER ',';
--SELECT * FROM Booking;

--Create pID sequence
DROP SEQUENCE IF EXISTS pIDseq;
CREATE SEQUENCE pIDseq;
SELECT setval('pIDseq', MAX(pID)) FROM Passenger;

CREATE OR REPLACE FUNCTION passenger_insert()
RETURNS "trigger" AS $BODY$
BEGIN
	NEW.pID = nextval('pIDseq');
	RETURN NEW;
END;
$BODY$ 
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER passenger_insert_t BEFORE INSERT
ON Passenger FOR EACH ROW
EXECUTE PROCEDURE passenger_insert();


--Create bookRef sequence
DROP SEQUENCE IF EXISTS bookRefseq;
CREATE SEQUENCE bookRefseq;
SELECT setval('bookRefseq', MAX(bookRef)) FROM Booking;

CREATE OR REPLACE FUNCTION booking_insert()
RETURNS "trigger" AS $BODY$
BEGIN
	NEW.bookRef = nextval('bookRefseq');
	RETURN NEW;
END;
$BODY$ 
LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER booking_insert_t BEFORE INSERT
ON Booking FOR EACH ROW
EXECUTE PROCEDURE booking_insert();


--GRANT USER PRIVELEGES TO ACCESS THE SEQUENCE
GRANT ALL PRIVILEGES ON SEQUENCE pIDseq TO bgutz;
GRANT ALL PRIVILEGES ON SEQUENCE bookRefseq TO bgutz;
