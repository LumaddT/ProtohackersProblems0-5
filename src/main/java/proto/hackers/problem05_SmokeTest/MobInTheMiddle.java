package proto.hackers.problem05_SmokeTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MobInTheMiddle {
    private static final Logger logger = LogManager.getLogger();

    private static final int TIMEOUT = 1_000;

    private static volatile boolean Running = false;

    private static final String UPSTREAM_SOCKET = "chat.protohackers.com";
    private static final int UPSTREAM_PORT = 16963;

    public static void run(int port) {
        if (Running) {
            logger.warn("Attempted to run, but this is already running.");
            return;
        }

        Running = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Started on port {}.", port);

            serverSocket.setSoTimeout(TIMEOUT);

            while (Running) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> manageSocket(socket)).start();
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timed out (timeout: {}) in thread {}.", TIMEOUT, Thread.currentThread().toString());
                }
            }
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by the SocketServer. No attempt will be made to reopen the socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    private static void manageSocket(Socket downstreamSocket) {
        Socket upstreamSocket;
        try {
            upstreamSocket = new Socket(UPSTREAM_SOCKET, UPSTREAM_PORT);
            MobChatUser user = new MobChatUser(downstreamSocket, upstreamSocket);
            user.run();
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by a Socket or one of its streams.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    public static void stop() {
        if (Running) {
            logger.info("Stopped.");
        } else {
            logger.warn("Attempted to stop, but this is already stopped.");
        }
        Running = false;
    }
}
