/*--------------------------------------------------------

1. Mounica Narni / 9/21/2022:

2. Java version used (java -version), if not the official version for the class:

    "17.0.2"

3. Precise command-line compilation examples / instructions:

    > javac JokeServer.java

4. Precise examples / instructions to run this program:

> java JokeServer
    This will run JokeServer, connect the primary server on localhost at port 4545
> java JokeServer secondary
    This will run JokeServer, connect the secondary server on localhost at port 4546

5. List of files needed for running the program.

 a. JokeServer.java
 b. JokeClient.java
 c. JokeClientAdmin.java

6. Notes:

JokeServer accepts client requests, creates a new thread for each request and returns joke and proverb responses to the client along with the updated state.
When the client sends its current joke and proverb state, JokeServer ensures to not repeat the joke or proverb within a cycle and returns random jokes and proverbs.
The jokes and proverbs are returned based on the current mode of the server, which is joke mode by default but can be changed by JokeClientAdmin.

It intiliazes a thread to listen to JokeClientAdmin on port 5050 if its a primary server and port 5051 if its a secondary server. 
It can swap the modes between joke and proverb and JokeServer return responses based on its current mode.

Both primary and secondary servers run independently.

----------------------------------------------------------*/

import java.io.*; //Import Java I/O libraries
import java.net.*; //Import Java networking libraries
import java.util.*;// Import Java utility libraries

class Worker extends Thread { // Worker class definition, it extend built-in Thread class
    Socket sock; // Local socket variable

    Worker(Socket s) {
        sock = s;
    } // Worker constructor accepts a socket arg and assigns it to local variable

    public void run() {

        PrintStream out = null; // PrintStream variable to write data to the output stream
        BufferedReader in = null; // Buffer variable to read from the input stream
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));// Buffer object to read the input
                                                                                  // stream from client
            out = new PrintStream(sock.getOutputStream());// PrintStream object to write to the output stream in
                                                          // response to client

            try {
                String name;// Variable for user name

                name = in.readLine();// Read the user name from client
                JokeServer.clientJokes = in.readLine();// Read the current joke state on the current server from client
                JokeServer.clientProverbs = in.readLine();// Read the current proverb state on the current server from
                                                          // client
                // Send responses based on primary or secondary server
                if (JokeServer.secondaryServerMode) {

                    out.println(JokeServer.secondaryMode);// Write to the o/p stream the current server mode

                    if (JokeServer.secondaryMode.equals("joke")) {
                        printJoke(name, out); // Method call to fetch a joke and send it to client

                    } else {
                        printProverb(name, out); // Method call to fetch a proverb and send it to client

                    }
                } else {

                    out.println(JokeServer.mode);

                    if (JokeServer.mode.equals("joke")) {
                        printJoke(name, out); // Method call to fetch a joke and send it to client
                    } else {
                        printProverb(name, out); // Method call to fetch a proverb and send it to client
                    }
                }
            } catch (IOException x) {
                System.out.println("Server read error");
                x.printStackTrace();// Print the exception message when it occurs
            }
            sock.close(); // close the connection
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }

    static void printJoke(String name, PrintStream out) {// Method to fetch a random joke without repetition in a cycle
                                                         // and send it to client
        // Jokes from https://parade.com/1287449/marynliles/short-jokes/
        try {
            // Collection of jokes
            Map<String, String> jokes = new HashMap<String, String>();
            jokes.put("JA", "What do you call an ant who fights crime? A vigilANTe!");
            jokes.put("JB", "Where did the music teacher leave her keys? In the piano!");
            jokes.put("JC", "What's Thanos's favorite app on his phone? Snapchat!");
            jokes.put("JD", "What is a room with no walls? A mushroom!");

            List<Map.Entry<String, String>> randomJokes = new ArrayList<>(jokes.entrySet());
            Collections.shuffle(randomJokes);// Shuffle the jokes for randomization

            String j = JokeServer.clientJokes;
            boolean found = false;
            // Loop to return the joke which was not returned previously based on the client
            // state
            for (Map.Entry<String, String> k : randomJokes) {
                String ID = (String) k.getKey();
                if (!j.contains(ID)) {
                    out.println(k.getKey() + " " + name + ": " + k.getValue());// Write the joke to the o/p stream and
                                                                               // send to client
                    j = j + k;
                    found = true;
                    System.out.println((JokeServer.secondaryServerMode ? "<S2>" : "") + "Sent joke " + k.getKey()
                            + " to " + name + "\n");

                    break;
                }
            }
            if (found) {
                JokeServer.clientJokes = j;
                // Check to see if all the jokes are returned within a cycle
                if (j.contains("JA") && j.contains("JB") && j.contains("JC") && j.contains("JD")) {
                    System.out.println((JokeServer.secondaryServerMode ? "<S2>" : "") + "Joke cycle completed for "
                            + name + "\n");
                }
                out.println(JokeServer.clientJokes);

            } else {
                JokeServer.clientJokes = "";
                printJoke(name, out);
            }

        } catch (Exception ex) {
        }
    }

    static void printProverb(String name, PrintStream out) {// Method to fetch a random proverb without repetition in a
                                                            // cycle and send it to client
        // Proverbs from https://lemongrad.com/proverbs-with-meanings-and-examples/
        try {
            HashMap<String, String> proverbs = new HashMap<String, String>();
            proverbs.put("PA", "A bad workman always blames his tools.");
            proverbs.put("PB", "A happy heart is better than a full purse.");
            proverbs.put("PC", "All is fair in love and war.");
            proverbs.put("PD", "A rolling stone gathers no moss.");

            List<Map.Entry<String, String>> randomProverbs = new ArrayList<>(proverbs.entrySet());
            Collections.shuffle(randomProverbs);

            String p = JokeServer.clientProverbs;
            boolean found = false;
            // Loop to return the proverb which was not returned previously based on the
            // client state
            for (Map.Entry<String, String> k : randomProverbs) {
                String ID = (String) k.getKey();
                if (!p.contains(ID)) {
                    out.println(k.getKey() + " " + name + ": " + k.getValue());// Write the proverb to the o/p stream
                                                                               // and send to client
                    p = p + k;
                    found = true;
                    System.out.println((JokeServer.secondaryServerMode ? "<S2>" : "") + "Sent proverb " + k.getKey()
                            + " to " + name + "\n");
                    break;
                }
            }

            if (found) {
                JokeServer.clientProverbs = p;
                // Check to see if all the proverbs are returned within a cycle
                if (p.contains("PA") && p.contains("PB") && p.contains("PC") && p.contains("PD")) {
                    System.out.println((JokeServer.secondaryServerMode ? "<S2>" : "") + "Proverb cycle completed for "
                            + name + "\n");
                }
                out.println(JokeServer.clientProverbs);
            } else {
                JokeServer.clientProverbs = "";
                printProverb(name, out);
            }

        } catch (Exception ex) {
            out.println("Failed in atempt to look up " + name);
        }
    }
}

