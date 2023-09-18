/*--------------------------------------------------------

1. Mounica Narni / 9/21/2022:

2. Java version used (java -version), if not the official version for the class:

    "17.0.2"

3. Precise command-line compilation examples / instructions:

    > javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:

> java JokeClientAdmin
    This will run the JokeClientAdmin, connect the primary server on localhost(default) at port 5050
> java JokeClientAdmin 140.192.1.9
    This will run the JokeClientAdmin, connect the primary server on the provided IP address, 140.192.1.9 at port 5050
> java JokeClientAdmin 192.168.0.72 140.192.1.9
    This will run the JokeClientAdmin and connect the primary server on the first argument IP address, 192.168.0.72 at port 5050 and
    connect to secondary server on the second argument IP address, 140.192.1.9 at port 5051


5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

6. Notes:
JokeClientAdmin toggles the modes between joke and proverbs and sets the server mode.
By default it connects the primary server to the localhost and send requests at port 5050. When provided a single argument of IP address, it connects the primary server to that IP address at port 5050.
When two arguments are given, the primary server is connected to the first argument at port 5050 and secondary server on the second argument at port 5051.
An input of Enter switches the mode of the server its connected to, and an input of 's' swiches the servers between primary and secondary when a secondary server exists.
By entering 'quit' the program aborts.

----------------------------------------------------------*/

import java.io.*; //Import Java I/O libraries
import java.net.*; //Import Java networking libraries

public class JokeClientAdmin {
    static String primaryMode = "joke";// Mode of the primary server
    static String secondaryMode = "joke";// Mode of the secondary server
    static String primaryServer = "";// Primary server address
    static String secondaryServer = "";// Primary server address
    static int primaryPort = 5050;// Primary server port
    static int secondaryPort = 5051;// Primary server port
    static int port;// Current port
    static boolean hasSecondaryServer = false;// Variable if secondary server exists
    static String serverMode = "p";// Mode of the server(primary/secondary)
    static String serverName = "";// Current server name

    public static void main(String args[]) {

        if (args.length < 1) {
            primaryServer = "localhost";
            serverName = primaryServer;// Set default server name to localhost when no args
        } else if (args.length == 1) {
            primaryServer = args[0];// Set primary server to the address in the first arg
        } else if (args.length == 2) {
            primaryServer = args[0];// Set primary server to the address in the first arg
            secondaryServer = args[1];// Set secondary server to the address in the second arg
            hasSecondaryServer = true;// Set secondary server exists to true
        }

        serverName = primaryServer;// Set default server name to primary
        port = primaryPort;// Set default port to primary port

        // Print the servers available
        System.out.println("Monica Narni's Joke Client Admin\n");
        System.out.println("Server one: " + primaryServer + ", port: " + primaryPort + "\n");
        if (hasSecondaryServer) {
            System.out.println("Server two: " + secondaryServer + ", port: " + secondaryPort + "\n");
        }
        // Create a buffer object for I/O operations to read the input name
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            String command;// Variable for user input
            System.out.println("Using server: " + serverName + ", Port:" + port + "\n");
            // Loop for user commands until quit
            do {
                // Print the current mode based on the server

                if (hasSecondaryServer && serverMode == "s") {
                    System.out.println(secondaryMode == "joke" ? "<S2>Joke Mode" : "<S2>Proverb Mode");

                } else {
                    System.out.println(((primaryMode == "joke") ? "Joke Mode" : "Proverb Mode"));

                }
                System.out.println("");

                System.out.println((serverMode == "s" ? "<S2>" : "")
                        + "Please enter 's' to switch the servers or 'quit' to cancel request or press 'Enter' for mode change:");
                System.out.flush(); // Flush the output stream to avoid overriding
                command = in.readLine();// Read user input
                // Switch the server on the input of 's'
                if ((command.equals("s"))) {
                    switchServer();// Method call to switch the current server
                } else if (command.indexOf("quit") < 0) // Look for user input to quit the process
                    changeMode(command, serverName);// Method call to change the mode of the current server
            } while (command.indexOf("quit") < 0); // Repeat this until the user input has 'quit'
            System.out.println("Cancelled by user request.");// Break the loop and print the message on the request to
                                                             // quit
        } catch (IOException x) {
            x.printStackTrace();// Print the exception message when it occurs
        }
    }

    static void switchServer() {// Method to switch the current server
        if (hasSecondaryServer) {// If secondary server exists switch the server on every call and set the
                                 // current server address and port accordingly
            if (serverMode == "p")
                serverMode = "s";
            else if (serverMode == "s")
                serverMode = "p";
            switch (serverMode) {
                case "p":
                    port = primaryPort;
                    serverName = primaryServer;
                    break;
                case "s":
                    port = secondaryPort;
                    serverName = secondaryServer;
                    break;
                default:
                    port = primaryPort;
                    serverName = primaryServer;
                    break;
            }
            // Print the change of server
            System.out.println("Now communicating with: " + serverName + ", port: " + port + "\n");
        } else
            System.out.println("No secondary server being used\n");// Print when there is no secondary server

    }

    static void changeMode(String command, String serverName) {// Method to change the mode of the current server
        Socket sock; // Socket variable to establish connection with the server
        BufferedReader fromServer; // Buffer variable to read the output from the server
        PrintStream toServer; // PrintStream variable to write the data to the output stream
        String textFromServer;// Variable for server response

        try {

            sock = new Socket(serverName, port);// Open a connection to server with a Socket object passing name and
                                                // port number

            fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));// Buffer object to read the
                                                                                          // output from the server
            toServer = new PrintStream(sock.getOutputStream());// Output stream to go to the server
            toServer.println("swap");// Send the message 'swap' to server to confirm mode change

            toServer.flush();// Flush the output stream to avoid overriding
            // Based on the current server swap the mode (joke/proverb)
            if (hasSecondaryServer && serverMode == "s") {
                if (secondaryMode == "joke")
                    secondaryMode = "proverb";
                else if (secondaryMode == "proverb")
                    secondaryMode = "joke";

            } else {
                if (primaryMode == "joke")
                    primaryMode = "proverb";
                else if (primaryMode == "proverb")
                    primaryMode = "joke";
            }
            textFromServer = fromServer.readLine();// Read the output lines from the server
            if (textFromServer != null)
                System.out.println(textFromServer);// Print the output from the server
            sock.close();// Close the connection with server
        } catch (IOException x) {
            System.out.println("Socket error.");
            x.printStackTrace();// Print the exception message when it occurs
        }

    }
}
