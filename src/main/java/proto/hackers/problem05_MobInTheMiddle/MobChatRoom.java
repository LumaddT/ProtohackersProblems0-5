package proto.hackers.problem05_MobInTheMiddle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

class MobChatRoom {
    private static final Logger logger = LogManager.getLogger();

    private static final String UPSTREAM_URL = "chat.protohackers.com";
    private static final int UPSTREAM_PORT = 16963;

    private static final Set<MobChatUser> CONNECTED_USERS = new HashSet<>();

    public static void connectUser(Socket downstreamSocket) {
        Socket upstreamSocket;
        try {
            upstreamSocket = new Socket(UPSTREAM_URL, UPSTREAM_PORT);
            MobChatUser user = new MobChatUser(downstreamSocket, upstreamSocket);
            CONNECTED_USERS.add(user);
            user.run();
        } catch (IOException e) {
            logger.fatal("An IO exception was thrown by a Socket or one of its streams.\n{}\n{}", e.getMessage(), e.getStackTrace());
        }
    }

    public static void disconnectAll() {
        for (MobChatUser user : CONNECTED_USERS) {
            user.disconnect();
        }
    }

    public static void cleanConnectedUsers() {
        CONNECTED_USERS.removeIf(u -> !u.isAlive());
    }
}
