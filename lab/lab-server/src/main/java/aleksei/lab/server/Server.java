package aleksei.lab.server;

import executor.CommandExecutor;
import manager.CollectionLoader;
import manager.CollectionManager;
import manager.Reader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    private static final int DEFAULT_PORT = 9876;
    private static final int IDLE_DELAY_MS = 50;
    private static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: server <collection-file> [port]");
            return;
        }

        String collectionFile = args[0];
        int port = parsePort(args);

        Scanner scanner = new Scanner(new ByteArrayInputStream(new byte[0]));
        CollectionManager collectionManager = new CollectionManager(scanner, collectionFile);
        CollectionLoader loader = new CollectionLoader(collectionManager);
        try {
            loader.loadCollection(collectionFile);
        } catch (Exception e) {
            System.out.println("File load warning: " + e.getMessage());
            logger.error("File load warning: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            String result = collectionManager.save();
            System.out.println("Server shutdown: " + result);
        }));

        CommandExecutor commandExecutor = new CommandExecutor(collectionManager);
        Reader reader = new Reader(collectionManager, commandExecutor);
        commandExecutor.setReader(reader);
        commandExecutor.initCommands();
        ClientRequestHandler clientRequestHandler = new ClientRequestHandler(reader);
        ServerConsoleReader serverConsoleReader = new ServerConsoleReader();
        try (ConnectionAcceptor connectionAcceptor = new ConnectionAcceptor(port)) {
            System.out.println("Server started on port " + port);
            while (true) {
                SocketChannel clientChannel = connectionAcceptor.accept();
                if (clientChannel == null) {
                    serverConsoleReader.executeAvailableCommand(collectionManager);
                    waitForConnection();
                } else {
                    clientRequestHandler.handle(clientChannel);
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            logger.error("Server error: " + e.getMessage());
        }
    }

    private static int parsePort(String[] args) {
        if (args.length < 2) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port '" + args[1] + "', using default " + DEFAULT_PORT);
            logger.error("Invalid port '" + args[1] + "', using default " + DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }

    private static void waitForConnection() {
        try {
            Thread.sleep(IDLE_DELAY_MS);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
