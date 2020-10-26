# Server-Client
Basic Server-Client communication program.

Server sends a new joke to a Client on request.


## To compile the programs use:

> javac JokeServer.java

> javac JokeClient.java

> javac JokeClientAdmin.java


## Server waits until new client connects and returns a joke or proverb back, depending on the current mode.

Client connects to the server, gives user name and user id. Then receives a joke or proverb: first cycle in alphabetic order, second and further cycles in random order.

Can be run more than one client at once.

Admin only tells the server to change the mode when it connects (ex: was in joke mode -> change to proverb mode and vice-versa).	

## Any of the following programs can be started independently from each other.
	
> java JokeServer 

> java JokeClient

> java JokeClientAdmin


If Client is run without any parameters, it'll connect to local machine automatically.

To connect to a different computer use IP address of a server machine as a parameter.
