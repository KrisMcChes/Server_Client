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
import java.util.Random; // to get random integer as a client ID

// Client side of the connection
public class JokeClient {
	
	static int uID = getRandomUserID(); // this will be used a user identifier

	public static void main(String args[]) {
		String serverName; // the name of the machine to connect to
		if (args.length < 1) serverName = "localhost"; // if no argument is given, connect to the same machine
		else serverName = args[0]; // if an argument was passed, connect to a server from machine with given IP address

		System.out.println("Kristina McChesney's Joke Client 1.3 is running.");
		System.out.println("Server one: " + serverName + ", port: 4545."); // I have only one server and using only one port to connect my Clients

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); // read user's input
		try {
			String userName; // get name of the current user
			String command;	// get the command of what to do next
			System.out.println("What is your name?");
			System.out.flush(); // sends previous String to the cient's window
			userName = in.readLine(); // save user's response as userName
			System.out.println("Your name is: "+userName+". Your ID: " + uID);
			System.out.flush(); // sends previous String to the cient's window
	
			// run this loop until user asks to exit
			do{
				System.out.println("Please press \"enter\" to start conversation, \"quit\" to stop.");
				System.out.flush();
				command = in.readLine(); // get user's input to decide which step is next
				if (command.indexOf("quit") < 0) // if user presses enter
					startConversation(userName, serverName); // request the server to get a new joke/proverb
			} while (command.indexOf("quit") < 0); // stop the loop when exit command received
			System.out.println("Client exited by user request.");
		} catch (IOException exc) {
			exc.printStackTrace();
		}
	}

	// this method will connect to the Sever with serverName as IP address
	// and passes the name of the current user as userName	
	static void startConversation(String userName, String serverName){
		
		Socket sock; // initialize the socket to talk to Server through
		// initialize streams and buffers to read input or write information to the Server 
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		try {
			sock = new Socket(serverName, 4545); // start the connection here
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); // read information sent from Server through the pipe
			toServer = new PrintStream(sock.getOutputStream()); // write back through the pipe
			toServer.println(userName); // send server your name
			toServer.println(uID); // send a cookie as client's identifier
			toServer.flush(); 
			
			// read up to 3 lines of text from server
			for (int i=1; i<=3; i++){
				textFromServer = fromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}
			sock.close(); // close the connection
		} catch(IOException exc) {
			System.out.println("Socket Error.");
			exc.printStackTrace();
		}
	}

	// I borrowed this code that I wrote for my cryptology class:
	// generate a random integer between 2^20 and 2^31 that will be used as user's ID
	public static int getRandomUserID(){
		
		int start = (int)Math.pow(2,20); // random numbers range
		int end = (int)Math.pow(2,31); // random numbers range
		Random rand = new Random(); // from java random library
		int intRand = rand.nextInt((end - start) + 1) + start; // create a random integer within "start-end" range 
		
		return (intRand);
	}
}
