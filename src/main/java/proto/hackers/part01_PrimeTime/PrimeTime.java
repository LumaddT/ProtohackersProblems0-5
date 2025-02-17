package proto.hackers.part01_PrimeTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;

public class PrimeTime {
    private static final Logger logger = LogManager.getLogger();

    private static final int TIMEOUT = 1_000;
    private static final String VALID_METHOD = "isPrime";

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
        ObjectMapper jsonMapper = JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .build();
        TypeFactory typeFactory = jsonMapper.getTypeFactory();

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
            while (!socket.isClosed()) {
                String line = input.readLine();

                if (line == null) {
                    logger.debug("null input received.");
                    output.write("{}\n".getBytes());
                    output.flush();
                    socket.close();
                    break;
                }

                ClientMessage clientMessage;

                try {
                    clientMessage = jsonMapper.readValue(line, typeFactory.constructType(ClientMessage.class));
                } catch (JsonProcessingException e) {
                    logger.debug("\"{}\" is a malformed JSON.", line);
                    output.write("{}\n".getBytes());
                    output.flush();
                    socket.close();
                    break;
                }

                ServerMessage serverMessage;

                if (!Objects.equals(clientMessage.getMethod(), VALID_METHOD)) {
                    logger.debug("\"{}\" has an invalid method.", line);

                    output.write("{}".getBytes());
                    socket.close();
                    break;
                }

                if (isPrime(clientMessage.getNumber())) {
                    logger.debug("\"{}\" is valid and prime.", line);
                    serverMessage = new ServerMessage(VALID_METHOD, true);
                } else {
                    logger.debug("\"{}\" is not prime.", line);
                    serverMessage = new ServerMessage(VALID_METHOD, false);
                }

                String messageString = jsonMapper.writeValueAsString(serverMessage) + "\n";
                output.write(messageString.getBytes());
                output.flush();
            }

            socket.close();
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by a Socket or one of its streams.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }

    }

    private static boolean isPrime(double number) {
        if (Math.floor(number) != number) {
            return false;
        }

        if (number <= 1) {
            return false;
        }

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
