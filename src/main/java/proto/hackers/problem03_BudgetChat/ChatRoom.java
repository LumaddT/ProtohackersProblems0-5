package proto.hackers.problem03_BudgetChat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ChatRoom {
    private static final Logger logger = LogManager.getLogger();

    private static final Map<String, ChatUser> NamesToUsers = new ConcurrentHashMap<>();

    private static final byte[] GREETING = "Welcome to Budget Chat! What shall I call you?\n".getBytes();
    private static final String ROOM_CONTAINS_FORMAT_STRING = "* The room contains: %s\n";
    private static final String USER_CONNECTED_FORMAT_STRING = "* %s has entered the room\n";
    private static final String USER_LEFT_FORMAT_STRING = "* %s has left the room\n";
    private static final String MESSAGE_FORMAT_STRING = "[%s] %s\n";

    private static final int NAME_MAX_LENGTH = 24;
    private static final String NAME_REGEX = "^[a-zA-Z0-9]+$";
    private static final Pattern NAME_PATTERN = Pattern.compile(NAME_REGEX);

    public static void addUser(Socket socket) {
        BufferedReader inputStream;
        BufferedOutputStream outputStream;
        String name;

        try {
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new BufferedOutputStream(socket.getOutputStream());
            outputStream.write(GREETING);
            outputStream.flush();

            name = inputStream.readLine();
        } catch (IOException e) {
            logger.error("An IO exception was thrown by a Socket's streams.\n{}\n{}", e.getMessage(), e.getStackTrace());
            return;
        }

        if (!isNameValid(name)) {
            logger.debug("Invalid name {} rejected.", name);
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("An IO exception was thrown while closing the socket for {} (user not added due to illegal name).\n{}\n{}", name, e.getMessage(), e.getStackTrace());
            }
            return;
        }

        if (NamesToUsers.containsKey(name)) {
            logger.debug("The user {} already exists.", name);
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("An IO exception was thrown while closing the socket for {} (user not added due to duplicate name).\n{}\n{}", name, e.getMessage(), e.getStackTrace());
            }

            return;
        }

        ChatUser newUser = new ChatUser(name, socket, inputStream, outputStream);

        broadcastNewUser(name);

        String roomContainsMessage = ROOM_CONTAINS_FORMAT_STRING.formatted(getUsernamesString());
        new Thread(() -> newUser.sendMessage(roomContainsMessage)).start();

        NamesToUsers.put(name, newUser);

        new Thread(() -> monitor(newUser)).start();
    }

    private static void broadcastNewUser(String name) {
        String message = USER_CONNECTED_FORMAT_STRING.formatted(name);

        for (String user : NamesToUsers.keySet()) {
            if (user.equals(name)) {
                continue;
            }

            sendMessage(message, user);
        }
    }

    private static void broadcastMessage(String message, String sender) {
        String formattedMessage = MESSAGE_FORMAT_STRING.formatted(sender, message);
        for (String user : NamesToUsers.keySet()) {
            if (user.equals(sender)) {
                continue;
            }

            sendMessage(formattedMessage, user);
        }
    }

    private static boolean isNameValid(String name) {
        if (name == null) {
            return false;
        }

        if (name.isEmpty()) {
            return false;
        }

        if (name.length() > NAME_MAX_LENGTH) {
            return false;
        }

        Matcher nameMatcher = NAME_PATTERN.matcher(name);

        return nameMatcher.matches();
    }

    private static void sendMessage(String message, String receiver) {
        ChatUser user = NamesToUsers.get(receiver);

        if (user == null) {
            logger.error("Attempted to send the message {} to {}, but the user does not exist.", message.trim(), receiver);
            return;
        }

        new Thread(() -> user.sendMessage(message)).start();
    }

    private static String getUsernamesString() {
        StringBuilder returnValue = new StringBuilder();
        for (String name : NamesToUsers.keySet()) {
            returnValue.append(name);
            returnValue.append(", ");
        }

        if (!returnValue.isEmpty()) {
            returnValue.setLength(returnValue.length() - 2);
        }

        return returnValue.toString();
    }

    private static void monitor(ChatUser user) {
        while (user.isAlive()) {
            String message = user.getMessage();

            if (message == null) {
                disconnectUser(user);
            } else {
                broadcastMessage(message, user.getName());
            }
        }
    }

    public static void disconnectAll() {
        for (ChatUser user : NamesToUsers.values()) {
            disconnectUser(user);
        }

        NamesToUsers.clear();
    }

    private static void disconnectUser(ChatUser userToDisconnect) {
        logger.debug("Disconnecting {}.", userToDisconnect.getName());
        NamesToUsers.remove(userToDisconnect.getName());
        userToDisconnect.disconnect();

        String message = USER_LEFT_FORMAT_STRING.formatted(userToDisconnect.getName());

        for (String user : NamesToUsers.keySet()) {
            sendMessage(message, user);
        }
    }
}
