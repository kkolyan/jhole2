package net.kkolyan.jhole2.war;

import java.io.IOException;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class SelfSpammer {
    public static void main(final String[] args) throws IOException, InterruptedException {
        new Thread() {
            @Override
            public void run() {
                try {
                    SpamReceiver.main(args);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        Thread.sleep(2000);
        Spammer.main(args);
        System.exit(0);
    }
}
