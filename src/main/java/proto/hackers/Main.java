package proto.hackers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.hackers.problem02_MeansToAnEnd.MeansToAnEnd;

@SuppressWarnings("CommentedOutCode")
public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Starting up...");

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownRoutine));

//        new Thread(() -> SmokeTest.run(10_000)).start();
//        new Thread(() -> PrimeTime.run(10_001)).start();
        new Thread(() -> MeansToAnEnd.run(10_002)).start();
    }

    private static void shutdownRoutine() {
        logger.info("Shutting down...");

//        SmokeTest.stop();
//        PrimeTime.stop();
        MeansToAnEnd.stop();

        logger.info("Have a nice day!.");
    }
}
