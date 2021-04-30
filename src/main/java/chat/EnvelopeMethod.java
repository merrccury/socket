package chat;

public enum EnvelopeMethod {
    CONNECT,
    DISCONNECT,

    CREATE_ROOM,
    DELETE_ROOM,

    CONNECT_ROOM,
    DISCONNECT_ROOM,

    LIST_OF_ROOMS,

    SEND_MESSAGE,
    RECEIVE_MESSAGE,
}
