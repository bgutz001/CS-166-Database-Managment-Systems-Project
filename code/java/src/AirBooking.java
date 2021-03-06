/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.Random;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class AirBooking{
    //reference to physical database connection
    private Connection _connection = null;
    static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
    public AirBooking(String dbname, String dbport, String user, String passwd) throws SQLException {
	System.out.print("Connecting to database...");
	try{
	    // constructs the connection URL
	    String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
	    System.out.println ("Connection URL: " + url + "\n");
			
	    // obtain a physical connection
	    this._connection = DriverManager.getConnection(url, user, passwd);
	    System.out.println("Done");
	}catch(Exception e){
	    System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	    System.out.println("Make sure you started postgres on this machine");
	    System.exit(-1);
	}
    }
	
    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     * 
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     * */
    public void executeUpdate (String sql) throws SQLException { 
	// creates a statement object
	Statement stmt = this._connection.createStatement ();

	// issues the update instruction
	stmt.executeUpdate (sql);

	// close the instruction
	stmt.close ();
    }//end executeUpdate

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     * 
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQueryAndPrintResult (String query) throws SQLException {
	//creates a statement object
	Statement stmt = this._connection.createStatement ();

	//issues the query instruction
	ResultSet rs = stmt.executeQuery (query);

	/*
	 *  obtains the metadata object for the returned result set.  The metadata
	 *  contains row and column info.
	 */
	ResultSetMetaData rsmd = rs.getMetaData ();
	int numCol = rsmd.getColumnCount ();
	int rowCount = 0;
		
	//iterates through the result set and output them to standard out.
	boolean outputHeader = true;
	while (rs.next()){
	    if(outputHeader){
		for(int i = 1; i <= numCol; i++){
		    System.out.print(rsmd.getColumnName(i) + "\t");
		}
		System.out.println();
		outputHeader = false;
	    }
	    for (int i=1; i<=numCol; ++i)
		System.out.print (rs.getString (i) + "\t");
	    System.out.println ();
	    ++rowCount;
	}//end while
	stmt.close ();
	return rowCount;
    }
	
    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the results as
     * a list of records. Each record in turn is a list of attribute values
     * 
     * @param query the input query string
     * @return the query result as a list of records
     * @throws java.sql.SQLException when failed to execute the query
     */
    public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
	//creates a statement object 
	Statement stmt = this._connection.createStatement (); 
		
	//issues the query instruction 
	ResultSet rs = stmt.executeQuery (query); 
	 
	/*
	 * obtains the metadata object for the returned result set.  The metadata 
	 * contains row and column info. 
	 */ 
	ResultSetMetaData rsmd = rs.getMetaData (); 
	int numCol = rsmd.getColumnCount (); 
	int rowCount = 0; 
	 
	//iterates through the result set and saves the data returned by the query. 
	boolean outputHeader = false;
	List<List<String>> result  = new ArrayList<List<String>>(); 
	while (rs.next()){
	    List<String> record = new ArrayList<String>(); 
	    for (int i=1; i<=numCol; ++i) 
		record.add(rs.getString (i)); 
	    result.add(record); 
	}//end while 
	stmt.close (); 
	return result; 
    }//end executeQueryAndReturnResult
	
    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the number of results
     * 
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery (String query) throws SQLException {
	//creates a statement object
	Statement stmt = this._connection.createStatement ();

	//issues the query instruction
	ResultSet rs = stmt.executeQuery (query);

	int rowCount = 0;

	//iterates through the result set and count nuber of results.
	if(rs.next()){
	    rowCount++;
	}//end while
	stmt.close ();
	return rowCount;
    }
	
    /**
     * Method to fetch the last value from sequence. This
     * method issues the query to the DBMS and returns the current 
     * value of sequence used for autogenerated keys
     * 
     * @param sequence name of the DB sequence
     * @return current value of a sequence
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
		
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next()) return rs.getInt(1);
	return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup(){
	try{
	    if (this._connection != null){
		this._connection.close ();
	    }//end if
	}catch (SQLException e){
	    // ignored.
	}//end try
    }//end cleanup

    /**
     * The main execution method
     * 
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    public static void main (String[] args) {
	if (args.length != 3) {
	    System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + AirBooking.class.getName () +
				" <dbname> <port> <user>");
	    return;
	}//end if
		
	AirBooking esql = null;
		
	try{
			
	    try {
		Class.forName("org.postgresql.Driver");
	    }catch(Exception e){

		System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
		e.printStackTrace();
		return;
	    }
			
	    String dbname = args[0];
	    String dbport = args[1];
	    String user = args[2];
			
	    esql = new AirBooking (dbname, dbport, user, "");
			
	    boolean keepon = true;
	    while(keepon){
		System.out.println("MAIN MENU");
		System.out.println("---------");
		System.out.println("1. Add Passenger");
		System.out.println("2. Book Flight");
		System.out.println("3. Review Flight");
		System.out.println("4. Insert or Update Flight");
		System.out.println("5. List Flights From Origin to Destination");
		System.out.println("6. List Most Popular Destinations");
		System.out.println("7. List Highest Rated Destinations");
		System.out.println("8. List Flights to Destination in order of Duration");
		System.out.println("9. Find Number of Available Seats on a given Flight");
		System.out.println("10. < EXIT");
				
		switch (readChoice()){
		case 1: AddPassenger(esql); break;
		case 2: BookFlight(esql); break;
		case 3: TakeCustomerReview(esql); break;
		case 4: InsertOrUpdateRouteForAirline(esql); break;
		case 5: ListAvailableFlightsBetweenOriginAndDestination(esql); break;
		case 6: ListMostPopularDestinations(esql); break;
		case 7: ListHighestRatedRoutes(esql); break;
		case 8: ListFlightFromOriginToDestinationInOrderOfDuration(esql); break;
		case 9: FindNumberOfAvailableSeatsForFlight(esql); break;
		case 10: keepon = false; break;
		}
	    }
	}catch(Exception e){
	    System.err.println (e.getMessage ());
	}finally{
	    try{
		if(esql != null) {
		    System.out.print("Disconnecting from database...");
		    esql.cleanup ();
		    System.out.println("Done\n\nBye !");
		}//end if				
	    }catch(Exception e){
		// ignored.
	    }
	}
    }

    public static int readChoice() {
	int input;
	// returns only if a correct value is given.
	do {
	    System.out.print("Please make your choice: ");
	    try { // read the integer, parse it and break.
		input = Integer.parseInt(in.readLine());
		break;
	    }catch (Exception e) {
		System.out.println("Your input is invalid!");
		continue;
	    }//end try
	}while (true);
	return input;
    }//end readChoice
	

    // TODO in all of the functions. when inputing a string, the query will break if there is a '. Fix this somehow

    public static void AddPassenger(AirBooking esql){//1
	//Add a new passenger to the database
	String sql = null;
	int result = 0;
	String passNum = null;
	String name = null;
	Date date = null;
	String country = null;
	
	// Get the passport number
	do {
	    System.out.print("Enter the passenger's passport number: ");
	    try {
		try {
		passNum = in.readLine();
		// Check that the passport number isn't too long
		if (passNum.length() > 10) {
		    System.out.println("The passport number is too long!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!passNum.matches("[a-zA-Z0-9]+")) {
		    System.out.println("The passport number has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}
		// passNum = URLEncoder.encode(passNum, "UTF-8");
		// The above line will eliminate errors when user inputs '
		// Should use prepared statements instead
		
		} catch (Exception e) {
		    System.out.println("Invalid input!");
		    if (!TryAgain()) return;
		    else continue;
		}

		// Check if passport number already exists
		String sqlpassNum = String.format("SELECT * FROM Passenger WHERE passNum='%s';", passNum);
		result = esql.executeQuery(sqlpassNum);
		if (result != 0) { // passport already exists
		    System.out.println("The passport number is already in use by a passenger!");
		    if(!TryAgain()) return;
		    else continue;
		}
		else { // passport doesn't exist
		    break;
		} 
	    } catch (Exception e) {
		System.out.println("Sorry, something went wrong.");
		System.err.println(e.getMessage());
	    }
	} while (true);

	// Get passenger's full name
	do {
	    System.out.print("Enter the passenger's full name: ");
	    try {
		name = in.readLine();

		// Check that the name isn't too long
		if (name.length() > 24) {
		    System.out.println("The name is too long!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!name.matches("[a-zA-Z]+")) {
		    System.out.println("The name has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}

		break; // name is valid
	    } catch (Exception e) {
		System.out.println("Invalid input!");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	    
	// Get passenger's DOB
	do {
	    System.out.print("Enter the passenger's birth date <YYYY-MM-DD>: ");
	    try {
		date = Date.valueOf(in.readLine());
		break; // Date is valid
	    }
	    catch (Exception e) {
		System.out.println("Invalid format. Use YYYY-MM-DD.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	    
	// Get the passenger's country
	do {
	    System.out.print("Enter the passenger's country: ");
	    try {
		country = in.readLine();

		// Check that the country isn't too long
		if (country.length() > 24) {
		    System.out.println("The country name is too long!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!country.matches("[a-zA-Z]+")) {
		    System.out.println("The name has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}

		break; // country is valid
	    } catch (Exception e) {
		System.out.println("Invalid input!");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);

	// Execute insert
	try {
	    sql = String.format("INSERT INTO Passenger (passNum, fullName, bdate, country) VALUES ('%s', '%s', '%s', '%s');",
				passNum, name, date.toString(), country);
	    esql.executeUpdate(sql);
	} catch (Exception e) {
	    System.out.println("Insertion failed! Please try again.");
	    System.err.println(e.getMessage());
	}
    }
	
    public static void BookFlight(AirBooking esql){//2
	//Book Flight for an existing customer

	int pID = 0;
	String origin = null;
	String destination = null;
	int result = 0;
	Date date = null;
	String bookRef = null;
	String passport = null;
	String flightNum = null;

	// Build bookRef
	do {
	    String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

	    StringBuilder randstring = new StringBuilder();
	    Random rnd = new Random();

	    while (randstring.length() < 10) { // length of the random string.
		int index = (int) (rnd.nextFloat() * CHARS.length());
		randstring.append(CHARS.charAt(index));
	    }
	
	    bookRef = randstring.toString();

	    String sqlref = String.format("SELECT * FROM Booking B WHERE B.bookRef ='%s';", bookRef);
	    try {
		result = esql.executeQuery(sqlref);
		
		if (result == 0) {
		    break;
		}
	    } catch(Exception e) {
		System.out.println("Something wrong");
		System.err.println(e.getMessage());
	    }
	} while(true);


	// Get the passport number, then passeneger id
	do {
	    System.out.print("Enter the passenger's passport number: ");

	    try {
		passport = in.readLine();
	    } catch (Exception e) {
		System.out.println("Invalid input!");
		if (!TryAgain()) return;
		else continue;
	    }

	    try {
		// Check if passport number exists
		String sqlpassNum = String.format("SELECT pID FROM Passenger WHERE passNum='%s';", passport);
		result = esql.executeQuery(sqlpassNum);
		if (result == 0) { // passport doesn't exist
		    System.out.println("A passenger with that passport number can not be found.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else { // passport exists
		    // Get the pid associated with the passport number
		    List<List<String>> pIDS = esql.executeQueryAndReturnResult(sqlpassNum);
		    pID = Integer.parseInt(pIDS.get(0).get(0));
		    break;
		} 
	    } catch (Exception e) {
		System.out.println("Sorry, something went wrong.");
		System.err.println(e.getMessage());
		return;
	    }
	} while (true);

	do {

	    // Get date
	    do {
		System.out.print("Enter the flight's date <YYYY-MM-DD>: ");
		try {
		    date = Date.valueOf(in.readLine());
		    // Date is valid
		    break;
		}
		catch (Exception e) {
		    System.out.println("Please enter a valid date. Use YYYY-MM-DD.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);


	    do {
		// Get the origin
		do {
		    System.out.print("Enter the origin: ");
		    try {
			origin = in.readLine();
			break;
		    } catch (Exception e) {
			System.out.println("Invalid input.");
			if (!TryAgain()) return;
			else continue;
		    }
		} while (true);
		   
		// Get the destination
		do {
		    System.out.print("Enter the destination: ");
		    try {
			destination = in.readLine();
			break;
		    } catch (Exception e) {
			System.out.println("Invalid input.");
			if (!TryAgain()) return;
			else continue;
		    }
		} while (true);
		
		try {
		    // Check if a flight between origin and destination exists
		    String sqlflightNum = String.format("SELECT * FROM Flight F " +
						     "WHERE F.origin = '%s' AND F.destination = '%s';", origin, destination);
		    
		    List<List<String>> flights = esql.executeQueryAndReturnResult(sqlflightNum);

		    // TODO can me multiple flights. Let user pick flightnum

		    if (flights.size() != 0) { // Flight exists
			// Check if seats are available on the date

			if(flights.size() > 0) {
			     System.out.println(String.format("%-17s%-17s%-15s%-15s",
					     "FlightNum", "Origin", "Destination",  "Total Seats"));
			     System.out.println("-------------------------------------------------------");

			     for (int i = 0; i < flights.size(); ++i) {
				 System.out.print(String.format("%-17s", flights.get(i).get(1))); // flightnum
				 System.out.print(String.format("%-17s", flights.get(i).get(2))); // origin
				 System.out.print(String.format("%-15s", flights.get(i).get(3))); // destination
				 System.out.print(String.format("%-15s", flights.get(i).get(5))); // Total Seats
				 System.out.print("\n");
				 System.out.println();
			     }
			     
			     // Get the flightnum
			     do {
				 System.out.print("Enter the flight number for the flight that you want: ");
				 try {
				     try {
					 flightNum  = in.readLine();
					 // Check that the flight number isn't too long
					 if (flightNum.length() > 6) {
					     System.out.println("The flight number is too long!");
					     if (!TryAgain()) return;
					     else continue;
					 }
		
				     } catch (Exception e) {
					 System.out.println("Invalid input!");
					 if (!TryAgain()) return;
					 else continue;
				     }

				     // Check if flightnum exists
				     String sqlflight = String.format("SELECT * FROM Flight F WHERE F.flightNum='%s';", flightNum);
				     result = esql.executeQuery(sqlflight);
				     if (result == 0) {
					 System.out.println("The flight number does not exist, try again!");
					 if(!TryAgain()) return;
					 else continue;
				     }
				     else {
					 break;
				     } 
				 } catch (Exception e) {
				     System.out.println("Sorry, something went wrong.");
				     System.err.println(e.getMessage());
				 }
			     } while (true);
			     
			}

			String sqlbook = String.format("SELECT F.flightNum, F.seats, F.seats-COUNT(*) " +
						       "FROM Flight F, Booking B " +
						       "WHERE F.flightNum=B.flightNum AND F.flightNum='%s' AND B.departure='%s' " +
						       "GROUP BY F.flightNum, F.origin, F.destination, B.departure, F.seats;", flightNum, date.toString());	
			List<List<String>> seat = esql.executeQueryAndReturnResult(sqlbook);
				
			if (seat.size() == 0 ) { // No seats taken
			    // Check if passenger already booked flight
			    String sqlbookcheck = String.format("SELECT * FROM Booking B WHERE B.departure='%s' AND B.flightNum='%s' AND B.pID='%s';"
								,date.toString(),flightNum,pID);
			    result = esql.executeQuery(sqlbookcheck);

			    if (result == 0) { // didn't book
				String sqlbookflight = String.format("INSERT INTO Booking (bookRef, departure, flightNum, pID) VALUES ('%s', '%s', '%s', '%d');",
								     bookRef, date.toString(), flightNum, pID);
				esql.executeUpdate(sqlbookflight);
				System.out.println(String.format("Booked flight '%s'.", flightNum)); // TODO
				return;
			    } 
			    else { // already booked
				System.out.println("Flight is already booked for that passenger at that date");
				if (!TryAgain()) return;
				else break;
			    }
			}
			else if (Integer.parseInt(seat.get(0).get(2)) > 0) { // Seats available
			    // Check if passenger already booked flight
			    String sqlbookcheck = String.format("SELECT * FROM Booking B WHERE B.departure='%s' AND B.flightNum='%s' AND B.pID='%s';"
								,date.toString(),flightNum,pID);
			    result = esql.executeQuery(sqlbookcheck);

			    if (result == 0) {
				String sqlbookflight = String.format("INSERT INTO Booking (bookRef, departure, flightNum, pID) VALUES ('%s', '%s', '%s', '%d');",
								     bookRef, date.toString(), flightNum, pID);
				esql.executeUpdate(sqlbookflight);
				System.out.println(String.format("Booked flight '%s'.", flightNum)); // TODO
				return;
			    } 
			    else {
				System.out.println("Flight is already booked for that passenger at that date");
				if (!TryAgain()) return;
				else break;
			    }
			}
			else { // No seats available
			    System.out.println("No available seats, please enter a differnt departure, origin, or destination");
			    if(!TryAgain()) return;
			    else break;
			}
		    }
		    else { // Flight doesn't exist
		    	System.out.println("Flight does not exist.");
			if (!TryAgain()) return;
			else continue;
		    }
		} catch (Exception e) {
		    System.out.println("Sorry, something went wrong.");
		    System.err.println(e.getMessage());
		    return;
		}
	    } while (true);
	}while(true);
    }
	
    public static void TakeCustomerReview(AirBooking esql){//3
	// Insert customer review into the ratings table
	String sql = null;
	int pID = 0;
	String passport = null;
	String flightNum = null;
	int score = -1;
	String comment = null;
	int result = 0;

	// Get passenger and flight pair
	do {
	    // Get the passport number, then passeneger id
	    do {
		System.out.print("Enter the passenger's passport number: ");

		try {
		    passport = in.readLine();
		    if (passport.length() > 10 || !passport.matches("[a-zA-Z0-9]+")) {
			System.out.println("Invalid passport number.");
			if (!TryAgain()) return;
			else continue;
		    }

		} catch (Exception e) {
		    System.out.println("Invalid input!");
		    if (!TryAgain()) return;
		    else continue;
		}

		try {
		    // Check if passport number exists
		    String sqlpassNum = String.format("SELECT pID FROM Passenger WHERE passNum='%s';", passport);
		    result = esql.executeQuery(sqlpassNum);
		    if (result == 0) { // passport doesn't exist
			System.out.println("A passenger with that passport number can not be found.");
			if (!TryAgain()) return;
			else continue;
		    }
		    else { // passport exists
			// Get the pid associated with the passport number
			List<List<String>> pIDS = esql.executeQueryAndReturnResult(sqlpassNum);
			pID = Integer.parseInt(pIDS.get(0).get(0));
			break;
		    } 
		} catch (Exception e) {
		    System.out.println("Sorry, something went wrong.");
		    System.err.println(e.getMessage());
		    return;
		}
	    } while (true);


	    // Get flight number
	    do {
		System.out.print("Enter the flight number: ");
		try {
		    flightNum = in.readLine();
		    if (flightNum.length() > 8) {
			System.out.println("Invalid flight number.");
			if (!TryAgain()) return;
			else continue;
		    }
		    else if (!flightNum.matches("[a-zA-Z0-9]+")) {
			System.out.println("The flight number has invalid characters!");
			if (!TryAgain()) return;
			else continue;
		    }

		    // Check if flight number exists
		    String sqlflightNum = String.format("SELECT * FROM Flight WHERE flightNum = '%s';", flightNum);
		    try {
		    result = esql.executeQuery(sqlflightNum);
		    if (result == 0) { // flight number doesn't exist
			System.out.println(String.format("Flight '%s' doesn't exist.", flightNum));
			if (!TryAgain()) return;
			else continue;
		    }
		    else { // flight number exists
			break;
		    }
		    } catch (Exception e) {
			System.out.println("Something went wrong!");
			System.out.println(e.getMessage());
			return;
		    }
		} catch (Exception e) {
		    System.out.println("Invalid input.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);

	    // Check if a booking exists.
	    try {
		String sqlbooking = String.format("SELECT * FROM Booking WHERE flightNum='%s' AND pID='%d';", flightNum, pID);
		result = esql.executeQuery(sqlbooking);
		if (result == 0) { // booking doesn't exist
		    System.out.println(String.format("Passenger with passport '%s' never booked flight '%s'.", passport, flightNum));
		    if (!TryAgain()) return;
		    else continue;
		}
	    } catch (Exception e) {
		System.out.println("Sorry, something went wrong.");
		System.err.println(e.getMessage());
		return;
	    }

	    // Check if a rating doesn't already exists
	    try {
		String sqlrating = String.format("SELECT * FROM Ratings WHERE flightnum='%s' AND pID='%d'", flightNum, pID);
		result = esql.executeQuery(sqlrating);
		if (result != 0) { // rating exists
		    System.out.println("Passenger already left a rating for this flight.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } catch (Exception e) {
		System.out.println("Sorry, something went wrong.");
		System.err.println(e.getMessage());
		return;
	    }

	    break;
	} while (true);

	// Bookings exist and no rating exists yet; found pID and flightNum
	// Get the score
	do {
	    System.out.print("Enter the score (from 0 to 5): ");
	    try {
		score = Integer.parseInt(in.readLine());
		    
		// Check if score is in valid range
		if (score > 5 || score < 0) {
		    System.out.print("Invalid range!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else {
		    break;
		}
	    } catch (Exception e) {
		System.out.println("Invalid input, please enter an integer.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
		
	// Get the comment
	do {
	    System.out.print("Enter the comment: ");
	    try {
		comment = in.readLine();
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input!");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	 
	// Execute the insert
	try {
	    sql = String.format("INSERT INTO Ratings (pID, flightNum, score, comment) VALUES ('%d', '%s', '%d', '%s')", pID, flightNum, score, comment);
	    esql.executeUpdate(sql);
	} catch (Exception e) {
	    System.out.println("Insertion failed! Please try again.");
	    System.err.println(e.getMessage());
	    return;
	}
    }
	
    public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
	//Insert a new route for the airline
	    
	int result = 0;
	int airID = 0;
	String flightNum = null;
	String origin = null;
	String destination = null;
	String plane = null;
	int seat = 0;
	int duration = 0;
	
	
	do {
	    // Get the Airline Id
	    do {
		// TODO maybe there is another way to get the Airline ID. User won't know it
		System.out.print("Enter the Airline ID: ");

		try {
		    airID = Integer.parseInt(in.readLine());
		} catch (Exception e) {
		    System.out.println("Invalid input!");
		    if (!TryAgain()) return;
		    else continue;
		}

		try {
		    // Check if airline  exists
		    String sqlairid = String.format("SELECT * FROM Airline A WHERE A.airId='%d';", airID);
		    result = esql.executeQuery(sqlairid);
		    if (result == 0) { // airline already exists
			System.out.println("The airline does not exist, please enter a different airline id.");
			if (!TryAgain()) return;
			else continue;
		    }
		    else { 
			break;
		    } 
		} catch (Exception e) {
		    System.out.println("Sorry, something went wrong.");
		    System.err.println(e.getMessage());
		    return;
		}
	    } while (true);

	    // Get the flight number
	    do {
		System.out.print("Enter the flight number: ");
		try {
		    flightNum = in.readLine();
		    if (flightNum.length() > 8) {
			System.out.println("Invalid flight number.");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while(true);

	 
	    // Get the origin
	    do {
		System.out.print("Enter the origin: ");
		try {
		    origin = in.readLine();
		    if (origin.length() > 16) {
			System.out.println("Invalid origin.");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);
		   
	    // Get the destination
	    do {
		System.out.print("Enter the destination: ");
		try {
		    destination = in.readLine();
		    if (destination.length() > 16) {
			System.out.println("Invalid destination.");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);
	
	    // Get the plane
	    do {
		System.out.print("Enter the plane: ");
		try {
		    plane = in.readLine();
		    if (plane.length() > 16) {
			System.out.println("Invalid plane.");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);

	    // Get the seat number
	    do {
		System.out.print("Enter the seat number: ");
		try {
		    seat = Integer.parseInt(in.readLine());
		    if (seat >= 500 || seat <= 0) {
			System.out.println("Seat count must be between 0 and 500.");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input. Please enter an integer.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);

	    // Get the duration
	    do {
		System.out.print("Enter the duration: ");
		try {
		    duration = Integer.parseInt(in.readLine());
		    if (duration > 24 || duration <= 0) {
			System.out.println("Duration must be between 0 and 25.");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input. Please enter an integer.");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);
	
	    // Ask user if they want to update or insert
	    int choice = 0;
	    System.out.println("1. Insert this flight.");
	    System.out.println("2. Update this flight.");
	    do {
		System.out.print("Choose 1 or 2: ");
		try {
		    choice = Integer.parseInt(in.readLine());
		    if (choice != 1 && choice != 2) {
			System.out.println("Invalid choice!");
			if (!TryAgain()) return;
			else continue;
		    }
		    break;
		} catch (Exception e) {
		    System.out.println("Invalid input!");
		    if (!TryAgain()) return;
		    else continue;
		}
	    } while (true);

	    if (choice == 1) { // Insert
		try {
		    // Check if flight already exists
		    String sqlcheck = String.format("SELECT * " +
						    "FROM Flight F " +
						    "WHERE F.flightNum='%s';",
						    flightNum);
		    result = esql.executeQuery(sqlcheck);

		    if(result == 0) { // Flight doesn't exist
			String sql = String.format("INSERT INTO Flight (airId, flightNum, origin, destination, plane, seats, duration) " +
						   "VALUES ('%d','%s','%s','%s','%s', '%d','%d');",
						   airID, flightNum, origin, destination, plane, seat, duration);
			esql.executeUpdate(sql);
			return;
		    }
		    else {
			System.out.println("Flight already exists, try entering different input.");
			if(!TryAgain()) return;
			else continue;
		    }

		} catch (Exception e) {
		    System.out.println("Something went wrong.");
		    System.err.println(e.getMessage());
		    return;
		}
	    }
	    else if (choice == 2) { // Update
		try {
		    // Check if flight already exists
		    String sqlcheck = String.format("SELECT * " +
						    "FROM Flight F " +
						    "WHERE F.flightNum='%s';",
						    flightNum);
		    result = esql.executeQuery(sqlcheck);

		    if(result != 0) { // Flight exists
			String sql = String.format("UPDATE Flight " +
						   "SET airId='%d', origin='%s', destination='%s', plane='%s', seats='%d', duration='%d' " +
						   "WHERE flightNum='%s';",
						   airID, origin, destination, plane, seat, duration, flightNum);
			esql.executeUpdate(sql);
			return;
		    }
		    else {
			System.out.println("Flight doesn't exist.");
			if(!TryAgain()) return;
			else continue;
		    }

		} catch (Exception e) {
		    System.out.println("Something went wrong.");
		    System.err.println(e.getMessage());
		    return;
		}
	    }
         } while(true);
    }
	
    public static void ListAvailableFlightsBetweenOriginAndDestination(AirBooking esql) throws Exception{//5
	//List all flights between origin and distination (i.e. flightNum,origin,destination,plane,duration) 
	String sql = null;
	int result = 0;
	String origin = null;
	String destination = null;
	
	// Get the origin
	do {
	    System.out.print("Enter the origin: ");
	    try {
		origin = in.readLine();
		if (origin.length() > 16) {
		    System.out.println("The origin name is too long.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!origin.matches("[a-zA-Z]+")) {
		    System.out.println("The origin has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	   
	// Get the destination
	do {
	    System.out.print("Enter the destination: ");
	    try {
		destination = in.readLine();
		if (destination.length() > 16) {
		    System.out.println("The destination name is too long.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!destination.matches("[a-zA-Z]+")) {
		    System.out.println("The destination has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	
	// Execute query
	sql = String.format("SELECT flightNum, origin, destination, plane, duration " +
			    "FROM Flight WHERE origin='%s' AND destination='%s';", origin, destination);
	try {
	    result = esql.executeQueryAndPrintResult(sql);
	} catch (Exception e) {
	    System.out.println("Sorry, something went wrong.");
	    System.err.println(e.getMessage());
	    return;
	}
	
	if (result == 0) {
	    System.out.println(String.format("There are no flights from '%s' to '%s'.", origin, destination));
	}
    }
	
    public static void ListMostPopularDestinations(AirBooking esql){//6
	//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
	String sql = null;
	int k = 10;

	// Get k
	do {
	    System.out.print("Enter the number of destinations to list: ");
	    try {
		k = Integer.parseInt(in.readLine());
		if (k <= 0) {
		    System.out.println("Invalid range!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else {
		    break;
		}
	    } catch (Exception e) {
		System.out.println("Invalid input, please enter an integer.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);

	// List highest rated routes
	try {	
	    System.out.println(String.format("%-25s%-9s",
						 "Destination", "Number of Flights"));
	    System.out.println("-------------------------------------------");

	    // Get list of most popular destinations based on number of flights to each one
	    sql = String.format("SELECT F.destination, COUNT(*) " +
				"FROM Flight F " + 
				"GROUP BY F.destination " +
				"ORDER BY COUNT(*) DESC;");

	    List<List<String>> flights = esql.executeQueryAndReturnResult(sql);
	    
	    for (int i = 0; i < flights.size() && i < k; ++i) {

		// Print result
		System.out.print(String.format("%-25s", flights.get(i).get(0))); // destination
		System.out.print(String.format("%-9s", flights.get(i).get(1))); // number of flight
		System.out.println();
	    }
	} catch (Exception e) {
	    System.out.println("Sorry, something went wrong.");
	    System.err.println(e.getMessage());
	    return;
	}
    }
	
    public static void ListHighestRatedRoutes(AirBooking esql){//7
	//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
	String sql = null;
	int k = 10;

	// Get k
	do {
	    System.out.print("Enter the number of routes to list: ");
	    try {
		k = Integer.parseInt(in.readLine());
		if (k <= 0) {
		    System.out.println("Invalid range!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else {
		    break;
		}
	    } catch (Exception e) {
		System.out.println("Invalid input, please enter an integer.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);

	// List highest rated routes
	try {	
	    System.out.println(String.format("%-25s%-9s%-17s%-17s%-17s%s",
						 "Airline", "Flight", "Origin", "Destination", "Plane", "Rating"));
	    System.out.println("------------------------------------------------------------------------------------------------");

	    // Get averge ratings of every flight in order
	    sql = String.format("SELECT F.flightNum, AVG(R.score) " +
				"FROM Flight F, Ratings R " +
				"WHERE F.flightNum = R.flightNum " + 
				"GROUP BY F.flightNum " +
				"ORDER BY AVG(R.score) DESC;");

	    List<List<String>> flights = esql.executeQueryAndReturnResult(sql);
	    
	    for (int i = 0; i < flights.size() && i < k; ++i) {
		// Get flight info
		sql = String.format("SELECT A.name, F.flightNum, F.origin, F.destination, F.plane " +
				    "FROM Airline A, Flight F " + 
				    "WHERE A.airId = F.airId AND F.flightNum='%s';", flights.get(i).get(0));
		List<List<String>> info = esql.executeQueryAndReturnResult(sql);

		// Print result
		for (List<String> tuple : info) {
		    System.out.print(String.format("%-25s", tuple.get(0))); // Airline name
		    System.out.print(String.format("%-9s", tuple.get(1))); // Flight num
		    System.out.print(String.format("%-17s", tuple.get(2))); // Origin
		    System.out.print(String.format("%-17s", tuple.get(3))); // Destination
		    System.out.print(String.format("%-17s", tuple.get(4))); // Plane
		}
		System.out.print(String.format("%.2f", Double.parseDouble(flights.get(i).get(1)))); // Average rating
		System.out.println();
	    }
	} catch (Exception e) {
	    System.out.println("Sorry, something went wrong.");
	    System.err.println(e.getMessage());
	    return;
	}
    }
	
    public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
	//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
	String sql = null;
	String origin = null;
	String destination = null;
	int k = 10;

	// Get k
	do {
	    System.out.print("Enter the number of flights to list: ");
	    try {
		k = Integer.parseInt(in.readLine());
		if (k <= 0) {
		    System.out.println("Invalid range!");
		    if (!TryAgain()) return;
		    else continue;
		}
		else {
		    break;
		}
	    } catch (Exception e) {
		System.out.println("Invalid input, please enter an integer.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);

	// Get the origin
	do {
	    System.out.print("Enter the origin: ");
	    try {
		origin = in.readLine();
		if (origin.length() > 16) {
		    System.out.println("The name of the origin is too long.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!origin.matches("[a-zA-Z]+")) {
		    System.out.println("The origin has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input!");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	   
	// Get the destination
	do {
	    System.out.print("Enter the destination: ");
	    try {
		destination = in.readLine();
		if (destination.length() > 16) {
		    System.out.println("The name of the destination is too long.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!destination.matches("[a-zA-Z]+")) {
		    System.out.println("The destination has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input!");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
	
	// Execute query
	try {
	    // Get flights in order of duration
	    sql = String.format("SELECT A.name, F.flightNum, F.origin, F.destination, F.plane, F.duration " +
				"FROM Flight F, Airline A " +
				"WHERE F.airId=A.airId AND F.origin='%s' AND F.destination='%s' " + 
				"ORDER BY F.duration ASC;", origin, destination);

	    List<List<String>> flights = esql.executeQueryAndReturnResult(sql);

	    if (flights.size() == 0) {
		System.out.println(String.format("There are no flights from '%s' to '%s'.", origin, destination));
	    }
	    else {
		System.out.println(String.format("%-25s%-9s%-17s%-17s%-17s%s",
						 "Airline", "Flight", "Origin", "Destination", "Plane", "Duration"));
		System.out.println("------------------------------------------------------------------------------------------------");
	    }

	    for (int i = 0; i < flights.size() && i < k; ++i) {
		System.out.print(String.format("%-25s", flights.get(i).get(0))); // Airline name
		System.out.print(String.format("%-9s", flights.get(i).get(1))); // Flight num
		System.out.print(String.format("%-17s", flights.get(i).get(2))); // Origin
		System.out.print(String.format("%-17s", flights.get(i).get(3))); // Destination
		System.out.print(String.format("%-17s", flights.get(i).get(4))); // Plane
		System.out.print(String.format("%d", Integer.parseInt(flights.get(i).get(5)))); // Duration
		System.out.println();
	    }
	} catch (Exception e) {
	    System.out.println("Sorry, something went wrong.");
	    System.err.println(e.getMessage());
	    return;
	}
    }
	
    public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
	//Find the number of seats available for a given flight on a given date
	String sql = null;
	String flightNum = null;
	Date date = null;
	int result = 0;
	
	// Get flight number
	do {
	    System.out.print("Enter the flight number: ");
	    try {
		flightNum = in.readLine();
		if (flightNum.length() > 8) {
		    System.out.println("The flight number is too long.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else if (!flightNum.matches("[a-zA-Z0-9]+")) {
		    System.out.println("The flight number has invalid characters!");
		    if (!TryAgain()) return;
		    else continue;
		}

		// Check if flight number exists
		String sqlflightNum = String.format("SELECT * FROM Flight WHERE flightNum = '%s';", flightNum);
		try {
		result = esql.executeQuery(sqlflightNum);
		if (result == 0) { // flight number doesn't exist
		    System.out.println("Flight doesn't exist, please enter a valid flight number.");
		    if (!TryAgain()) return;
		    else continue;
		}
		else { // flight number exists
		    break;
		}
		} catch (Exception e) {
		    System.out.println("Something went wrong.");
		    System.out.println(e.getMessage());
		    return;
		}
	    } catch (Exception e) {
		System.out.println("Invlaid input!");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);

	// Get date
	do {
	    System.out.print("Enter the flight's date <YYYY-MM-DD>: ");
	    try {
		date = Date.valueOf(in.readLine());
		// Date is valid
		break;
	    }
	    catch (Exception e) {
		System.out.println("Please enter a valid date. Use YYYY-MM-DD.");
		if (!TryAgain()) return;
		else continue;
	    }
	} while (true);
		
	// Execute Query
	try {
	    sql = String.format("SELECT F.flightNum, F.origin, F.destination, B.departure, COUNT(*), F.seats, F.seats-COUNT(*) " +
				"FROM Flight F, Booking B " +
				"WHERE F.flightNum=B.flightNum AND F.flightNum='%s' AND B.departure='%s' " +
				"GROUP BY F.flightNum, F.origin, F.destination, B.departure, F.seats;", flightNum, date.toString());

	    List<List<String>> seats = esql.executeQueryAndReturnResult(sql);

	    System.out.println(String.format("%-9s%-17s%-17s%-15s%-15s%-15s%-15s",
					     "Flight", "Origin", "Destination", "Departure",
					     "Booked Seats", "Total Seats", "Free Seats"));
	    System.out.println("------------------------------------------------------------------------------------------------");

	    if (seats.size() == 0) { // Every seat is free that day
		sql = String.format("SELECT F.flightNum, F.origin, F.destination, F.seats " +
				    "FROM Flight F " + 
				    "WHERE F.flightNum='%s';", flightNum);
		seats = esql.executeQueryAndReturnResult(sql);
		System.out.print(String.format("%-9s", seats.get(0).get(0))); // Flight num
		System.out.print(String.format("%-17s", seats.get(0).get(1))); // Origin
		System.out.print(String.format("%-17s", seats.get(0).get(2))); // Destination
		System.out.print(String.format("%-15s", date.toString())); // Departure
		System.out.print(String.format("%-15s", "0")); // Booked Seats
		System.out.print(String.format("%-15s", seats.get(0).get(3))); // Total Seats
		System.out.print(String.format("%-15s", seats.get(0).get(3))); // Free Seats
		System.out.println();
		return;
	    }

	    for (int i = 0; i < seats.size(); ++i) {
		System.out.print(String.format("%-9s", seats.get(i).get(0))); // Flight num
		System.out.print(String.format("%-17s", seats.get(i).get(1))); // Origin
		System.out.print(String.format("%-17s", seats.get(i).get(2))); // Destination
		System.out.print(String.format("%-15s", seats.get(i).get(3))); // Departure
		System.out.print(String.format("%-15s", seats.get(i).get(4))); // Booked Seats
		System.out.print(String.format("%-15s", seats.get(i).get(5))); // Total Seats
		System.out.print(String.format("%-15s", seats.get(i).get(6))); // Free Seats
		System.out.println();
	    }
	} catch (Exception e) {
	    System.out.println("Sorry, something went wrong.");
	    System.err.println(e.getMessage());
	    return;
	}
    }

    public static boolean TryAgain() {
	do {
	    try {
		System.out.print("Try again (y/n)? ");
		String answer = in.readLine();
		if (answer.charAt(0) == 'y' || answer.charAt(0) == 'Y') {
		    return true;
		}
		else if (answer.charAt(0) == 'n' || answer.charAt(0) == 'N') {
		    return false;
		}
		else {
		    System.out.println("Please enter y or n.");
		}
	    } catch(Exception e) {
		System.out.println("Invalid input.");
	    }
	} while(true);
    }
    
}
