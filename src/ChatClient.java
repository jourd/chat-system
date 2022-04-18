import java.io.*;
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
            // start scanner for client terminal
            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter your client username: ");
            // will capture client's username
            String clientUsername = scanner.nextLine();

            // create socket object and client object to enable connection
            Socket socket = new Socket("localHost", 14001);
            ChatClient client = new ChatClient(socket, clientUsername);

            // call methods for client functionality - listening will occur simultaneously due to threading
            client.receiveMessage();
            client.sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
