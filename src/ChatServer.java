import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {

    private ServerSocket serverSocket;
    // used to read data from the server console - assumed admin user
    private BufferedReader bufferedReader;


    // constructor:
    public ChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    }

    // method: run server to continuously listen for new client connections
    public void runServer() {
        try {
            while(!serverSocket.isClosed()) {
                // open server socket and await connections from clients
                Socket clientSocket = serverSocket.accept();
                // state when successful connection with client is made
                System.out.println("New client has successfully connected.");

                // Pass newly established connection (socket) to a thread (runnable implementation)
                ChatClientThread clientThread = new ChatClientThread(clientSocket);
                Thread ct = new Thread(clientThread);
                ct.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    // method THREADING: listens for input from the server's console - user is considered admin
    public void readAdminMessage() {
        Thread stAdminMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                String adminMessage;
                while (!serverSocket.isClosed()) {
                    try {
                        adminMessage = bufferedReader.readLine();
                        // echo input for confirmationtion
                        System.out.println("[Received]: " + adminMessage);
                        if (adminMessage != null && adminMessage.equals("EXIT")) {
                            System.out.println("Server shutdown initiated, the server and chatroom will now close. Bye!");
                            ChatClientThread.serverShutdownAlert();
                            closeServerSocket();
                            System.exit(1);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Server cannot currently accept admin messages. Please restart.");
                    }
                }
            }
        });
        stAdminMessage.start();
    }

    // method: closes server socket
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                bufferedReader.close();
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method: main - creates server object and runs said object
    public static void main(String[] args) {
        // CLI configuration
        try {
            int serverPort = 14001;
            // check for -csp parameter to change server port
            if (args.length > 0) {
                if (!args[0].equals("-csp")) {
                    System.err.println("The following command line interface options supported: -csp");
                    System.exit(1);
                }
                if (args.length != 2) {
                    System.err.println("Command line interface option -csp requires only one argument.");
                    System.exit(1);
                }
                try {
                    serverPort = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Argument for -csp must be an integer.");
                    System.exit(1);
                }
            }
            ServerSocket chatroomServerSocket = new ServerSocket(serverPort);
            ChatServer chatServer = new ChatServer(chatroomServerSocket);
            System.out.println("Port: " + serverPort);
            System.out.println("Chat server online.");
            // call methods for server functionality - receiving admin input will occur simultaneously due to threading
            chatServer.readAdminMessage();
            chatServer.runServer();
        } catch (ConnectException e) {
            System.out.println("Connection error: Are you sure there is a server listening?");
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }
}
