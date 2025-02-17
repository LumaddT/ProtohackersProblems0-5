package proto.hackers.part02_MeansToAnEnd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class MeansToAnEnd {
    private static final Logger logger = LogManager.getLogger();

    private static final int TIMEOUT = 1_000;

    private static final int MESSAGE_LENGTH = 9;
    private static final char INSERT_CHAR = 'I';
    private static final char QUERY_CHAR = 'Q';

    private static volatile boolean Running = false;

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

    private static void manageSocket(Socket socket) {
        try {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            int currentByte = 0;
            int[] message = new int[MESSAGE_LENGTH];
            PricesHolder pricesHolder = new PricesHolder();

            while (true) {
                do {
                    message[currentByte] = input.read();
                    currentByte++;
                    currentByte %= MESSAGE_LENGTH;
                } while (currentByte != 0);

                ClientMessage clientMessage = decodeClientMessage(message);

                if (clientMessage == null) {
                    continue;
                }

                logger.debug("Received {}.", clientMessage.toString());

                switch (clientMessage.getMessageType()) {
                    case INSERT -> pricesHolder.put(clientMessage.getFirstValue(), clientMessage.getSecondValue());
                    case QUERY -> {
                        int mean = pricesHolder.get(clientMessage.getFirstValue(), clientMessage.getSecondValue());
                        byte[] serverMessage = encodeServerMessage(mean);

                        logger.debug("Responded {} to {}.", mean, clientMessage.toString());
                        output.write(serverMessage);
                    }
                }
            }
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by a Socket or one of its streams.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    private static ClientMessage decodeClientMessage(int[] message) {
        ClientMessage.ClientMessageBuilder builder = ClientMessage.builder();
        if ((char) message[0] == INSERT_CHAR) {
            builder.MessageType(MessageTypes.INSERT);
        } else if ((char) message[0] == QUERY_CHAR) {
            builder.MessageType(MessageTypes.QUERY);
        } else {
            return null;
        }

        int firstValue = 0;
        int secondValue = 0;

        for (int i = 0; i < 4; i++) {
            firstValue = (firstValue << 8) | message[i + 1];
            secondValue = (secondValue << 8) | message[i + 5];
        }

        return builder.FirstValue(firstValue).SecondValue(secondValue).build();
    }

    private static byte[] encodeServerMessage(int value) {
        byte[] serverMessage = new byte[4];

        for (int i = 0; i < 4; i++) {
            serverMessage[i] = (byte) ((value >>> 8 * (3 - i)) & 0b11111111);
        }

        return serverMessage;
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
