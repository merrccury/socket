package chat;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client {
    private Socket serverSocket;
    private final BufferedReader reader;
    private final BufferedReader in;
    private final BufferedWriter out;
    private String username;
    private Envelope envelope;

    public Client(String host, int port) throws IOException {
        serverSocket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(System.in));
        in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()));
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client(args[0], Integer.parseInt(args[1]));
        try {
            client.start();
        } catch (IOException e) {
            client.close();
        }
    }

    public void start() throws IOException {
        System.out.print("Enter your username: ");
        username = reader.readLine();
        envelope = new Envelope(EnvelopeMethod.CONNECT, username, "unknown", "unknown");
        out.write(envelope.serialize());

        while (true) {
            switch (selectChoice()) {
                case 1: //Get List Of Rooms
                    envelope = new Envelope(EnvelopeMethod.LIST_OF_ROOMS, username, "unknown", "unknown");
                    out.write(envelope.serialize());
                    out.flush();
                    envelope = new Envelope(in.readLine());
                    System.out.println("Available rooms: " + envelope.getMessage());
                    break;
                case 2: //Connect to the Room
                    System.out.println("Enter the name of the room");
                    String room = reader.readLine();
                    envelope = new Envelope(EnvelopeMethod.CONNECT_ROOM, username, room, "unknown");
                    out.write(envelope.serialize());
                    out.flush();
                    System.out.println("If you want to leave the chat enter \"!EXIT\"");
                    EnvelopeReceiver receiver = new EnvelopeReceiver();
                    ExecutorService service = Executors.newFixedThreadPool(1);
                    service.execute(new EnvelopeReceiver());
                    while (true){
                        String input = reader.readLine();
                        if (input.equals("!EXIT"))
                            break;
                        envelope = new Envelope(EnvelopeMethod.SEND_MESSAGE, username, room, input);
                        out.write(envelope.serialize());
                        out.flush();
                    }
                    service.shutdownNow();
                    envelope = new Envelope(EnvelopeMethod.DISCONNECT_ROOM, username, "unknown", "unknown");
                    out.write(envelope.serialize());
                    out.flush();
                    break;
                case 3: //Create Room
                    System.out.print("Enter the name of the new room: ");
                    String newRoom = reader.readLine();
                    envelope = new Envelope(EnvelopeMethod.CREATE_ROOM, username, newRoom, "unknown");
                    out.write(envelope.serialize());
                    out.flush();
                    break;
                case 4: //Delete Room
                    System.out.print("Enter the room you want to delete: ");
                    String oldRoom = reader.readLine();
                    envelope = new Envelope(EnvelopeMethod.DELETE_ROOM, username, oldRoom, "unknown");
                    out.write(envelope.serialize());
                    out.flush();
                    break;
                case 5: //Exit
                    close();
                    return;

            }
        }
    }

    public int selectChoice() throws IOException {
        int choice = 0;

        do {
            System.out.println("    Menu");
            System.out.println("->1 Get List Of Rooms");
            System.out.println("->2 Connect to the Room");
            System.out.println("->3 Create Room");
            System.out.println("->4 Delete Room");
            System.out.println("->5 Exit");
            choice = Integer.parseInt(reader.readLine());
        }
        while (choice > 5 || choice < 1);
        return choice;
    }

    private void close() throws IOException {
        envelope = new Envelope(EnvelopeMethod.DISCONNECT, username, "unknown", "unknown");
        out.write(envelope.serialize());
        out.flush();
        in.close();
        out.close();
        serverSocket.close();
    }

    class EnvelopeReceiver implements Runnable {
        public void run(){
            Envelope envelope = null;
            while(true){
                try {
                    envelope = new Envelope(in.readLine());
                    if(envelope.getMethod() == EnvelopeMethod.DISCONNECT_ROOM)
                        break;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                    System.out.println(dateFormat.format(envelope.getTimestamp()) + " " + envelope.getSender() + " " + envelope.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
