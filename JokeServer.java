/*--------------------------------------------------------

1. Kristina McChesney 04/23/2019

2. I used suggested Java version build 1.8.0_201-b09

3. To compile the programs use:

> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java

4. 	Server waits until new client connects and returns a joke or proverb back, depending on the current mode.
	Client connects to the server, gives user name and user id. Then receives a joke or proverb: first cycle in alphabetic order, second and further cycles in random order.
	Can be run more than one client at once.
	Admin only tells the server to change the mode when it connects (ex: was in joke mode -> change to proverb mode and vice-versa).	

	Any of the following programs can be started independently from each other.
	
> java JokeServer
> java JokeClient
> java JokeClientAdmin

If Client is run without any parameters, it'll connect to local machine automatically.
To connect to a different computer use IP address of a server machine as a parameter.

5. Files were submitted:

	- JokeServer.java
 	- JokeClient.java
 	- JokeClientAdmin.java
 	- JokeLog.txt
 	- checklist-joke.html
 	
 6. There is a small possibility that randomization will give the same order ABCD because the list has only 4 elements, start over.

----------------------------------------------------------*/

import java.io.*; // libraries to produce input/output
import java.net.*; // libraries for network connections
import java.util.ArrayList; // to store current client's joke/proverb state
import java.util.HashMap; // to store client's states and names
import java.util.Random; // to get random joke or proverb


// after client is connected, we start a new thread here
class JokeHelper extends Thread {

	// initialize data to save on the server side
	Socket socket; // create a "pipe" to talk to a client
	public static boolean mode = true; // initially mode of the server is "Joke"

	static int uID; // user ID sent from client
	static HashMap<Integer, String> clientNames = new HashMap<>(); // stores user's id and name

	static HashMap<Integer, Integer> clientContersJ = new HashMap<>(); // for each client stores the location of last seen joke from the joke list
	static HashMap<Integer, ArrayList<String>> jokeStates = new HashMap<>(); // stores joke list after randomizing for each client

	static HashMap<Integer, Integer> clientContersP = new HashMap<>(); // for each client stores the location of last seen proverb from the proverb list
	static HashMap<Integer, ArrayList<String>> proverbStates = new HashMap<>(); // stores proverb list after randomizing for each client


	// jokes templates stored here
	static String[] jokes = {
		"JA <name>: Why do firefighters wear red suspenders? To hold their pants up.",
		"JB <name>: Why couldn't Mozart find his teacher? Because he was Haydn.",
		"JC <name>: Why shouldn't you date a tennis player? Because love means nothing to them.",
		"JD <name>: Two men were arrested trying to steal a calendar. They each got six months."
	};

	// proverbs templates stored here
	static String[] proverbs = {
		"PA <name>: Think twice, cut once.",
		"PB <name>: There's free cheese in every mousetrap.",
		"PC <name>: Two wrongs don't make a right.",
		"PD <name>: The pen is mightier than the sword."
	};

	// our constructor to start a communication with the client
	JokeHelper(Socket s) {
		socket = s;
	}

