import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClientThread implements Runnable {

    // socket passed from the ChatServer class - holds the connection between the client and server
    private Socket socket;
    // used to read data from the associated client
    private BufferedReader bufferedReader;
    // used to send data to the associated client, specifically from the server and other clients
    private BufferedWriter bufferedWriter;
    // client username
    private String clientUsername;
    // list of active client threads and thus users
    public static ArrayList<ChatClientThread> activeClientThreads = new ArrayList<>();


    // constructor:
    public ChatClientThread(Socket socket) {
        try {
            this.socket = socket;
            // get socket's input stream, wrapped in the bufferedReader, allowing clientThread to receive messages from the associated client
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // get socket's output stream, wrapped in the bufferedWriter, allowing clientThread to write messages to the associated client (from the server)
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // client user is first prompted to provide their username, so the first line into the bufferedReader will be the username
            this.clientUsername = bufferedReader.readLine();
            // add clientThread and thus the associated client to the activeClientThreads ArrayList
            activeClientThreads.add(this);
            // announce that associated client has joined the chatroom
            messageChatroom("[SERVER]: " + clientUsername + " has joined the chat.");
        } catch (IOException e) {
            closeClientThread(socket, bufferedReader, bufferedWriter);
        }
    }


    // method THREADING: listens and reads messages from the associated client so long as the corresponding socket is connected
    @Override
    public void run() {
        String messageFromClient;
        while (socket.isConnected()) {
            try {
                // this operation is performed within the thread so that other tasks are not blocked and can occur simultaneously
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient != null) {
                    messageChatroom(messageFromClient);
                }
            } catch (IOException e) {
                closeClientThread(socket, bufferedReader, bufferedWriter);
                // if client disconnects the break will exit while-loop
                break;
            }
        }
    }

    // method: broadcasts client's messages to the chatroom using the ArrayList of active clients
    public void messageChatroom(String messageToSend){
        for (ChatClientThread clientThread : activeClientThreads) {
            try {
                // message to all OTHER clients
                if (!clientThread.clientUsername.equals(clientUsername)) {
                    // use each clientThread's bufferedWriter to write the message to the associated client
                    clientThread.bufferedWriter.write(messageToSend);
                    // append messageToSend with new-line since the messageFromClient uses .readLine()
                    clientThread.bufferedWriter.newLine();
                    // manually flush the buffer in case it is not filled
                    clientThread.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeClientThread(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // method: safely closes down the socket, by first closing its input and output streams
    public void closeClientThread(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeChatClientThread();
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

    // method: remove clientThread from the active client thread list and notifies chatroom
    public void removeChatClientThread() {
        activeClientThreads.remove(this);
        messageChatroom("[SERVER]: " + clientUsername + " has left the chat.");
    }
}

