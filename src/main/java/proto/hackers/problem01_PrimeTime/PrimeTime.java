package proto.hackers.problem01_PrimeTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;

public class PrimeTime {
    private static final Logger logger = LogManager.getLogger();

    private static final int TIMEOUT = 1_000;

    private static final String IS_PRIME_STRING = "isPrime";

    private static volatile boolean Running = false;

    public static void run(int port) {
        if (Running) {
            logger.warn("Attempted to run, but this is already running.");
            return;
        }

        Running = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Started on port {}.", port);

            JsonMapper jsonMapper = JsonMapper.builder()
                    .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                    .build();

            serverSocket.setSoTimeout(TIMEOUT);

            while (Running) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> manageSocket(socket, jsonMapper)).start();
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timed out (timeout: {}) in thread {}.", TIMEOUT, Thread.currentThread().toString());
                }
            }
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by the SocketServer. No attempt will be made to reopen the socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    private static void manageSocket(Socket socket, JsonMapper jsonMapper) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());

            while (!socket.isClosed()) {
                String line = input.readLine();

                ClientMessage clientMessage = parseClientMessage(line, jsonMapper);

                if (clientMessage == null) {
                    logger.debug("\"{}\" is malformed.", line);

                    output.write("{}\n".getBytes());
                    output.flush();
                    socket.close();
                } else {
                    ServerMessage serverMessage;

                    if (isPrime(clientMessage.getNumber())) {
                        logger.debug("\"{}\" is valid and prime.", line);
                        serverMessage = new ServerMessage(IS_PRIME_STRING, true);
                    } else {
                        logger.debug("\"{}\" is valid and not prime.", line);
                        serverMessage = new ServerMessage(IS_PRIME_STRING, false);
                    }

                    String messageString = jsonMapper.writeValueAsString(serverMessage) + "\n";
                    output.write(messageString.getBytes());
                    output.flush();
                }
            }

            socket.close();
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by a Socket or one of its streams.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }

    }

    private static ClientMessage parseClientMessage(String line, JsonMapper jsonMapper) {
        if (line == null) {
            return null;
        }

        ClientMessage clientMessage;

        try {
            clientMessage = jsonMapper.readValue(line, jsonMapper.getTypeFactory().constructType(ClientMessage.class));
        } catch (JsonProcessingException e) {
            return null;
        }

        if (!Objects.equals(clientMessage.getMethod(), IS_PRIME_STRING)) {
            return null;
        }

        return clientMessage;
    }

    private static boolean isPrime(double number) {
        if (Math.floor(number) != number) {
            return false;
        }

        if (number <= 1) {
            return false;
        }

        // Of course the primality check could be better
        for (int i = 2; i <= Math.sqrt(number); i++) {
            if (number % i == 0) {
                return false;
            }
        }

        return true;
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
