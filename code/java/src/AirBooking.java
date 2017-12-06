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
		passNum = in.readLine();

		// Check if passport number already exists
		String sqlpassNum = String.format("SELECT * FROM Passenger WHERE passNum='%s';", passNum);
		result = esql.executeQuery(sqlpassNum);
		if (result != 0) { // passport already exists
		    System.out.println("The passport number is already in use by a passenger!");
		}
		else { // passport doesn't exist
		    break;
		} 
	    } catch (Exception e) {
		System.out.println("Shouldn't see this"); // TODO error message
	    }
	} while (true);

	// Get passenger's full name
	do {
	    System.out.print("Enter the passenger's full name: ");
	    try {
		name = in.readLine();
		break; // name is valid
	    } catch (Exception e) {
		System.out.println("Shouldn't see this"); // TODO error message
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
		System.out.println("Invalid Format! Use YYYY-MM-DD.");
	    }
	} while (true);
	    
	// Get the passenger's country
	do {
	    System.out.print("Enter the passenger's country: ");
	    try {
		country = in.readLine();
		break; // country is valid
	    } catch (Exception e) {
		System.out.println("Shouldn't see this"); // TODO error message
	    }
	} while (true);

	// Execute insert
	try {
	    sql = String.format("INSERT INTO Passenger (passNum, fullName, bdate, country) VALUES ('%s', '%s', '%s', '%s');",
				passNum, name, date.toString(), country);
	    esql.executeUpdate(sql);
	} catch (Exception e) {
	    System.out.println("Insertion failed! Please try again.");
	}
    }
	
    public static void BookFlight(AirBooking esql){//2
	//Book Flight for an existing customer
    }
	
    public static void TakeCustomerReview(AirBooking esql){//3
	// Insert customer review into the ratings table
	String sql = null;
	int pID = 0;
	String flightNum;
	int score = -1;
	String comment;
	int result = 0;

	// TODO change pid to passport number
	
	// Get passenger and flight pair
	do {
	    // Get passenger id
	    do {
		System.out.print("Enter the passenger's ID: ");
		try {
		    pID = Integer.parseInt(in.readLine());

		    // Check if passenger exists
		    String sqlpID = String.format("SELECT * FROM Passenger WHERE pID = '%d';", pID);
		    result = esql.executeQuery(sqlpID);
		    if (result == 0) { // passenger doesn't exist
			System.out.println("Passenger doesn't exist, please enter a valid pID.");
		    }
		    else { // passenger exists
			break;
		    }
		} catch (Exception e) { // IOexception or SQLexception
		    System.out.println("Invalid input, please enter an integer.");
		}
	    } while (true);

	    // Get flight number
	    do {
		System.out.print("Enter the flight number: ");
		try {
		    flightNum = in.readLine();
		    
		    // Check if flight number exists
		    String sqlflightNum = String.format("SELECT * FROM Flight WHERE flightNum = '%s';", flightNum);
		    result = esql.executeQuery(sqlflightNum);
		    if (result == 0) { // flight number doesn't exist
			System.out.println("Flight doesn't exist, please enter a valid flight number.");
		    }
		    else { // flight number exists
			break;
		    }
		} catch (Exception e) {
		    System.out.println("Invalid input."); // TODO error message
		}
	    } while (true);

	    // Check if a booking exists.
	    try {
		String sqlbooking = String.format("SELECT * FROM Booking WHERE flightNum='%s' AND pID='%d';", flightNum, pID);
		result = esql.executeQuery(sqlbooking);
		if (result == 0) { // booking doesn't exist
		    System.out.println("Booking doesn't exist, please enter a passenger that booked the flight.");
		    continue;
		}
	    } catch (Exception e) {
		System.out.println("Shouldn't see this"); // TODO error message
	    }

	    // Check if a rating doesn't already exists
	    try {
		String sqlrating = String.format("SELECT * FROM Ratings WHERE flightnum='%s' AND pID='%d'", flightNum, pID);
		result = esql.executeQuery(sqlrating);
		if (result != 0) { // rating exists
		    System.out.println("Passenger already left a rating for this flight, please enter a flight and passenger that hasn't already left a rating");
		    continue;
		}
	    } catch (Exception e) {
		System.out.println("Shouldn't see this"); // TODO error message
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
		}
		else {
		    break;
		}
	    } catch (Exception e) {
		System.out.println("Invalid input, please enter an integer.");
	    }
	} while (true);
		
	// Get the comment
	do {
	    System.out.print("Enter the comment: ");
	    try {
		comment = in.readLine();
		break;
	    } catch (Exception e) {
		System.out.println("Shouldn't see this"); // TODO error message
	    }
	} while (true);
	 
	// Execute the insert
	try {
	    sql = String.format("INSERT INTO Ratings (pID, flightNum, score, comment) VALUES ('%d', '%s', '%d', '%s')", pID, flightNum, score, comment);
	    esql.executeUpdate(sql);
	} catch (Exception e) {
	    System.out.println("Insertion failed! Please try again.");
	    System.err.println(e.getMessage()); // TODO remove this; test this never happens
	}
    }
	
    public static void InsertOrUpdateRouteForAirline(AirBooking esql){//4
	//Insert a new route for the airline
    }
	
    // TODO check that origin/destination exist?
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
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input."); // TODO
	    }
	} while (true);
	   
	// Get the destination
	do {
	    System.out.print("Enter the destination: ");
	    try {
		destination = in.readLine();
		break;
	    } catch (Exception e) {
		System.out.println("Invalid input."); // TODO
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
	}
	
	if (result == 0) {
	    System.out.println(String.format("There are no flights from '%s' to '%s'.", origin, destination));
	}
    }
	
    public static void ListMostPopularDestinations(AirBooking esql){//6
	//Print the k most popular destinations based on the number of flights offered to them (i.e. destination, choices)
    }
	
    public static void ListHighestRatedRoutes(AirBooking esql){//7
	//List the k highest rated Routes (i.e. Airline Name, flightNum, Avg_Score)
	String sql = null;
	int k = 10;

	// Get k
	do {
	    try {
		break; // TODO
	    } catch (Exception e) {
		System.out.println("Invalid input, please enter an integer.");
	    }
	} while (true);

	// List highest rated routes
	try {	
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
	}
    }
	
    public static void ListFlightFromOriginToDestinationInOrderOfDuration(AirBooking esql){//8
	//List flight to destination in order of duration (i.e. Airline name, flightNum, origin, destination, duration, plane)
    }
	
    public static void FindNumberOfAvailableSeatsForFlight(AirBooking esql){//9
	//
		
    }
    
}