	// start communication with the client from this function
	public void run() {
		// this will be used to get input/send output through the pipe 
		PrintStream out = null;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // read from the socket
			out = new PrintStream(socket.getOutputStream()); // write to the socket
			try {
				String userName = in.readLine(); // read name of the user who's currently in session 
				String id = in.readLine(); // read user's id passed from the client
				JokeHelper.uID = Integer.parseInt(id); 
				if (!clientNames.containsKey(uID)) // if the user never connected before
					clientNames.put(uID, userName); // add key-value pair (uID and user's name) to my dictionary
				System.out.println("User's name connected is: " + clientNames.get(uID) + ". uID: "+ uID);	

				if (mode == true) // if current mode is "Joke"
					sendJoke(uID, out);	// send jokes from here
				else sendProverb(uID, out); // if mode wa changed to "Proverb"
			} catch (IOException exc1) { // in case error occurs
				System.out.println("Something went wrong while communicating with the Client");
				exc1.printStackTrace();
			}
			socket.close();	// after one joke or proverb was sent, close the connection
		} catch (IOException exc2) {
			System.out.println(exc2);
		}		
	}

	// sending jokes from here; saving the state of the conversation individually with each client
	static void sendJoke(int uID, PrintStream out){

		int counter = 0; // counter helps to remember where we stopped in the list of jokes for a particular client
		if (clientContersJ.containsKey(uID)) { // if client previously connected
			counter = clientContersJ.get(uID); // get the information about the last position at the joke list from this map
		} 

		// for the first time connection, send jokes in the right order: JA,JB,JC,JD
		if (counter < 4){
			// takes a joke from the joke list template at the last saved position for this client and adds name of the user to the sentence;
			String joke = jokes[counter].replaceAll("<name>", clientNames.get(uID));  
			out.println(joke); // send to client
			counter++; // change the position of the cursor
			clientContersJ.put(uID, counter); // save current of the client to the server

			if (clientContersJ.get(uID) == 4) { // when all 4 jokes were sent, complete the cycle
				out.println("JOKE CYCLE COMPLETED"); // announce on client console
				System.out.println("JOKE CYCLE COMPLETED for user: "+clientNames.get(uID)); // announce on server side
			}
		}

		// second and all further cycles will send jokes randomly
		else {
			ArrayList<String> clientJokes = new ArrayList<>(); // stores joke list after randomizing for each client individually
			Random rand = new Random(); // random method was borrowed from here: https://www.baeldung.com/java-random-list-element

			if (!jokeStates.containsKey(uID) || jokeStates.get(uID).size() <= 0) { // if client doesn't have its state yet || or the cycle's been completed
				// add 0-3 elements representing 4 jokes and their position in the jokes template list
				clientJokes.add("0");
				clientJokes.add("1");
				clientJokes.add("2");
				clientJokes.add("3"); 

				String randomElement = clientJokes.get(rand.nextInt(clientJokes.size())); //get a random element 
				// takes a joke from the joke list template at random position for this client and adds name of the user to the sentence;
				String joke = jokes[Integer.parseInt(randomElement)].replaceAll("<name>", clientNames.get(uID)); 
				out.println(joke); // send joke to client
				clientJokes.remove(randomElement); // remove this joke from the list
				jokeStates.put(uID, clientJokes); // save current state
			}

			else {
				clientJokes = jokeStates.get(uID); // if client has saved state, upload it here
				String randomElement = clientJokes.get(rand.nextInt(clientJokes.size())); // get next random element
				// takes a joke from the joke list template at random position for this client and adds name of the user to the sentence;
				String joke = jokes[Integer.parseInt(randomElement)].replaceAll("<name>", clientNames.get(uID));
				out.println(joke); // print joke to client
				clientJokes.remove(randomElement); // remove the element that was sent
				jokeStates.put(uID, clientJokes); // save a new state
				
				if (jokeStates.get(uID).size() <= 0) { // when all jokes were sent and removed from the list
					out.println("JOKE CYCLE COMPLETED"); // announce on client console
					System.out.println("JOKE CYCLE COMPLETED for user: "+clientNames.get(uID)); // announce on server side
				}		
			}
		}
	}

	// sending proverbs from here; saving the state of the conversation individually with each client
	static void sendProverb(int uID, PrintStream out){

		int counter = 0; // counter helps to remember where we stopped in the list of proverbs for a particular client
		if (clientContersP.containsKey(uID)) { // if client previously connected
			counter = clientContersP.get(uID); // get the information about the last position at the proverb list from this map
		} 

		// for the first time connection, send proverbs in the right order: PA,PB,PC,PD
		if (counter < 4){
			// takes a proverb from the proverb list template at the last saved position for this client and adds name of the user to the sentence;
			String proverb = proverbs[counter].replaceAll("<name>", clientNames.get(uID)); // adds name of the user to the proverb sentence
			out.println(proverb); // send to client
			counter++; // change the position of the cursor
			clientContersP.put(uID, counter); // save current of the client to the server

			if (clientContersP.get(uID) == 4) { // when all 4 proverbs were sent, complete the cycle
				out.println("PROVERB CYCLE COMPLETED"); // announce on client console
				System.out.println("PROVERB CYCLE COMPLETED for user: "+clientNames.get(uID)); // announce on server side
			}
		}

		// second and all further cycles will send proverbs randomly
		else {
			ArrayList<String> clientProverbs = new ArrayList<>(); // stores proverb list after randomizing for each client individually
			Random rand = new Random(); // random method from here: https://www.baeldung.com/java-random-list-element

			if (!proverbStates.containsKey(uID) || proverbStates.get(uID).size() <= 0) { // if client doesn't have its state yet || or the cycle's been completed
				// add 0-3 elements representing 4 proverbs and their position in the proverbs template list			
				clientProverbs.add("0");
				clientProverbs.add("1");
				clientProverbs.add("2");
				clientProverbs.add("3"); 

				String randomElement = clientProverbs.get(rand.nextInt(clientProverbs.size())); //get a random element 
				// takes a proverb from the joke list template at random position for this client and adds name of the user to the sentence;
				String proverb = proverbs[Integer.parseInt(randomElement)].replaceAll("<name>", clientNames.get(uID));
				out.println(proverb); // send proverb to client
				clientProverbs.remove(randomElement); // remove this proverb from the list
				proverbStates.put(uID, clientProverbs); // save current state
			}

			else { // if client has saved state
				clientProverbs = proverbStates.get(uID); // upload client's list here
				String randomElement = clientProverbs.get(rand.nextInt(clientProverbs.size())); // get next random element
				// takes a proverb from the joke list template at random position for this client and adds name of the user to the sentence;
				String proverb = proverbs[Integer.parseInt(randomElement)].replaceAll("<name>", clientNames.get(uID)); 
				out.println(proverb); // print proverb to client
				clientProverbs.remove(randomElement); // remove the element that was sent
				proverbStates.put(uID, clientProverbs); // save a new state

				if (proverbStates.get(uID).size() <= 0) { // when all jokes were sent and removed from the list
					out.println("PROVERB CYCLE COMPLETED"); // announce on client console
					System.out.println("PROVERB CYCLE COMPLETED for user: "+clientNames.get(uID)); // announce on server side
				}
			}
		}
	}
}


