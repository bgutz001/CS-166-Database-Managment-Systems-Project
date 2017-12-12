SELECT *
FROM Airline A
WHERE A.name='Atlantic Airlines'
;

SELECT *
FROM Passenger P
WHERE P.country='HELL'
;

SELECT B.pID, B.flightNum
FROM Booking B
WHERE B.pID = 0 AND NOT EXISTS (
SELECT *
FROM Ratings R
WHERE R.pID = B.pID AND R.flightNum = B.flightNum)
;

SELECT *
FROM Ratings R
WHERE R.pID = 0
;

SELECT flightNum, departure, COUNT(*)
FROM Booking B
GROUP BY flightNum, departure
;

SELECT DISTINCT origin, destination, COUNT(*)
FROM Flight
GROUP BY origin, destination
ORDER BY COUNT(*) DESC
;
