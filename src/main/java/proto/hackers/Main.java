package proto.hackers;

import proto.hackers.part00_SmokeTest.SmokeTest;

public class Main {
    public static void main(String[] args) {
        new Thread(() -> SmokeTest.run(10_000)).start();

        // TODO: stop
    }
}