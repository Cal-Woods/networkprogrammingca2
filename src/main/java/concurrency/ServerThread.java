package concurrency;

import interfaces.Server;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs a server interface implementation for multithreading capabilities.
 *
 * @author Cal Woods
 */
@Slf4j
public class ServerThread implements Runnable {
    Server server;

    public ServerThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {

    }
}
