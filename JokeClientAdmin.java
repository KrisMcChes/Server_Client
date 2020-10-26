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

// Admin side of the connection
public class JokeClientAdmin {

	public static void main (String args[]) {
		String serverName; // ip address of the machine to connect
		if (args.length < 1) serverName = "localhost"; // if no argument is given, connect to the same machine
		else serverName = args[0]; // in case the machine name was provided

		System.out.println("Kristina McChesney's Admin Client, 1.3.\n");
		System.out.println("Using server: " + serverName + "Port: 5050.\n");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in)); //creating a buffer to get input form the user
		try {
			String command; // get the command of what to do next
			// get an input from the user, until "quit" is received
			do {
				System.out.println("Press <enter> to switch the mode, (quit) to end: ");
				System.out.flush();
				command = in.readLine(); // get user's input to decide which step is next
				if (command.indexOf("quit") < 0) // if user presses enter
					changeMode(command, serverName); // create a new connection with the server
			} while (command.indexOf("quit") < 0); // loop until user asks to quit
			System.out.println("Canceled by user request.");
		}
		catch (IOException x) {x.printStackTrace();} // exception handling
	}

	// establishing the connection with the server
	static void changeMode (String command, String serverName){

		// initializing socket to establish connection with the server and
		// other variables to read and write from/to server
		Socket sock;				
		BufferedReader fromServer;	
		PrintStream toServer;		
		String textFromServer;		 

		try{
			sock = new Socket(serverName, 5050); 	//open a socket passing 2 arguments: server IP address and port number
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));	// get input from the server
			toServer = new PrintStream(sock.getOutputStream());								// send output to the server
			toServer.println(command); // send an empty string to the server								
			toServer.flush();

			// reading and printing up to 3 lines from server
			for (int i=1; i<=3; i++){
				textFromServer = fromServer.readLine();
				if (textFromServer != null)
					System.out.println(textFromServer);
			}
			sock.close(); // close the connection
		} catch (IOException x) { // exception handling
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
}
