import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    // this will be the socket passed from the Server class
    private Socket socket;
    // used to read data from the client
    private BufferedReader bufferedReader;
    // used to write data to the client, specifically from the server and other clients
    private BufferedWriter bufferedWriter;
    // client username
    private String clientUsername;


    // constructor:
    public ChatClient (Socket socket, String clientUsername) {
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUsername = clientUsername;
        } catch (IOException e) {
            closeClientSocket(socket, bufferedReader, bufferedWriter);
        }
    }


    // method THREADING: listens for and receives messages from the chatroom
    public void receiveMessage() {
        Thread ctReceive = new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromChatroom;
                while (socket.isConnected()) {
                    try {
                        messageFromChatroom = bufferedReader.readLine();
                        System.out.println(messageFromChatroom);
                    } catch (IOException e) {
                        closeClientSocket(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        });
        ctReceive.start();
    }

    // method: sends messages from client to associated client threader
    public void sendMessage() {
        try {
            bufferedWriter.write(clientUsername);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            // scan the terminal for input from the associated client
            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write("[" + clientUsername + "]: " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeClientSocket(socket, bufferedReader, bufferedWriter);
        }
    }

    // method: safely closes the client's socket, by first closing its input and output streams
    public void closeClientSocket(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // method: main
    public static void main(String[] args) {
        try {
            int serverPort = 14001;
            int i = 0;
            String hostAddress = "localHost";
            while(args.length > i + 1) {
                System.out.println("Looping");
                // check for -ccp parameter to change client port
                if(args[i].equals("-ccp")) {
                    try {
                        serverPort = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Argument for -ccp must be an integer.");
                        System.exit(1);
                    }
                }
                // check for -cca parameter to change client address
                else if(args[i].equals("-cca")) {
                    hostAddress = args[i + 1];
                }
                // unknown parameter entered
                else {
                    System.err.println("Option " + args[i] + " unknown.");
                    System.exit(1);
                }
                i += 2;
            }
            System.out.println("Port: " + Integer.toString(serverPort));
            System.out.println("Address: " + hostAddress);

            // scanner for client username
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your client username: ");
            String clientUsername = scanner.nextLine();

            // create socket object and client object to enable connection
            Socket socket;
            ChatClient client;
            try {
                socket = new Socket(hostAddress, serverPort);
                client = new ChatClient(socket, clientUsername);
                // call methods for client functionality - receiving will occur simultaneously due to threading
                client.receiveMessage();
                client.sendMessage();
            } catch (ConnectException e) {
                System.out.println("Connection error: server cannot be found at specified address.");
                System.exit(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