// Server starts running from here
public class JokeServer {

	public static boolean controlSwitch = true; // will be constantly looping, open to a new client

	public static void main (String arg[]) throws IOException{
		int q_len = 6; // how many requests to put to the line
		int port = 4545; // port for client connections
		Socket sock; // this socket will be used to create an actual connection with a client 

		// start new thread to wait for admin connections
		AdminLooper AL = new AdminLooper(); 
		Thread t = new Thread(AL);
		t.start();		

		ServerSocket servsock = new ServerSocket(port, q_len); // this socket will be always listening to a new client

		System.out.println("Kristina McChesney's Joke Server 1.3 is starting at port number " + port + ".\n");

		// this loop always runs, always waits for a new client to connect
		while (controlSwitch){
			sock = servsock.accept(); // start a new connection with new client
			new JokeHelper(sock).start(); // begin execution of actual program, after the connection is established
		}
	}
}

// when Admin connects, the mode is changed here
class AdminWorker extends Thread {

	Socket sock; // to create a connection with Admin
	AdminWorker (Socket s) {sock = s;} // Admin constructor to start a communication with the server

	// execution start from here
	public void run() {
		PrintStream out = null; // initialize stream to write to Admin
		BufferedReader in = null; // initialize stream to read from Admin

		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream())); // getting input from Admin
			out = new PrintStream(sock.getOutputStream()); //sending output to Admin
			try {
				String command = in.readLine(); // when Admin connects, it send an empty string
				System.out.println("Admin connected");

				if (JokeHelper.mode == true){ // check the current state of the Server
					JokeHelper.mode = false; // if it's in "Joke" mode, change to "Proverb"
					System.out.println("Mode changed to proverbs"); // announce on Server window
					out.println("Mode changed to proverbs"); // announce to Admin
				}
				else { // if the current mode is "Proverb"
					JokeHelper.mode = true; // change to "Joke"
					System.out.println("Mode changed to jokes"); // announce on Server window
					out.println("Mode changed to jokes"); // announce to Admin
				}
			} catch (IOException x){ // exception handling
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); // close the connection after mode changed
		} catch (IOException ioe) {System.out.println(ioe);} // exception handling
	}
}

// new thread to wait for a connection from Admin
class AdminLooper implements Runnable {
	
	public static boolean adminControlSwitch = true; // will be constantly looping, waiting for an admin connection

	// start execution from here
	public void run(){ 
		int q_len = 6; // how many requests to put to the line
		int port = 5050; // port for admin connection
		Socket sock; // this socket will be used to create a connection with Admin

		try{
			ServerSocket servsock = new ServerSocket(port, q_len); // this socket will be always listening to a new admin to connect
			while (adminControlSwitch) { // always running
				sock = servsock.accept(); // create a connecrtion with admin
				new AdminWorker (sock).start(); // start an Admin thread
			}
		}catch (IOException ioe) {System.out.println(ioe);}
	}
}
