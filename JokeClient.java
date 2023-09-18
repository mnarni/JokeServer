/*--------------------------------------------------------

1. Mounica Narni / 9/21/2022:

2. Java version used (java -version), if not the official version for the class:

    "17.0.2"

3. Precise command-line compilation examples / instructions:

    > javac JokeClient.java


4. Precise examples / instructions to run this program:

> java JokeClient
    This will run the JokeClient, connect the primary server on localhost(default) at port 4545
> java JokeClient 140.192.1.9
    This will run the JokeClient, connect the primary server on the provided IP address, 140.192.1.9 at port 4545
> java JokeClient 192.168.0.72 140.192.1.9
    This will run the JokeClient and connect the primary server on the first argument IP address, 192.168.0.72 at port 4545 and
    connect to secondary server on the second argument IP address, 140.192.1.9 at port 4546


5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

6. Notes:
JokeClient connects sends requests to the server to retrieve jokes and proverbs for a client.
It accepts the username and maintains the state of the joke and proverbs responses from the server to ensure that they're not repeated within a cycle.
By default it connects the primary server to the localhost and send requests at port 4545. When provided a single argument of IP address, it connects the primary server to that IP address at port 4545.
When two arguments are given, the primary server is connected to the first argument at port 4545 and secondary server on the second argument at port 4546.
It reads the user input for username first, once provided on every Enter it sends requests to the server for a joke or proverb along with the current state of the client.
The server returns responses based on its mode and the JokeClient prints the responses to the user and also saves the updated state from the server.
When there is a secondary server, a user input of 's' will switch the servers between primary and secondary.
This process continues until the user inputs 'quit' and then its aborted.

----------------------------------------------------------*/

import java.io.*; // Import Java I/O libraries
import java.net.*; // Import Java networking libraries
import java.util.*;// Import Java utility libraries

public class JokeClient {

    static String mode = "joke";//Variable for joke/proverb mode
    static String jokeIds = "";//Variable for storing server state of jokes
    static String proverbIds = "";//Variable for storing server state of proverbs
    static String primaryJokeIds = "";//Variable for storing server state of jokes for Primary server
    static String primaryProverbIds = "";//Variable for storing server state of proverbs for Primary server
    static String secondaryJokeIds = "";//Variable for storing server state of jokes for Secondary server
    static String secondaryProverbIds = "";//Variable for storing server state of proverbs for Secondary server
    static String primaryServer = "";//Variable for Primary server address
    static String secondaryServer = "";//Variable for Secondary server address
    static int primaryPort = 4545;//Variable for Primary server port
    static int secondaryPort = 4546;//Variable for Secondary server port
    static int port;//Variable for server port
    static boolean hasSecondaryServer = false;//Variable to store if secondary server exists
    static String serverMode = "p";//Variable to server operation mode
    static String serverName = "";//Variable for server address

