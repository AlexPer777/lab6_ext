package manager;

import executor.CommandExecutor;
import response.Response;
import response.ResponseStatus;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class ApplicationRunner {

    public void run(String[] args) {
        if (args.length == 0) {
            System.out.println("File not chosen");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        CollectionManager collectionManager = new CollectionManager(scanner, args[0]);
        ResponsePrinter responsePrinter = new ResponsePrinter();
        CollectionLoader loader = new CollectionLoader(collectionManager);
        try {
            loader.loadCollection(args[0]);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + args[0]);
            return;
        }

        CommandExecutor commandExecutor = new CommandExecutor(collectionManager);
        Reader reader = new Reader(collectionManager, commandExecutor);

        commandExecutor.setReader(reader);
        commandExecutor.initCommands();

        startInteractiveMode(reader, responsePrinter, scanner);
    }

    private void startInteractiveMode(Reader reader, ResponsePrinter responsePrinter, Scanner scanner) {
        System.out.println("Program is running");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            Response response = reader.getLine(line);
            responsePrinter.print(response);
            if (response.getStatus() == ResponseStatus.EXIT) {
                break;
            }
        }

        System.out.println("EOF detected. Program terminated");
    }
}
