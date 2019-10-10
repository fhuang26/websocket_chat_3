package com.example.webSocketChat.controller;

import com.alibaba.fastjson.JSON;
import com.example.webSocketChat.model.Message;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Server
 *
 * @see ServerEndpoint WebSocket Client
 * @see Session   WebSocket Session
 */

@Component
@ServerEndpoint("/chat")
public class WebSocketChatServer {

    /**
     * All chat sessions.
     */
    private static Map<String, Session> onlineSessions = new ConcurrentHashMap();
    private static Map<String, String> sessionUser = new ConcurrentHashMap();

    private static void sendMessageToAll(String msg) { // msg is a JSON string of the current message
        //TODO: add send message method.
        onlineSessions.forEach((id, session) -> {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Open connection, 1) add session, 2) add user.
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        //TODO: add on open connection.
        onlineSessions.put(session.getId(), session);

        // System.out.println("len of hash map=" + onlineSessions.size() + "  session=" + session.getId());
    }

    /**
     * Send message, 1) get username and session, 2) send message to all.
     */
    @OnMessage
    public void onMessage(Session session, String jsonStr) throws IOException {
        //TODO: add send message.

        Message message = JSON.parseObject(jsonStr, Message.class);
        if (message.getType().equals(Message.ENTER)) {
            sendMessageToAll(Message.jsonStr(message.getUsername(), message.getMessage(), Message.ENTER,
                    "" + onlineSessions.size()));
            return;
        }

        sessionUser.put(session.getId(), message.getUsername());
        sendMessageToAll(Message.jsonStr(message.getUsername(), message.getMessage(), message.getType(),
                "" + onlineSessions.size()));
    }

    /**
     * Close connection, 1) remove session, 2) update user.
     */
    @OnClose
    public void onClose(Session session) throws IOException {
        //TODO: add close connection.
        /*
          System.out.println("session=" + session.getId() + "  size of hash map=" + onlineSessions.size() +
                "  size of sessionUser=" + sessionUser.size());
         */
        onlineSessions.remove(session.getId());
        if (sessionUser.containsKey(session.getId())) {
            String user = sessionUser.get(session.getId());
            sendMessageToAll(Message.jsonStr(user, "offline", Message.QUIT, "" + onlineSessions.size()));
            sessionUser.remove(session.getId());
        }
        session.close();
    }

    /**
     * Print exception.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}