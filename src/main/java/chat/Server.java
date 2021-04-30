package chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.temporal.ValueRange;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class Server {

    public static void main(String[] args) throws IOException {
        new Server(Integer.parseInt(args[0])).listen();
    }

    ServerSocket server;
    static List<SocketHandler> socketConnections;
    static List<String> rooms;

    public Server(int port) throws IOException {
        server = new ServerSocket(port);
        socketConnections = new CopyOnWriteArrayList<>();
        rooms = new CopyOnWriteArrayList<>();
    }

    public void listen() throws IOException {
        while (true) {
            Socket clientSocket = server.accept();
            new Thread(new SocketHandler(clientSocket)).start();
        }
    }

    static void diagnostic(String label) {
        System.out.print(label + "    socketConnections - " + socketConnections.size());
        System.out.println("    rooms - " + rooms.size());
    }

    static class SocketHandler implements Runnable {

        private final BufferedReader in;
        private final BufferedWriter out;
        private String username;
        private String room;
        private final Socket clientSocket;
        private Envelope envelope;

        public String getUsername() {
            return username;
        }

        public String getRoom() {
            return room;
        }

        SocketHandler(Socket clientSocket) throws IOException {
            this.room = "unknown";
            this.clientSocket = clientSocket;
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }

        @Override
        public void run() {

            try {
                do {
                    envelope = new Envelope(in.readLine());
                    System.out.println(envelope);
                    diagnostic("before");
                    if (envelope.getMethod() == EnvelopeMethod.DISCONNECT) {
                        diagnostic("after");
                        break;
                    }
                    switch (envelope.getMethod()) {
                        case CONNECT:
                            socketConnections.add(this);
                            username = envelope.getSender();
                            break;
                        case CREATE_ROOM:
                            rooms.add(envelope.getRoom());
                            break;
                        case DELETE_ROOM:
                            rooms.remove(envelope.getRoom());
                            break;
                        case LIST_OF_ROOMS:
                            String message = rooms.stream().reduce("", (left, right) -> left + " " + right);
                            if (message.equals(""))
                                message = "Rooms not created";
                            envelope = new Envelope(EnvelopeMethod.LIST_OF_ROOMS, "server", "server", message);
                            out.write(envelope.serialize());
                            out.flush();
                            break;
                        case CONNECT_ROOM:
                            room = envelope.getRoom();
                            break;
                        case DISCONNECT_ROOM:
                            room  = "unknown";
                            envelope = new Envelope(EnvelopeMethod.DISCONNECT_ROOM, "server", "unknown", "unknown");
                            out.write(envelope.serialize());
                            out.flush();
                            break;
                        case SEND_MESSAGE:
                            final String sender = envelope.getSender();
                            final String room = envelope.getRoom();
                            socketConnections.stream()
                                    .filter(socketHandler -> socketHandler.getRoom().equals(room) && !socketHandler.getUsername().equals(sender))
                                    .forEach(socketHandler -> {
                                envelope.setMethod(EnvelopeMethod.RECEIVE_MESSAGE);
                                try {
                                    socketHandler.out.write(envelope.serialize());
                                    socketHandler.out.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            break;
                    }
                    diagnostic("after");
                }
                while (true);
                socketConnections.remove(this);
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
