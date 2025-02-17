package proto.hackers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.hackers.part01_PrimeTime.PrimeTime;

public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Starting up...");

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownRoutine));

//        new Thread(() -> SmokeTest.run(10_000)).start();
        new Thread(() -> PrimeTime.run(10_001)).start();
    }

    private static void shutdownRoutine() {
        logger.info("Shutting down...");

//        SmokeTest.stop();
        PrimeTime.stop();

        logger.info("Have a nice day!.");
    }
}