public class JokeServer {

    static String mode = "joke";// Primary server mode
    static String secondaryMode = "joke";// Secondary server mode
    static String clientJokes = "";// Variable for client joke state
    static String clientProverbs = "";// Variable for client server state
    static boolean secondaryServerMode = false;// Variable if secondary server exists

    public static void main(String args[]) throws IOException {

        if (args.length == 1 && args[0].equals("secondary")) {// Checking for first arg for secondary server
            secondaryServerMode = true;// Set to true if secondary server exists
        }

        int q_len = 6; // Maximum no. of connection requests allowed in the queue before creation of
                       // new thread
        int primaryPort = 4545; // Primary Port number
        int secondaryPort = 4546;// Secondary port number
        int port;// Current port

        Socket sock; // Socket variable for connection with client
        port = primaryPort;// Default set to primary port
        if (secondaryServerMode) {
            port = secondaryPort;// Set to secondary port if it exists
        }
        // Print the details of current server and mode
        System.out.println("Monica Narni's Joke Server\n");

        System.out.println("Using " + ((secondaryServerMode) ? "Secondary" : "Primary") + " server" + ", Port:" + port);

        ServerSocket servsock = new ServerSocket(port, q_len); // Serversocket object created with port number
                                                               // and max.
        System.out.print(secondaryServerMode ? "\n<S2>" : "\n");
        System.out.println(secondaryServerMode ? ((secondaryMode == "joke") ? "Joke mode" : "Proverb mode")
                : ((mode == "joke") ? "Joke mode" : "Proverb mode"));

        // Async thread for Joke client admin to listen for mode changes
        AdminThread admin = new AdminThread();
        Thread t = new Thread(admin);
        t.start();

        while (true) {
            sock = servsock.accept(); // Accept the connection requests from client
            new Worker(sock).start(); // Create new Worker object to begin execution of the thread, start() calls the
                                      // run() method
        }
    }

}

class AdminThread implements Runnable {// Class to listen to the JokeClientAdmin requests

    public void run() {

        int q_len = 6; // Maximum no. of connection requests allowed in the queue before creation of
                       // new thread

        int primaryPort = 5050; // Primary port for JokeClientAdmin
        int secondaryPort = 5051;// Secondary port for JokeClientAdmin
        int port;// Current port
        Socket sock;

        port = primaryPort;// Default set to primary port
        if (JokeServer.secondaryServerMode) {
            port = secondaryPort;// Set to secondary port if it exists
        }

        try {
            ServerSocket servsock = new ServerSocket(port, q_len);
            while (true) {
                sock = servsock.accept();
                new AdminWorker(sock).start();//Create a new AdminWorker thread
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

class AdminWorker extends Thread {
    Socket sock; // Local socket variable

    AdminWorker(Socket s) {
        sock = s;
    }

    public void run() {

        PrintStream out = null; // PrintStream variable to write data to the output stream
        BufferedReader in = null; // Buffer variable to read from the input stream
        try {
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));// Buffer object to read the input
                                                                                  // stream from client
            out = new PrintStream(sock.getOutputStream());// PrintStream object to write to the output stream in
                                                          // response to client

            try {
                String swap;
                swap = in.readLine();// Read the input from JokeClientAdmin
                // Change the current mode of the current server(primary/secondary) and print
                // the log
                if (swap.equals("swap")) {
                    if (JokeServer.secondaryServerMode) {
                        if (JokeServer.secondaryMode.equals("joke")) {
                            System.out.println("<S2> Proverb mode");
                            JokeServer.secondaryMode = "pro";
                        } else {
                            System.out.println("<S2> Joke mode");

                            JokeServer.secondaryMode = "joke";
                        }
                    } else {

                        if (JokeServer.mode.equals("joke")) {
                            System.out.println("Proverb mode");

                            JokeServer.mode = "pro";
                        } else {
                            System.out.println("Joke mode");

                            JokeServer.mode = "joke";
                        }
                    }
                }

            } catch (Exception x) {
                System.out.println("Server read error");
                x.printStackTrace();// Print the exception message when it occurs
            }
            sock.close(); // close the connection
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
    }
}
