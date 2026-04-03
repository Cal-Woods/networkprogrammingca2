import concurrency.ServerThread;
import interfaces.EmailManager;
import interfaces.Server;
import lombok.extern.slf4j.Slf4j;
import services.TcpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

@Slf4j
public class TcpMultiThreadServer {
    //Setup
    static int SERVER_PORT = 5555;
    static InetAddress SERVER_ADDRESS;
    static EmailManager emailManager;

    //Set max number of connections
    static int BACK_LOG = 5;
    static String separator = "##";
    static Runnable serverThread;

    public static void main(String[] args) throws IOException, InterruptedException {
        Server tcpServer = new TcpServer(SERVER_PORT, BACK_LOG, SERVER_ADDRESS, separator, emailManager);
        serverThread = new ServerThread();
        Thread[] threads = new Thread[BACK_LOG];
        //Setup threads Thread array
        for (int i = 0; i < BACK_LOG; i++) {
            threads[i] = new Thread(serverThread);
            //Start threads[i]
            threads[i].start();
        }
    }

    /**
     * Gets user input via a local {@link Scanner} object sc.
     * @param prompt Given prfompt to display
     * @return Obtained {@link String} from {@link Scanner}
     */
    private static String getInput(String prompt) {
        Scanner sc = new Scanner(System.in);
        System.out.print(prompt);
        return sc.nextLine();
    }
}
