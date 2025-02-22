package proto.hackers.problem05_SmokeTest;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class MobChatUser {
    private static final Logger logger = LogManager.getLogger();

    private static final String BOGUS_ADDRESS = "7YWHMfk9JZe0LM0g1ZauHuiSxhI";
    private static final String ADDRESS_REGEX = "(?<=^| )7[a-zA-Z0-9]{25,34}(?=$| )";

    private final java.net.Socket DownatreamSocket;
    private final BufferedReader DownstreamInputStream;
    private final BufferedOutputStream DownstreamOutputStream;

    private final java.net.Socket UpstreamSocket;
    private final BufferedReader UpstreamInputStream;
    private final BufferedOutputStream UpstreamOutputStream;

    public MobChatUser(Socket downatreamSocket, Socket upstreamSocket) throws IOException {
        DownatreamSocket = downatreamSocket;
        UpstreamSocket = upstreamSocket;

        DownstreamInputStream = new BufferedReader(new InputStreamReader(downatreamSocket.getInputStream()));
        DownstreamOutputStream = new BufferedOutputStream(downatreamSocket.getOutputStream());

        UpstreamInputStream = new BufferedReader(new InputStreamReader(upstreamSocket.getInputStream()));
        UpstreamOutputStream = new BufferedOutputStream(upstreamSocket.getOutputStream());
    }

    @Getter
    private boolean Alive = true;

    public void run() {
        new Thread(this::manageDownstreamInput).start();
        new Thread(this::manageUpstreamInput).start();
    }

    private void manageDownstreamInput() {
        while (Alive) {
            try {
                StringBuilder lineBuilder = new StringBuilder();

                while (true) {
                    int ch = DownstreamInputStream.read();
                    if (ch == -1) {
                        break;
                    }

                    lineBuilder.append((char) ch);

                    if (ch == '\n') {
                        break;
                    }
                }

                if (!lineBuilder.isEmpty() && lineBuilder.charAt(lineBuilder.length() - 1) != '\n') {
                    logger.debug("Disconnected a user while managing downstream input due to null input.");
                    this.disconnect();
                    break;
                }

                String line = lineBuilder.toString();

                logger.debug("Downstream: {}.", line.trim());

                line = line.replaceAll(ADDRESS_REGEX, BOGUS_ADDRESS);

                UpstreamOutputStream.write(line.getBytes());
                UpstreamOutputStream.flush();
            } catch (IOException e) {
                this.disconnect();
                logger.debug("Disconnected a user while managing downstream input.");
            }
        }
    }

    private void manageUpstreamInput() {
        while (Alive) {
            try {
                String line = UpstreamInputStream.readLine();

                if (line == null) {
                    logger.debug("Disconnected a user while managing upstream input due to null input.");

                    this.disconnect();
                    break;
                }

                logger.debug("Upstream: {}.", line.trim());


                line += '\n';


                line = line.replaceAll(ADDRESS_REGEX, BOGUS_ADDRESS);
                DownstreamOutputStream.write(line.getBytes());
                DownstreamOutputStream.flush();
            } catch (IOException e) {
                this.disconnect();
                logger.debug("Disconnected a user while managing upstream input.");
            }
        }
    }

    public void disconnect() {
        try {
            Alive = false;
            DownatreamSocket.close();
            UpstreamSocket.close();
        } catch (IOException e) {
            logger.error("An IO exception was thrown while closing a socket.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }
}
