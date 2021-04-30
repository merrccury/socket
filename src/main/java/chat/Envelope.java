package chat;

import java.util.Date;

public class Envelope {

    private EnvelopeMethod method;
    private String sender;
    private String room;
    private Date timestamp;
    private String message;

    public Envelope(EnvelopeMethod method, String sender, String room, String message) {
        this.method = method;
        this.sender = sender;
        this.room = room;
        this.message = message;
        this.timestamp = new Date();
    }

    public Envelope(String input){
        String[] body = input.split("\\|");
        method = EnvelopeMethod.valueOf(body[0]);
        sender = body[1];
        room = body[2];
        timestamp = new Date(Long.parseLong(body[3]));
        message = body[4];
    }

    public EnvelopeMethod getMethod() {
        return method;
    }

    public void setMethod(EnvelopeMethod method) {
        this.method = method;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String serialize() {
        String serialize = "";
        serialize += method.name() + "|";
        serialize += sender + "|";
        serialize += room + "|";
        serialize += timestamp.getTime() + "|";
        serialize += message + "\n";
        return serialize;
    }

    @Override
    public String toString() {
        return "Envelope{" +
                "method=" + method +
                ", sender='" + sender + '\'' +
                ", room='" + room + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