    public static void main(String args[]) {

        if (args.length < 1) {
            primaryServer = "localhost";
            serverName = primaryServer;// Set default server name to localhost when no args
        } else if (args.length == 1) {
            primaryServer = args[0];// Set primary server to the address in the first arg
        } else if (args.length == 2) {
            primaryServer = args[0];// Set primary server to the address in the first arg
            secondaryServer = args[1];// Set secondary server to the address in the second arg
            hasSecondaryServer = true;//Set secondary server exists to true 
        }

        serverName = primaryServer;// Set default server name to primary
        port = primaryPort;// Set default port to primary port

        //Print the servers available
        System.out.println("Monica Narni's Joke Client\n");
        System.out.println("Server one: " + primaryServer + ", port: " + primaryPort + "\n");
        if (hasSecondaryServer) {
            System.out.println("Server two: " + secondaryServer + ", port: " + secondaryPort + "\n");
        }

        // Create a buffer object for I/O operations to read the input name
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String userName;//Variable for user name
            String command;//Variable for user input

            System.out.println("Using server: " + serverName + ", Port:" + port + "\n");

            System.out.print("Enter an username, (quit) to end: ");
            userName = in.readLine();//Read the user input for name
            //Loop until the user name is provided
            if (userName.equals("")) {
                do {
                    System.out.println("Please enter a name");
                    userName = in.readLine();

                } while (userName.equals(""));
            }
            if (userName.equals("quit")) {
                System.out.println("User request cancelled");// Break the loop and print when user quits
            } else {
                System.out.flush(); // Flush the output stream to avoid overriding

                //Loop for user commands until quit
                do {
                    //Print the current servers and ports
                    System.out.println("\n");
                    System.out.println(serverMode == "p" ? "Primary Server at port 4545" : "<S2> Secondary Server at port 4546");
                    System.out.println((serverMode == "s" ? "<S2>": "") + "Please enter 's' to switch the servers or 'quit' to cancel request or press 'Enter' for response message");

                    //Switch the server on the input of 's'
                    command = in.readLine();
                    if ((command.equals("s"))) {
                        switchServer();//Method call to switch the current server
                    }
                    //request for server on any other input
                    else if ((command.indexOf("quit") < 0)) {
                        getJokeProverb(userName, serverName);// Method call to server to fetch joke/proverb from server
                    }
                } while (command.indexOf("quit") < 0);

                System.out.println("User request cancelled");// Break the loop and print the message on the request to quit
            }
         
        } catch (IOException x) {
            x.printStackTrace(); // Print the exception message when it occurs
        } 
    }

    static void switchServer() { //Method to switch the current server
        if (hasSecondaryServer) { //If secondary server exists switch the server on every call and set the current server address and port accordingly
            if (serverMode == "p")
                serverMode = "s";
            else if (serverMode == "s")
                serverMode = "p";
            switch (serverMode) {
                case "p":
                    port = primaryPort;
                    serverName = primaryServer;
                    jokeIds = primaryJokeIds;
                    proverbIds = primaryProverbIds;
                    break;
                case "s":
                    port = secondaryPort;
                    serverName = secondaryServer;
                    jokeIds = secondaryJokeIds;
                    proverbIds = secondaryProverbIds;
                    break;
                default:
                    port = primaryPort;
                    serverName = primaryServer;
                    break;

            }
            //Print the change of server
            System.out.println("Now communicating with: " + serverName + ", port: " + port + "\n");
        } else
            System.out.println("No secondary server being used");//Print when there is no secondary server

    }

    static void getJokeProverb(String name, String serverName) {// Method to server to fetch joke/proverb from server
        Socket sock; // Socket variable to establish connection with the server
        BufferedReader fromServer; // Buffer variable to read the output from the server
        PrintStream toServer; // PrintStream variable to write the data to the output stream
        String textFromServer;//Variable for server response

        try {

            sock = new Socket(serverName, port);// Open a connection to server with a Socket object passing current server name and port
                                                

            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));// Buffer object to read the output from the server
                                                                                          
            toServer = new PrintStream(sock.getOutputStream());// Output stream to go to the server
            toServer.println(name);// Send the name to server through the stream
            toServer.println(JokeClient.jokeIds);//Send the current server joke state
            toServer.println(JokeClient.proverbIds);//Send the current server proverb state

            toServer.flush();// Flush the output stream to avoid overriding

            mode = fromServer.readLine();//Read the current mode of the server

            textFromServer = fromServer.readLine();// Read the output message from the server
            if (textFromServer != null)
                System.out.println(((serverMode == "p") ? "" : "<S2>") + textFromServer);//Print the server joke/proverb response

                //Based on the server mode, save the updated joke and proverb states
                if (mode.equals("joke")) {
                jokeIds = fromServer.readLine();
                if (jokeIds.contains("JA") && jokeIds.contains("JB") && jokeIds.contains("JC")
                        && jokeIds.contains("JD")) {
                    System.out.println("\nJoke Cycle completed");
                }
                if (serverMode == "p") {
                    primaryJokeIds = jokeIds;
                } else
                    secondaryJokeIds = jokeIds;

            } else {
                proverbIds = fromServer.readLine();
                if (proverbIds.contains("PA") && proverbIds.contains("PB") && proverbIds.contains("PC")
                        && proverbIds.contains("PD")) {
                    System.out.println("\nProverb Cycle completed");
                }
                if (serverMode == "p") {
                    primaryProverbIds = proverbIds;
                } else
                    secondaryProverbIds = proverbIds;
            }

            sock.close();// Close the connection with server
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();// Print the exception message when it occurs
        }
    }
}
