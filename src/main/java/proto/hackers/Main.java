package proto.hackers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proto.hackers.problem05_SmokeTest.MobInTheMiddle;

@SuppressWarnings("CommentedOutCode")
public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) {
        logger.info("Starting up...");

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownRoutine));

//        new Thread(() -> SmokeTest.run(10_000)).start();
//        new Thread(() -> PrimeTime.run(10_001)).start();
//        new Thread(() -> MeansToAnEnd.run(10_002)).start();
//        new Thread(() -> BudgetChat.run(10_003)).start();
//        new Thread(() -> UnusualDatabaseProgram.run(10_004)).start();
        new Thread(() -> MobInTheMiddle.run(10_005)).start();
    }

    private static void shutdownRoutine() {
        logger.info("Shutting down...");

//        SmokeTest.stop();
//        PrimeTime.stop();
//        MeansToAnEnd.stop();
//        BudgetChat.stop();
//        UnusualDatabaseProgram.stop();
        MobInTheMiddle.stop();

        logger.info("Have a nice day!.");
    }
}
