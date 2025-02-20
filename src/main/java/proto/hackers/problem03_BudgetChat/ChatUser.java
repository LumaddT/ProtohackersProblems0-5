package proto.hackers.problem03_BudgetChat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

@RequiredArgsConstructor
class ChatUser {
    private static final Logger logger = LogManager.getLogger();

    @Getter
    private final String Name;
    private final Socket Socket;
    private final BufferedReader InputStream;
    private final BufferedOutputStream OutputStream;

    @Getter
    private boolean Alive = true;

    public synchronized void sendMessage(String message) {
        try {
            OutputStream.write(message.getBytes());
            OutputStream.flush();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while writing the message {} to {}.\n{}\n{}", message.trim(), Name, e.getMessage(), e.getStackTrace());
        }
    }

    public String getMessage() {
        try {
            return InputStream.readLine();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while trying to read a message from {}.\n{}\n{}", Name, e.getMessage(), e.getStackTrace());
            return null;
        }
    }

    public void disconnect() {
        try {
            Alive = false;
            Socket.close();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while closing the socket for {}.\n{}\n{}", Name, e.getMessage(), e.getStackTrace());
        }
    }
}
