package aleksei.lab.server;

import manager.CollectionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

public class ServerConsoleReader {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public void executeAvailableCommand(CollectionManager collectionManager) throws IOException {
        if (!reader.ready()) {
            return;
        }
        String command = reader.readLine();
        if (command == null || command.isBlank()) {
            return;
        }
        Stream.of(command.trim())
                .filter("save"::equals)
                .findFirst()
                .ifPresentOrElse(
                        saveCommand -> System.out.println(collectionManager.save()),
                        () -> System.out.println("Unknown server command: " + command)
                );
    }
}
