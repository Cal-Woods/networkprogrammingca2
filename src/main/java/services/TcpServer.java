package services;

import exceptions.InvalidEmailFormatException;
import interfaces.EmailManager;
import interfaces.Server;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import model.Email;
import utils.Validators;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * Provides TCP server object with built-in TCP actions as a service.
 */
@Slf4j
public class TcpServer implements Server {
    int port;
    int backLog = -1;
    @NonNull InetAddress address;
    @NonNull ServerSocket serverSocket;
    Socket clientSocket;
    @NonNull EmailManager emails;
    private final String separator;

    private static final int DATA_SIZE = 5;
    private static int idGenerator = 1;
    /**
     * Setup instance with given port to use in binding to current host ip address.
     * @param port Given port, between 0 - 65535
     * @param separator Given {@link String} for splitting input into tokens
     */
    public TcpServer(int port, String separator, EmailManager emails) throws IOException {
        setPort(port);
        this.address = InetAddress.getLocalHost();

        this.serverSocket = new ServerSocket(port);
        this.emails = emails;
        //Set separator
        this.separator = setSeparator(separator);
    }

    /**
     * Setup instance with given port, address and backlog values.
     * @param port Given port, between 0 - 65535
     * @param backLog given backlog as requested max number of connections
     * @param address Given {@link InetAddress} object as ip address, cannot be null
     */
    public TcpServer(int port, int backLog, InetAddress address, String separator, EmailManager emails) throws IOException {
        setPort(port);
        setBackLog(backLog);
        this.address = address;

        this.serverSocket = new ServerSocket(port, backLog, address);
        this.emails = emails;

        //Validate separator
        this.separator = setSeparator(separator);
    }

    /**
     * Starts TCP server by listening for connections.
     *
     * @implNote Method blocks until connection is made.
     */
    public void startServer() {
        try {
            log.info("Starting TCP server on port " + port);
            this.clientSocket = serverSocket.accept();
        }
        catch (IOException e) {
            log.error("Could not accept connection and create clientSocket! {}", e.toString());
            System.err.println("Could not start server!" + e.toString());
        }
    }

    /**
     * Stops TCP server and closes connections
     */
    public void stopServer() {
        try {
            this.clientSocket.close();
            this.serverSocket.close();
        }
        catch (IOException e) {
            log.error("Could not close connection and close clientSocket! {}", e.toString());
            System.err.println("Could not close server!" + e.toString());
        }
    }
    /**
     * Sends an email by storing it as an email object in-memory.
     * @param email Given {@link Email} to send
     * @return True if email sent successfully, else false
     */
    @Override
    public boolean saveEmail(Email email) {
        if(email == null) {
            throw new IllegalArgumentException("Given Email object was null!");
        }

        //Store email
        emails.storeEmail(email);

        return true;
    }

    /**
     * Parses incoming email from connected client.
     * @return Parsed email as {@link Email} object.
     * @throws InvalidEmailFormatException From private String method
     * validateEmailData(String inputData).
     */
    @Override
    public Email receiveEmail() throws InvalidEmailFormatException {
        Email email = null;

        try {
            Scanner inputScanner = new Scanner(this.clientSocket.getInputStream());
            String inputData = inputScanner.nextLine();

            String safeData = null;

            try {
                safeData = Validators.validateStringData(inputData);
            }
            catch(IllegalArgumentException e) {
                log.error("Could not perform receive email operation as given inputData was null or blank! {}", e.toString());
                throw new IllegalArgumentException("Could not perform receive email operation as received data was not valid!");
            }

            //Split safeData
            String[] splitData = safeData.split(this.separator);

            Validators.validateEmail(splitData[0]);
            Validators.validateEmail(splitData[1]);

            int id = idGenerator++;
            //Build Email
            email = new Email(id, splitData[0], splitData[1], splitData[2], splitData[3]);


        }
        catch (IOException e) {
            log.error("Could not receive email due to IOException! {}", e.toString());
            return null;
        }

        return email;
    }

    @Override
    public void sendEmailsForRecipient(String recipient) {

    }

    private void setPort(int port) {
        if (port < 0 || port > 65535) {
            log.error("Could not set port as given port was out of range! {}", port);
            throw new IllegalArgumentException("Given port must be between 0 and 65535!");
        }

        this.port = port;
    }

    private void setBackLog(int backLog) {
        if (backLog < 0) {
            log.error("Could not set back log as given back log cannot be negative! {}", backLog);
            throw new IllegalArgumentException("Given back log must not be < 0!");
        }

        this.backLog = backLog;
    }

    private String setSeparator(String separator) {
        try {
            Validators.validateStringData(separator);
            return separator;
        }
        catch (IllegalArgumentException e) {
            log.error("Could not set separator! {}", separator);
            throw new IllegalArgumentException("Given separator was null or blank!");
        }
    }
}